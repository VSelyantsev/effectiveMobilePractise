package src.exception;

import redis.clients.jedis.exceptions.JedisConnectionException;

public class ConnectionException extends JedisConnectionException {
    public ConnectionException(String message, Throwable cause) {
        super(String.format("Cannot establish a connection: %s", message), cause);
    }
}
