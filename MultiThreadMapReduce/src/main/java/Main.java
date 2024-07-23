import model.TaskRequest;
import service.Coordinator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static util.FileUtils.searchFiles;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        List<File> beginFiles = searchFiles(Paths.get("src/main/resources/files"), ".txt");
        Coordinator coordinator = new Coordinator(beginFiles, 2);

        List<TaskRequest> taskRequests = coordinator.mapToWorkerRequest(beginFiles, 2);
        coordinator.executeMap(taskRequests);
        coordinator.executeReduce();
    }
}
