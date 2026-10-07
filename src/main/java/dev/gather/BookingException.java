package dev.gather;

public class BookingException extends RuntimeException {
    private final int status;

    public BookingException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() { return status; }
}
