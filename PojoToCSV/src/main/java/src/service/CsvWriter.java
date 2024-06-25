package src.service;

import java.util.List;

public interface CsvWriter<T> {
    void writeToFile(List<T> businessObjects, String fileName);
}
