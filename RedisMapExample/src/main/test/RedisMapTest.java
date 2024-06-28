import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RedisMapTest {

    private RedisMap redisMap;

    @BeforeEach
    public void setUp() {
        this.redisMap = new RedisMap("localhost", 6379);
    }

    @Test
    public void testPut() {
        String actualValue = redisMap.put("firstKey", "firstValue");
        assertEquals("firstValue", actualValue);
    }

    @Test
    public void testGet() {
        redisMap.put("firstKey", "firstValue");
        assertEquals("firstValue", redisMap.get("firstKey"));
        assertEquals("secondValue", redisMap.put("firstKey", "secondValue"));
        assertEquals("secondValue", redisMap.get("firstKey"));
    }
}
