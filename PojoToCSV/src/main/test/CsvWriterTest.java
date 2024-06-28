import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import src.exceptions.ClassFormatException;
import src.model.ExampleClass;
import src.exceptions.NullOrEmptyListException;
import src.model.ExampleObject2;
import src.model.ExampleObject3;
import src.service.ObjectWriter;
import src.service.impl.CsvWriterImpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
public class CsvWriterTest {

    private static final List<ExampleClass> LIST_WITH_ANNOTATED_FIELD = Arrays.asList(
            new ExampleClass("testName1", 20),
            new ExampleClass("testName2", 30)
    );

    private static final List<ExampleObject2> LIST_WITHOUT_ANNOTATED_FIELD = Arrays.asList(
            new ExampleObject2("firstName1", "lastName1", "sureName1"),
            new ExampleObject2("firstName2", "lastName2", "sureName2")
    );

    private static final List<ExampleObject3> LIST_WITH_ALL_ANNOTATED_FIELD = Arrays.asList(
            new ExampleObject3(1,2,3),
            new ExampleObject3(11, 22, 33)
    );

    private static final String PREFIX = "output";
    private static final String SUFFIX = ".csv";

    private static final String FILE_NAME = "output.csv";
    private static final String THROWABLE_LIST_MESSAGE = "The object list empty or null";
    private static final String THROWABLE_CLASS_FORMAT_MESSAGE = "All fields are Annotated";

    @Test
    public void testFileWriterWithNullListAsParam_shouldThrowIllegalArgument() {
        ObjectWriter<ExampleClass> csvWriter = new CsvWriterImpl<>();
        List<ExampleClass> nullList = null;

        NullOrEmptyListException expectedException = assertThrows(
                NullOrEmptyListException.class,
                () -> csvWriter.writeToFile(nullList, FILE_NAME)
        );

        assertEquals(THROWABLE_LIST_MESSAGE, expectedException.getMessage());
    }

    @Test
    public void testFileWriterWithEmptyListAsParam_shouldThrowIllegalArgument() {
        ObjectWriter<ExampleClass> csvWriter = new CsvWriterImpl<>();
        List<ExampleClass> objects = new ArrayList<>();

        NullOrEmptyListException expectedMessage = assertThrows(
                NullOrEmptyListException.class,
                () -> csvWriter.writeToFile(objects, FILE_NAME)
        );

        assertEquals(THROWABLE_LIST_MESSAGE, expectedMessage.getMessage());
    }

    @Test
    public void testFileWriterWithValidAnnotatedFields() throws Exception {
        ObjectWriter<ExampleClass> csvWriter = new CsvWriterImpl<>();
        Path tempFile = Files.createTempFile(PREFIX, SUFFIX);

        csvWriter.writeToFile(LIST_WITH_ANNOTATED_FIELD, tempFile.toString());
        List<String> expectedLines = Files.readAllLines(tempFile);

        assertEquals(2, expectedLines.size());
        assertEquals("testName1,", expectedLines.get(0));
        assertEquals("testName2,", expectedLines.get(1));

        Files.delete(tempFile);
    }

    @Test
    public void testFileWriterWithValidList() throws Exception {
        ObjectWriter<ExampleObject2> csvWriter = new CsvWriterImpl<>();
        Path tempFile = Files.createTempFile(PREFIX, SUFFIX);

        csvWriter.writeToFile(LIST_WITHOUT_ANNOTATED_FIELD, tempFile.toString());
        List<String> expectedList = Files.readAllLines(tempFile);

        assertEquals(2, expectedList.size());
        assertEquals("firstName1,lastName1,sureName1", expectedList.get(0));
        assertEquals("firstName2,lastName2,sureName2", expectedList.get(1));

        Files.delete(tempFile);
    }

    // пройтись ридером и посмотреть пустой ли файл
    // если пустой то експешн

    @Test
    public void testFileWriterWithAllAnnotatedFields() throws Exception {
        ObjectWriter<ExampleObject3> csvWriter = new CsvWriterImpl<>();
        Path tempFile = Files.createTempFile(PREFIX, SUFFIX);

        ClassFormatException expectedMessage = assertThrows(
                ClassFormatException.class,
                () -> csvWriter.writeToFile(LIST_WITH_ALL_ANNOTATED_FIELD, tempFile.toString())
        );

        assertEquals(THROWABLE_CLASS_FORMAT_MESSAGE, expectedMessage.getMessage());

        Files.delete(tempFile);
    }
}
