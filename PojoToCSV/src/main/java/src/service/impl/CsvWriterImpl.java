package src.service.impl;

import src.service.CsvWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public class CsvWriterImpl<T> implements CsvWriter<T> {

    @Override
    public void writeToFile(List<T> businessObjects, String fileName) {
        if (Objects.isNull(businessObjects) || businessObjects.isEmpty()) {
            throw new IllegalArgumentException("The object list empty or null: " + businessObjects);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            Class<?> clazz = businessObjects.get(0).getClass();

            Field[] fields = clazz.getDeclaredFields();

            for (T businessObject : businessObjects) {
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    Object value = fields[i].get(businessObject);
                    writer.write(value.toString());
                    if (i < fields.length - 1) {
                        writer.write(",");
                    }
                }
                writer.newLine();
            }

        } catch (IllegalAccessException | IOException e) {
            e.printStackTrace();
        }
    }
}
