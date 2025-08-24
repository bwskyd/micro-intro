package exception;

public class SongCreateException extends RuntimeException {
    public SongCreateException(String message) {
        super(message);
    }
}
