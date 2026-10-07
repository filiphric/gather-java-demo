const $ = (selector) => document.querySelector(selector);
const state = { rooms: [], bookings: [], today: '', quoteVersion: 0 };
const money = (amount) => new Intl.NumberFormat('en-IE', { style: 'currency', currency: 'EUR' }).format(amount);
const dialog = $('#booking-dialog');
const form = $('#booking-form');
let toastTimeout;

async function api(path, options = {}) {
  const response = await fetch(`/api${path}`, {
    ...options, headers: { 'Content-Type': 'application/json', ...options.headers }
  });
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.detail || 'Something went wrong. Please try again.');
  }
  return response.status === 204 ? null : response.json();
}

function toast(message) {
  $('#toast').textContent = message;
  $('#toast').hidden = false;
  clearTimeout(toastTimeout);
  toastTimeout = setTimeout(() => { $('#toast').hidden = true; }, 5000);
}

function element(tag, className, text) {
  const node = document.createElement(tag);
  node.className = className;
  if (text !== undefined) node.textContent = text;
  return node;
}

function renderRooms() {
  const rooms = state.rooms.filter(room => room.capacity >= Number($('#capacity-filter').value));
  $('#room-list').replaceChildren(...rooms.map(room => {
    const card = $('#room-template').content.firstElementChild.cloneNode(true);
    card.querySelector('.room-visual').classList.add(room.color);
    card.querySelector('.room-image').src = `/rooms/${room.id}.svg`;
    card.querySelector('.room-capacity').textContent = `♧ Up to ${room.capacity} people`;
    card.querySelector('.room-number').textContent = `0${state.rooms.indexOf(room) + 1}`;
    card.querySelector('h3').textContent = room.name;
    const price = card.querySelector('.room-price');
    price.append(document.createTextNode(money(room.hourlyRate)), element('small', '', ' / hour'));
    card.querySelector('.room-description').textContent = room.description;
    card.querySelector('.amenities').replaceChildren(...room.amenities.map(item => element('span', '', item)));
    const book = card.querySelector('.room-book');
    book.setAttribute('aria-label', `Book ${room.name}`);
    book.addEventListener('click', () => openBooking(room.id));
    return card;
  }));
}

function confirmCancellation(title) {
  const confirmation = $('#cancel-dialog');
  $('#cancel-description').textContent = `Cancel “${title}”? The room will be available for someone else.`;
  confirmation.returnValue = 'keep';
  confirmation.showModal();
  return new Promise(resolve => confirmation.addEventListener('close', () => resolve(confirmation.returnValue === 'cancel'), { once: true }));
}
$('#keep-booking').addEventListener('click', () => $('#cancel-dialog').close('keep'));
$('#confirm-cancel').addEventListener('click', () => $('#cancel-dialog').close('cancel'));

function renderBookings() {
  const today = state.bookings.filter(booking => booking.start.startsWith(state.today));
  $('#stat-bookings').textContent = today.length.toString().padStart(2, '0');
  $('#stat-hours').textContent = today.reduce((sum, booking) => {
    const minutes = (value) => Number(value.slice(11, 13)) * 60 + Number(value.slice(14, 16));
    return sum + (minutes(booking.end) - minutes(booking.start)) / 60;
  }, 0).toLocaleString('en', { maximumFractionDigits: 2 });
  $('#booking-count').textContent = state.bookings.length;
  const selected = state.bookings.filter(booking => booking.start.startsWith($('#booking-date').value));
  const list = $('#booking-list');
  list.replaceChildren();
  if (!selected.length) {
    const empty = element('div', 'empty');
    empty.append(element('strong', '', 'A little room for possibility.'), document.createTextNode('No bookings on this day. Make it a good one.'));
    list.append(empty);
    return;
  }
  selected.forEach(booking => {
    const room = state.rooms.find(item => item.id === booking.roomId);
    const row = element('div', 'booking-row');
    const title = element('div', 'booking-title');
    const names = element('div', 'booking-names');
    names.append(element('strong', 'booking-name', booking.title), element('span', 'booking-room', room.name));
    title.append(element('span', `booking-symbol ${room.color}`, '▱'), names);
    const cancel = element('button', 'cancel-booking', '×');
    cancel.setAttribute('aria-label', `Cancel ${booking.title}`);
    cancel.addEventListener('click', async () => {
      if (!await confirmCancellation(booking.title)) return;
      cancel.disabled = true;
      try {
        await api(`/bookings/${booking.id}`, { method: 'DELETE' });
        await refreshBookings();
        toast('Booking cancelled. A little space opened up.');
      } catch (error) { toast(error.message); cancel.disabled = false; }
    });
    row.append(title, element('span', 'booking-time', `${booking.start.slice(11, 16)} – ${booking.end.slice(11, 16)}`),
      element('span', 'booking-people', `${booking.attendees} people`), element('span', 'booking-total', money(booking.total)), cancel);
    list.append(row);
  });
}

