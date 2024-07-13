public class Main {

    public static void main(String[] args) {
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

        redisMap.clear();
        System.out.println(redisMap.isEmpty());
    }
}
