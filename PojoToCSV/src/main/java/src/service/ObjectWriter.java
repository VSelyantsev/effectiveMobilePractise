package src.service;

import src.exceptions.AccessException;

import java.util.List;

public interface ObjectWriter<T> {
    void writeToFile(List<T> businessObjects, String fileName) throws AccessException;
}
