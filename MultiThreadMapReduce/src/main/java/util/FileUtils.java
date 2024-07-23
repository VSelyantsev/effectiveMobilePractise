package util;

import exceptions.IncorrectFileFormatException;
import exceptions.NotExistDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtils {
    private FileUtils() { }

    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());

    public static List<File> searchFiles(Path searchDirectory, String pattern) throws IOException {
        DirectoryStream.Filter<Path> filter = Files::isRegularFile;

        List<File> resultList = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(searchDirectory, filter)) {
            for (Path entry : stream) {
                File file = entry.toFile();
                if (file.getName().contains(pattern)) {
                    resultList.add(file);
                    logger.log(Level.INFO, String.format("Founded file: %s", file.getName()));
                }
            }
        } catch (IOException e) {
            logger.log(
                    Level.SEVERE,
                    "Problem with PATH:",
                    new NotExistDirectory(e.getMessage())
            );
        }
        return resultList;
    }

    public static Map<String, List<File>> sortedFilesBySuffix(List<File> files) {
        Map<String, List<File>> sortedFiles = new HashMap<>();

        for (File file : files) {
            String fileName = file.getName();
            String[] parts = fileName.split("-");

            if (!(parts.length == 7)) {
                logger.log(
                        Level.SEVERE,
                        "Problem with File Name:",
                        new IncorrectFileFormatException(fileName)
                );
            }

            sortedFiles.computeIfAbsent(parts[6], key -> new ArrayList<>()).add(file);
        }
        return sortedFiles;
    }
}
