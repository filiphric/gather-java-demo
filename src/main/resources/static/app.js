import { availableStarts, minutes, shiftDate, timeAt } from './availability.js';

const $ = selector => document.querySelector(selector);
const state = { rooms: [], bookings: [], today: '', organizer: '', quoteVersion: 0 };
const money = amount => new Intl.NumberFormat('en-IE', { style: 'currency', currency: 'EUR' }).format(amount);
const dialog = $('#booking-dialog');
const form = $('#booking-form');
let toastTimeout;

async function api(path, options = {}) {
  const response = await fetch(`/api${path}`, {
    ...options, headers: { 'Content-Type': 'application/json', ...options.headers }
  });
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.detail || 'Please try again.');
  }
  return response.status === 204 ? null : response.json();
}

function element(tag, className, text) {
  const node = document.createElement(tag);
  node.className = className;
  if (text !== undefined) node.textContent = text;
  return node;
}

function icon(name) {
  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  svg.classList.add('icon');
  svg.setAttribute('aria-hidden', 'true');
  const use = document.createElementNS('http://www.w3.org/2000/svg', 'use');
  use.setAttribute('href', `#i-${name}`);
  svg.append(use);
  return svg;
}

function toast(message) {
  $('#toast').replaceChildren(icon('check'), document.createTextNode(message));
  $('#toast').hidden = false;
  clearTimeout(toastTimeout);
  toastTimeout = setTimeout(() => { $('#toast').hidden = true; }, 4500);
}

function renderRooms() {
  const day = $('#booking-date').value;
  const rooms = state.rooms.filter(room => room.capacity >= Number($('#capacity-filter').value));
  $('#room-list').replaceChildren(...rooms.map(room => {
    const card = $('#room-template').content.firstElementChild.cloneNode(true);
    card.querySelector('.room-image').src = `/rooms/${room.id}.svg`;
    card.querySelector('h2').textContent = room.name;
    card.querySelector('.room-price').append(document.createTextNode(money(room.hourlyRate)), element('small', '', ' /hr'));
    card.querySelector('.room-capacity').append(icon('people'), document.createTextNode(`${room.capacity} people`));
    const amenities = { Whiteboard: 'board', Coffee: 'coffee', Screen: 'screen', 'Video call': 'video' };
    room.amenities.forEach(name => {
      const amenity = element('span', 'amenity');
      amenity.title = name;
      amenity.setAttribute('role', 'img');
      amenity.setAttribute('aria-label', name);
      amenity.append(icon(amenities[name]));
      card.querySelector('.amenities').append(amenity);
    });
    const starts = availableStarts(state.bookings, room.id, day);
    const status = card.querySelector('.availability');
    status.textContent = starts.length ? 'Available' : 'No 1-hour slots';
    status.classList.toggle('full', !starts.length);
    const slots = card.querySelector('.time-slots');
    starts.slice(0, 3).forEach(start => {
      const button = element('button', 'time-slot', start);
      button.setAttribute('aria-label', `Book ${room.name} at ${start}`);
      button.addEventListener('click', () => openBooking(room, start));
      slots.append(button);
    });
    if (!starts.length) slots.append(element('span', 'no-slots', 'Try a shorter meeting'));
    const book = card.querySelector('.room-book');
    book.setAttribute('aria-label', `Book ${room.name}`);
    book.addEventListener('click', () => openBooking(room, starts[0] || '08:00'));
    return card;
  }));
  $('#room-list').setAttribute('aria-busy', 'false');
}

function renderBookings() {
  const day = $('#booking-date').value;
  const selected = state.bookings.filter(booking => booking.start.startsWith(day));
  $('#booking-count').textContent = selected.length;
  $('#schedule-date').textContent = day === state.today ? 'Today' : new Date(`${day}T12:00:00`).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' });
  const list = $('#booking-list');
  list.replaceChildren();
  if (!selected.length) {
    const empty = element('div', 'empty');
    empty.append(icon('calendar'), document.createTextNode('No bookings'));
    list.append(empty);
  }
  selected.forEach(booking => {
    const room = state.rooms.find(item => item.id === booking.roomId);
    const row = element('div', 'booking-row');
    const time = element('div', 'booking-time', booking.start.slice(11, 16));
    time.append(element('small', '', booking.end.slice(11, 16)));
    const title = element('div', 'booking-title');
    title.append(element('strong', '', booking.title), element('small', '', `${booking.attendees} people`));
    const cancel = element('button', 'icon-button cancel-booking');
    cancel.append(icon('close'));
    cancel.title = 'Cancel booking';
    cancel.setAttribute('aria-label', `Cancel ${booking.title}`);
    cancel.addEventListener('click', async () => {
      if (!await confirmCancellation(booking.title)) return;
      cancel.disabled = true;
      try {
        await api(`/bookings/${booking.id}`, { method: 'DELETE' });
        state.bookings = state.bookings.filter(item => item.id !== booking.id);
        render();
        toast('Booking cancelled');
      } catch (error) { toast(error.message); cancel.disabled = false; }
    });
    row.append(time, title, element('span', `room-tag ${room.color}`, room.name), element('span', 'booking-total', money(booking.total)), cancel);
    list.append(row);
  });
}

