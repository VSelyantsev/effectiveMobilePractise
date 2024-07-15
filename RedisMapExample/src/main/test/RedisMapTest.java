import exception.ExecuteException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.lang.reflect.Executable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;

@Testcontainers
public class RedisMapTest {

    private static RedisMap redisMap;

    private static final String THROWABLE_EXECUTION_MESSAGE = "Cannot execute command: Simulated exception";
    private static final String THROWABLE_JEDIS_MESSAGE = "Simulated exception";

    @Container
    private static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:latest")
            .withExposedPorts(6379)
            .withReuse(true);

    @BeforeAll
    public static void setUp() {
        redisContainer.start();
        redisMap = new RedisMap("localhost", 6379);
        redisMap.clear();
    }

    @AfterAll
    public static void cleanUp() {
        redisMap.clear();
        redisContainer.stop();
    }

    @Test
    public void testPut() {
        String oldValue = redisMap.put("firstKey", "firstValue");
        assertNotNull(oldValue);

        String actualValue = redisMap.get("firstKey");
        assertEquals("firstValue", actualValue);
    }

    @Test
    public void testGet() {
        String oldValue = redisMap.put("firstKey", "firstValue");
        assertNull(oldValue);

        String actualValue = redisMap.get("firstKey");
        assertEquals("firstValue", actualValue);
    }

    @Test
    public void testIsEmpty() {
        boolean isEmpty = redisMap.isEmpty();
        assertFalse(isEmpty);
    }

    @Test
    public void testExecuteExceptionWithSimulatedExceptionMessage() {
        redisMap = Mockito.mock();
        Throwable stubbedThrowable = Mockito.mock();
        doThrow(new ExecuteException("Simulated exception", stubbedThrowable)).when(redisMap).get(Mockito.anyString());


        Throwable actualThrowable = assertThrows(
                ExecuteException.class,
                () -> redisMap.get(Mockito.anyString())
        );

        assertEquals(THROWABLE_EXECUTION_MESSAGE, actualThrowable.getMessage());
        assertEquals(ExecuteException.class, actualThrowable.getClass());
    }

    @Test
    public void testJedisExceptionWithSimulatedExceptionMessage() {
        redisMap = Mockito.mock();
        Throwable stubbedThrowable = Mockito.mock();
        doThrow(new JedisException("Simulated exception", stubbedThrowable))
                .when(redisMap)
                .put(Mockito.anyString(), Mockito.anyString());


        Throwable actualThrowable = assertThrows(
                JedisException.class,
                () -> redisMap.put(Mockito.anyString(), Mockito.anyString())
        );

        assertEquals(THROWABLE_JEDIS_MESSAGE, actualThrowable.getMessage());
        assertEquals(JedisException.class, actualThrowable.getClass());
    }
}
