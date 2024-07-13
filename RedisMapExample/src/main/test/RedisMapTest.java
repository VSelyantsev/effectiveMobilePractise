import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class RedisMapTest {

    private static RedisMap redisMap;

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
}
