package src.exception;

import redis.clients.jedis.exceptions.JedisException;

public class ExecuteException extends JedisException {
    public ExecuteException(String message, Throwable cause) {
        super(String.format("Cannot execute command: %s", message), cause);
    }
}
