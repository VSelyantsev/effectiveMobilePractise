package src.exceptions;

import java.net.SocketException;

public class ClientSocketCloseException extends SocketException {
    public ClientSocketCloseException(String message) {
        super(String.format("Error creating or accessing a Socket: %s", message));
    }
}
