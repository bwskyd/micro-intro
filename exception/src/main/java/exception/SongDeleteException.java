package exception;

public class SongDeleteException extends RuntimeException {
    public SongDeleteException(String message) {
        super(message);
    }
}