async function refreshBookings() {
  state.bookings = await api('/bookings');
  renderBookings();
}

function requestFromForm() {
  const values = Object.fromEntries(new FormData(form));
  return { roomId: values.roomId, title: values.title, organizer: values.organizer,
    attendees: Number(values.attendees), start: `${values.date}T${values.start}:00`, end: `${values.date}T${values.end}:00` };
}

async function updateQuote() {
  const version = ++state.quoteVersion;
  const request = requestFromForm();
  const room = state.rooms.find(item => item.id === request.roomId);
  form.elements.attendees.max = room.capacity;
  $('#quote-total').textContent = '—';
  if (!form.elements.date.value || !form.elements.start.value || !form.elements.end.value) return;
  try {
    const quote = await api('/quotes', { method: 'POST', body: JSON.stringify({ ...request, title: request.title || 'Meeting', organizer: request.organizer || 'guest@example.com' }) });
    if (version === state.quoteVersion) $('#quote-total').textContent = money(quote.total);
  } catch (_) { if (version === state.quoteVersion) $('#quote-total').textContent = '—'; }
}

function openBooking(roomId) {
  form.reset();
  $('#occurrence-field').hidden = true;
  form.elements.roomId.value = roomId || state.rooms[0].id;
  form.elements.date.min = state.today;
  form.elements.date.value = $('#booking-date').value < state.today ? state.today : $('#booking-date').value;
  $('#form-error').hidden = true;
  dialog.showModal();
  updateQuote();
}

$('#repeat-weekly').addEventListener('change', () => {
  $('#occurrence-field').hidden = !$('#repeat-weekly').checked;
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
    $('#booking-date').value = form.elements.date.value;
    dialog.close();
    await refreshBookings();
    toast(recurring ? `Your weekly series is booked. Total: ${money(result.total)}.` : 'You’re booked. Good things are on the calendar.');
  } catch (error) {
    $('#form-error').textContent = error.message;
    $('#form-error').hidden = false;
  } finally { submit.disabled = false; }
});

$('#close-dialog').addEventListener('click', () => dialog.close());
$('#capacity-filter').addEventListener('change', renderRooms);
$('#booking-date').addEventListener('change', renderBookings);
document.querySelectorAll('.new-booking').forEach(button => {
  button.disabled = true;
  button.addEventListener('click', () => openBooking());
});
document.querySelectorAll('.nav-link').forEach(link => link.addEventListener('click', () => {
  document.querySelectorAll('.nav-link').forEach(item => item.classList.toggle('active', item === link));
}));

async function init() {
  try {
    const [rooms, bookings, workspace] = await Promise.all([api('/rooms'), api('/bookings'), api('/workspace')]);
    Object.assign(state, { rooms, bookings, today: workspace.today });
    $('#booking-date').value = state.today;
    $('#room-select').replaceChildren(...rooms.map(room => {
      const option = element('option', '', room.name);
      option.value = room.id;
      return option;
    }));
    renderRooms();
    renderBookings();
    document.querySelectorAll('.new-booking').forEach(button => { button.disabled = false; });
  } catch (error) { toast(`Couldn't load the workspace. ${error.message}`); }
}
init();