function render() {
  $('#previous-day').disabled = $('#booking-date').value <= state.today;
  $('#today-button').setAttribute('aria-pressed', String($('#booking-date').value === state.today));
  renderRooms();
  renderBookings();
}

function confirmCancellation(title) {
  const confirmation = $('#cancel-dialog');
  $('#cancel-description').textContent = title;
  confirmation.returnValue = 'keep';
  confirmation.showModal();
  return new Promise(resolve => confirmation.addEventListener('close', () => resolve(confirmation.returnValue === 'cancel'), { once: true }));
}

function requestFromForm() {
  const values = Object.fromEntries(new FormData(form));
  const end = timeAt(minutes(values.start) + Number(values.duration));
  return { roomId: values.roomId, title: values.title, organizer: values.organizer,
    attendees: Number(values.attendees), start: `${values.date}T${values.start}:00`, end: `${values.date}T${end}:00` };
}

async function updateQuote() {
  const version = ++state.quoteVersion;
  $('#quote-total').textContent = '—';
  $('#quote-label').textContent = $('#repeat-weekly').checked ? 'Per meeting' : 'Total';
  if (!form.elements.date.value || !form.elements.start.value) return;
  const request = requestFromForm();
  try {
    const quote = await api('/quotes', { method: 'POST', body: JSON.stringify({ ...request, title: request.title || 'Meeting', organizer: 'guest@example.com' }) });
    if (version === state.quoteVersion) $('#quote-total').textContent = money(quote.total);
  } catch (_) { if (version === state.quoteVersion) $('#quote-total').textContent = '—'; }
}

function openBooking(room, start) {
  form.reset();
  $('#occurrence-field').hidden = true;
  form.elements.occurrences.disabled = true;
  form.elements.roomId.value = room.id;
  form.elements.date.min = state.today;
  form.elements.date.value = $('#booking-date').value;
  form.elements.start.value = start;
  form.elements.attendees.value = $('#capacity-filter').value;
  form.elements.attendees.max = room.capacity;
  form.elements.organizer.value = state.organizer;
  $('#dialog-title').textContent = room.name;
  $('#dialog-image').src = `/rooms/${room.id}.svg`;
  $('#form-error').hidden = true;
  dialog.showModal();
  form.elements.organizer.focus();
  updateQuote();
}

$('#repeat-weekly').addEventListener('change', () => {
  const repeat = $('#repeat-weekly').checked;
  $('#occurrence-field').hidden = !repeat;
  form.elements.occurrences.disabled = !repeat;
});
form.addEventListener('input', updateQuote);
form.addEventListener('submit', async event => {
  event.preventDefault();
  const submit = $('.submit-booking');
  submit.disabled = true;
  $('#form-error').hidden = true;
  try {
    const request = requestFromForm();
    const recurring = $('#repeat-weekly').checked;
    const path = recurring ? '/bookings/recurring' : '/bookings';
    const payload = recurring ? { booking: request, occurrences: Number(form.elements.occurrences.value) } : request;
    const result = await api(path, { method: 'POST', body: JSON.stringify(payload) });
    state.organizer = request.organizer;
    state.bookings.push(...(recurring ? result.bookings : [result]));
    state.bookings.sort((first, second) => first.start.localeCompare(second.start));
    $('#booking-date').value = form.elements.date.value;
    dialog.close();
    render();
    toast(recurring ? `Series booked · ${money(result.total)}` : 'Room booked');
  } catch (error) {
    $('#form-error').textContent = error.message;
    $('#form-error').hidden = false;
  } finally { submit.disabled = false; }
});

$('#close-dialog').addEventListener('click', () => dialog.close());
$('#keep-booking').addEventListener('click', () => $('#cancel-dialog').close('keep'));
$('#confirm-cancel').addEventListener('click', () => $('#cancel-dialog').close('cancel'));
$('#capacity-filter').addEventListener('change', renderRooms);
$('#booking-date').addEventListener('change', () => {
  if (!$('#booking-date').value || $('#booking-date').value < state.today) $('#booking-date').value = state.today;
  render();
});
$('#previous-day').addEventListener('click', () => { $('#booking-date').value = shiftDate($('#booking-date').value, -1); render(); });
$('#next-day').addEventListener('click', () => { $('#booking-date').value = shiftDate($('#booking-date').value, 1); render(); });
$('#today-button').addEventListener('click', () => { $('#booking-date').value = state.today; render(); });

async function init() {
  try {
    const [rooms, bookings, workspace] = await Promise.all([api('/rooms'), api('/bookings'), api('/workspace')]);
    Object.assign(state, { rooms, bookings, today: workspace.today });
    $('#booking-date').value = state.today;
    $('#booking-date').min = state.today;
    render();
  } catch (error) {
    $('#page-error').textContent = `Couldn't load rooms. ${error.message}`;
    $('#page-error').hidden = false;
    $('#room-list').replaceChildren();
    $('#room-list').setAttribute('aria-busy', 'false');
  }
}
init();
