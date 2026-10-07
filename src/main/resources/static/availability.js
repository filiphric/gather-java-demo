export function minutes(time) {
  const [hours, minutes] = time.split(':').map(Number);
  return hours * 60 + minutes;
}

export function timeAt(value) {
  return `${String(Math.floor(value / 60)).padStart(2, '0')}:${String(value % 60).padStart(2, '0')}`;
}

export function shiftDate(date, days) {
  const value = new Date(`${date}T12:00:00Z`);
  value.setUTCDate(value.getUTCDate() + days);
  return value.toISOString().slice(0, 10);
}

export function availableStarts(bookings, roomId, day, duration = 60) {
  const occupied = bookings.filter(booking => booking.roomId === roomId && booking.start.startsWith(day));
  const available = [];
  for (let start = 8 * 60; start + duration <= 18 * 60; start += 30) {
    const overlap = occupied.some(booking => start < minutes(booking.end.slice(11, 16))
      && start + duration > minutes(booking.start.slice(11, 16)));
    if (!overlap) available.push(timeAt(start));
  }
  return available;
}
