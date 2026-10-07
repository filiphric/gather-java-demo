import test from 'node:test';
import assert from 'node:assert/strict';
import { availableStarts, minutes, shiftDate, timeAt } from '../../main/resources/static/availability.js';

const day = '2026-10-08';
const meeting = (start, end, roomId = 'the-nook', date = day) => ({
  roomId, start: `${date}T${start}:00`, end: `${date}T${end}:00`
});

test('offers adjacent slots and rejects slots that partly overlap a reservation', () => {
  const slots = availableStarts([meeting('10:00', '11:00')], 'the-nook', day);
  assert.ok(slots.includes('09:00'));
  assert.ok(slots.includes('11:00'));
  for (const time of ['09:30', '10:00', '10:30']) assert.ok(!slots.includes(time));
});

test('only bookings in the selected room and day block a slot', () => {
  const slots = availableStarts([
    meeting('09:00', '10:00', 'the-studio'),
    meeting('09:00', '10:00', 'the-nook', '2026-10-09')
  ], 'the-nook', day);
  assert.ok(slots.includes('09:00'));
});

test('suggested meetings fit entirely inside opening hours', () => {
  const slots = availableStarts([], 'the-nook', day, 90);
  assert.equal(slots[0], '08:00');
  assert.equal(slots.at(-1), '16:30');
  assert.equal(timeAt(minutes(slots.at(-1)) + 90), '18:00');
});

test('a full room has no suggested starts', () => {
  assert.deepEqual(availableStarts([meeting('08:00', '18:00')], 'the-nook', day), []);
});

test('date navigation crosses months, years, and daylight saving dates', () => {
  assert.equal(shiftDate('2026-12-31', 1), '2027-01-01');
  assert.equal(shiftDate('2026-03-01', -1), '2026-02-28');
  assert.equal(shiftDate('2026-10-25', 1), '2026-10-26');
});
