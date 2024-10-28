package src.exceptions;

import java.io.IOException;

public class NotExistDirectory extends IOException {
    public NotExistDirectory(String message) {
        super(String.format("Can not create directory: %s", message));
    }
}
