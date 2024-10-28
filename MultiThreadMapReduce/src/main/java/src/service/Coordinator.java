package src.service;

import src.model.TaskRequest;
import src.service.runnable.MapWorker;
import src.service.runnable.ReduceWorker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static src.util.FileUtils.searchFiles;
import static src.util.FileUtils.sortedFilesBySuffix;

public class Coordinator {

    private List<File> files;
    private int worker;

    private static final Logger logger = Logger.getLogger(Coordinator.class.getName());

    public Coordinator(List<File> files, int worker) {
        this.files = files;
        this.worker = worker;
    }

    public List<TaskRequest> mapToWorkerRequest(List<File> files, int taskReduce) {
        return files.stream()
                .map(file -> new TaskRequest(
                        UUID.randomUUID(),
                        file.getName(),
                        file,
                        taskReduce
                )).collect(Collectors.toList());
    }

    public void executeMap(List<TaskRequest> taskRequests) throws InterruptedException {
        int mapReduceTask = taskRequests.size();
        CountDownLatch latch = new CountDownLatch(mapReduceTask);
        List<Thread> mapHolder = new ArrayList<>();

        for (TaskRequest taskRequest : taskRequests) {
            Thread intermediateTask = new Thread(new MapWorker(taskRequest, latch));
            mapHolder.add(intermediateTask);
            intermediateTask.start();
            logger.log(Level.INFO, String.format("MapTask begin executed: %s", taskRequest.getId()));
        }

        latch.await();

        for (Thread thread : mapHolder) {
            thread.join();
        }
    }

    public void executeReduce() throws IOException, InterruptedException {
        List<File> intermediateFiles = searchFiles(Paths.get("src/main/resources/files/processed"), "mr-");

        Map<String, List<File>> fileMap = sortedFilesBySuffix(intermediateFiles);

        List<Thread> threadHolder = new ArrayList<>();

        int latch = fileMap.keySet().size();
        CountDownLatch reduceLatch = new CountDownLatch(latch);

        for (Map.Entry<String, List<File>> entry : fileMap.entrySet()) {
            Thread finalReduceTask = new Thread(new ReduceWorker(Integer.parseInt(entry.getKey()), entry.getValue(), reduceLatch));
            finalReduceTask.start();
            threadHolder.add(finalReduceTask);
            logger.log(Level.INFO, "ReduceTask begin executed");
        }

        reduceLatch.await();

        for (Thread thread : threadHolder) {
            thread.join();
        }
    }
}
