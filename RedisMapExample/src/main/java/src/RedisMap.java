package src;

import src.exception.ConnectionException;
import src.exception.ExecuteException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class RedisMap implements Map<String, String> {

    private final Jedis jedis;

    public RedisMap(String host, int port) {
        this.jedis = new Jedis(host, port);
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() throws ConnectionException, ExecuteException {
        try {
            return jedis.dbSize() == 0;
        } catch (JedisConnectionException e) {
            throw new ConnectionException(e.getMessage(), e.getCause());
        } catch (JedisException e) {
            throw new ExecuteException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public boolean containsKey(Object key) throws ConnectionException, ExecuteException {
        try {
            return jedis.exists((String) key);
        } catch (JedisConnectionException e) {
            throw new ConnectionException(e.getMessage(), e.getCause());
        } catch (JedisException e) {
            throw new ExecuteException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public String get(Object key) throws ConnectionException, ExecuteException {
        try {
            return jedis.get(key.toString());
        } catch (JedisConnectionException e) {
            throw new ConnectionException(e.getMessage(), e.getCause());
        } catch (JedisException e) {
            throw new ExecuteException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public String put(String key, String value) throws ConnectionException, ExecuteException {
        try {
            return jedis.setGet(key, value);
        } catch (JedisConnectionException e) {
            throw new ConnectionException(e.getMessage(), e.getCause());
        } catch (JedisException e) {
            throw new ExecuteException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public String remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {

    }

    @Override
    public void clear() {
        try {
            jedis.flushDB();
        } catch (JedisConnectionException e) {
            throw new ConnectionException(e.getMessage(), e.getCause());
        } catch (JedisException e) {
            throw new ExecuteException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public Set<String> keySet() {
        return null;
    }

    @Override
    public Collection<String> values() {
        return null;
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return null;
    }
}
