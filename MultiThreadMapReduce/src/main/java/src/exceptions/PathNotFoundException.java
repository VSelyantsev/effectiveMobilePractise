package src.exceptions;

import java.io.IOException;

public class PathNotFoundException extends IOException {
    public PathNotFoundException(String message) {
        super(String.format("Can not find directory by current path: %s", message));
    }
}
