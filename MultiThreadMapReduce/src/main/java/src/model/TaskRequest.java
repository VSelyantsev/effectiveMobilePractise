package src.model;

import java.io.File;
import java.util.UUID;

public class TaskRequest {

    private UUID id;
    private String fileName;
    private File file;
    private int taskReduce;
    public TaskRequest() { }

    public TaskRequest(UUID id, String fileName, File file, int taskReduce) {
        this.id = id;
        this.fileName = fileName;
        this.file = file;
        this.taskReduce = taskReduce;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getTaskReduce() {
        return taskReduce;
    }

    public void setTaskReduce(int taskReduce) {
        this.taskReduce = taskReduce;
    }
}
