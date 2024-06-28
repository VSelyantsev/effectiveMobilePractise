package src.service.impl;

import src.architecture.IgnoreField;
import src.exceptions.AccessException;
import src.exceptions.ClassFormatException;
import src.exceptions.NullOrEmptyListException;
import src.service.ObjectWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CsvWriterImpl<T> implements ObjectWriter<T> {

    private static final Logger logger = Logger.getLogger(CsvWriterImpl.class.getName());

    @Override
    public void writeToFile(List<T> businessObjects, String fileName) throws AccessException {
        if (Objects.isNull(businessObjects) || businessObjects.isEmpty()) {
            throw new NullOrEmptyListException("The object list empty or null");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            Class<?> clazz = businessObjects.get(0).getClass();

            if (isAllFieldsAnnotated(clazz)) {
                logger.log(Level.INFO, "Error while trying to check class fields.");
                throw new ClassFormatException("All fields are Annotated");
            }

            Field[] fields = clazz.getDeclaredFields();

            for (T businessObject : businessObjects) {
                for (int i = 0; i < fields.length; i++) {
                    if (!fields[i].isAnnotationPresent(IgnoreField.class)) {
                        fields[i].setAccessible(true);
                        Object value = fields[i].get(businessObject);
                        writer.write(value.toString());
                        if (i < fields.length - 1) {
                            writer.write(",");
                        }
                    }
                }
                writer.newLine();
            }

        } catch (IllegalAccessException | IOException e) {
            logger.log(Level.INFO, "Got error while trying convert to csv");
            throw new AccessException(e.getMessage());
        }
    }

    private boolean isAllFieldsAnnotated(Class<?> clazz) {
        int counter = 0;
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(IgnoreField.class)) {
                counter++;
            }
        }

        return counter == fields.length;
    }
}
