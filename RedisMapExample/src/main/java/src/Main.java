import redis.clients.jedis.exceptions.JedisException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        try {
            RedisMap redisMap = new RedisMap("localhost", 6379);

            String oldValue = redisMap.put("1", "firstPackageData");
            System.out.println(oldValue);

            String anotherOldValue = redisMap.put("2", "secondPackageData");
            System.out.println(anotherOldValue);

            boolean isEmpty = redisMap.isEmpty();
            System.out.println(isEmpty);

            String firstValue = redisMap.get("1");
            System.out.println(firstValue);

            String secondValue = redisMap.get("2");
            System.out.println(secondValue);

            System.out.println(redisMap.isEmpty());

            System.out.println(redisMap.containsKey("1"));

            redisMap.clear();
            System.out.println(redisMap.isEmpty());
        } catch (JedisException exception) {
            logger.log(Level.INFO, exception.getMessage());
        }
    }
}
