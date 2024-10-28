package src.service.runnable;

import src.exceptions.NotExistDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ReduceWorker implements Runnable {

    private int taskReduceNumber;
    private List<File> files;
    private CountDownLatch latch;

    private static final Logger logger = Logger.getLogger(ReduceWorker.class.getName());

    private static final String FINAL_RESULT_PATH = "src/main/resources/files/result/";
    private static final String REDUCE_FILE_NAME = "reduce-result";

    public ReduceWorker(int taskReduceNumber, List<File> files, CountDownLatch latch) {
        this.taskReduceNumber = taskReduceNumber;
        this.files = files;
        this.latch = latch;
    }



    @Override
    public void run() {
        try {
            List<String> lines = readAllLinesFromFiles(files);
            Map<String, List<String>> sortedData = sortData(lines);
            reduceFinalResult(sortedData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            latch.countDown();
        }
    }

    private List<String> readAllLinesFromFiles(List<File> files) throws IOException {
        List<String> allLines = new ArrayList<>();
        for (File file : files) {
            allLines.addAll(Files.readAllLines(file.toPath()));
        }
        return allLines;
    }

    private synchronized Map<String, List<String>> sortData(List<String> lines) {
        Map<String, List<String>> sortedData = new TreeMap<>();

        for (String line : lines) {
            String[] parts = line.split(" ");
            String key = parts[0];
            String value = parts[1];
            sortedData.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return sortedData;
    }

    private synchronized String reduce(String key, List<String> values) {
        int valueSize = values.size();
        return key + " " + valueSize;
    }

    private synchronized void reduceFinalResult(Map<String, List<String>> sortedData) throws IOException {
        Path actualDirectory = Paths.get(FINAL_RESULT_PATH);
        if (Files.notExists(actualDirectory)) {
            try {
                Files.createDirectories(actualDirectory);
                logger.log(Level.INFO, "Directory created successfully");
            } catch (IOException e) {
                logger.log(
                        Level.SEVERE,
                        "Problem with PATH:",
                        new NotExistDirectory(e.getMessage())
                );
            }
        }

        List<String> totalReduceResult = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : sortedData.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            String reduceResult = reduce(key, value);
            totalReduceResult.add(reduceResult);
        }

        Files.write(Paths.get(FINAL_RESULT_PATH + REDUCE_FILE_NAME), totalReduceResult);
    }
}
