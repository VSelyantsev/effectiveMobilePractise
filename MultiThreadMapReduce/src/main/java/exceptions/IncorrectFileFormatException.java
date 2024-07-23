package exceptions;

public class IncorrectFileFormatException extends RuntimeException {
    public IncorrectFileFormatException(String message) {
        super(String.format("Incorrect file name: %s", message));
    }
}
