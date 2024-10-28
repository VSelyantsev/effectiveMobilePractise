package src.exceptions;

import java.io.IOException;

public class ClientReadWriteException extends IOException {
    public ClientReadWriteException(String message, Throwable cause) {
        super(String.format("Error reading/writing from/to client: %s", message), cause);
    }
}
