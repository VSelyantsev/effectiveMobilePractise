package src.service.runnable;

import src.exceptions.NotExistDirectory;
import src.model.KeyValue;
import src.model.TaskRequest;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MapWorker implements Runnable {

    private final static Logger logger = Logger.getLogger(MapWorker.class.getName());
    private static final String FILE_PROCESSED_PATH = "src/main/resources/files/processed/";
    private static final String FILE_PATH = "src/main/resources/files/";

    private final TaskRequest taskRequest;
    private final CountDownLatch latch;

    public MapWorker(TaskRequest taskRequest, CountDownLatch latch) {
        this.taskRequest = taskRequest;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            String mainContent = Files.readString(Paths.get(FILE_PATH + taskRequest.getFileName()));
            List<KeyValue> keyValues = map(taskRequest.getFileName(), mainContent);
            distribute(keyValues);
        } catch (IOException e) {
            logger.log(
                    Level.SEVERE,
                    "Problem with PATH:",
                    new NotExistDirectory(e.getMessage())
            );
        } finally {
            latch.countDown();
        }
    }

    private synchronized void distribute(List<KeyValue> keyValues) throws IOException {
        Map<Integer, List<KeyValue>> reduceTaskMap = new HashMap<>();

        for (int i = 0; i < taskRequest.getTaskReduce(); i++) {
            reduceTaskMap.put(i, new ArrayList<>());
        }

        for (KeyValue kv : keyValues) {
            int reduceTask = (kv.getWord().hashCode() & Integer.MAX_VALUE) % taskRequest.getTaskReduce();
            reduceTaskMap.get(reduceTask).add(kv);
        }

        Path actualDirectory = Paths.get(FILE_PROCESSED_PATH);
        if (Files.notExists(actualDirectory)) {
            try {
                Files.createDirectories(actualDirectory);
                logger.log(Level.INFO, "Directory created successfully");
            } catch (IOException e) {
                logger.log(
                        Level.SEVERE,
                        "Problem with creating:",
                        new NotExistDirectory(e.getMessage())
                );
            }
        }

        for (int i = 0; i < taskRequest.getTaskReduce(); i++) {
            String fileName = String.format("mr-%s-%d", taskRequest.getId().toString(), i);
            List<String> lines = reduceTaskMap.get(i)
                    .stream()
                    .map(KeyValue::toString)
                    .collect(Collectors.toList());

            Files.write(Paths.get(
                    FILE_PROCESSED_PATH + fileName
            ), lines);
        }
    }

    private synchronized List<KeyValue> map(String fileName, String content) {
        logger.log(Level.INFO, String.format("Start processing the file: %s", fileName));

        List<KeyValue> keyValues = new ArrayList<>();
        String[] words = content.split("\\s+");

        Arrays.stream(words).forEach(word -> {
                keyValues.add(new KeyValue(word, 1));
                logger.log(Level.INFO, String.format("KeyValue elem processed: %s", word));
            }
        );

        logger.log(Level.INFO, String.format("End processing the file: %s", fileName));
        return keyValues;
    }
}
