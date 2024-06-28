import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import src.ExampleClass;
import src.architecture.IgnoreField;
import src.exceptions.NullOrEmptyListException;
import src.service.impl.CsvWriterImpl;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
public class CsvWriterTest {

    private CsvWriterImpl<ExampleClass> csvWriter;

    private static final String FILE_NAME = "output.csv";
    private static final String THROWABLE_MESSAGE = "The object list empty or null";

    @BeforeEach
    public void setUp() {
        csvWriter = new CsvWriterImpl<>();
    }

    @Test
    public void testFileWriterWithNullListAsParam_shouldThrowIllegalArgument() {
        List<ExampleClass> nullList = null;
        NullOrEmptyListException expectedException = assertThrows(
                NullOrEmptyListException.class,
                () -> csvWriter.writeToFile(nullList, FILE_NAME)
        );

        assertEquals(THROWABLE_MESSAGE, expectedException.getMessage());
    }

    @Test
    public void testFileWriterWithEmptyListAsParam_shouldThrowIllegalArgument() {
        List<ExampleClass> objects = new ArrayList<>();

        NullOrEmptyListException expectedMessage = assertThrows(
                NullOrEmptyListException.class,
                () -> csvWriter.writeToFile(objects, FILE_NAME)
        );

        assertEquals(THROWABLE_MESSAGE, expectedMessage.getMessage());
    }

    @Test
    public void testFileWriterWithValidEntityList_shouldReturnCsvFIle() throws Exception {
        List<ExampleClass> businessObjects = Arrays.asList(
                new ExampleClass("testName1", 20),
                new ExampleClass("testName2", 30)
        );

        Path tempFile = Files.createTempFile("output", ".csv");

        boolean isPresent = isCustomAnnotationPresent(businessObjects);
        if (isPresent) {
            csvWriter.writeToFile(businessObjects, tempFile.toString());
            List<String> expectedLines = Files.readAllLines(tempFile);

            assertEquals(2, expectedLines.size());
            assertEquals("testName1,", expectedLines.get(0));
            assertEquals("testName2,", expectedLines.get(1));
        } else {
            csvWriter.writeToFile(businessObjects, tempFile.toString());
            List<String> expectedLines = Files.readAllLines(tempFile);

            assertEquals(2, expectedLines.size());
            assertEquals("testName1,20", expectedLines.get(0));
            assertEquals("testName2,30", expectedLines.get(1));
        }

        Files.delete(tempFile);
    }

    private boolean isCustomAnnotationPresent(List<ExampleClass> businessObjects) {
        boolean isPresent = false;
        Field[] fields = businessObjects.get(0).getClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(IgnoreField.class)) {
                isPresent = true;
            }
        }
        return isPresent;
    }






}
