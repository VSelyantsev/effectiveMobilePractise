package src;


import src.service.CsvWriter;
import src.service.impl.CsvWriterImpl;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        ExampleClass exampleClass = new ExampleClass("123", 20);
        ExampleClass exampleClass1 = new ExampleClass("231", 25);

        List<ExampleClass> exampleClasses = new ArrayList<>();
        exampleClasses.add(exampleClass);
        exampleClasses.add(exampleClass1);

        CsvWriter<ExampleClass> writer = new CsvWriterImpl<>();
        writer.writeToFile(exampleClasses, "output.csv");


    }
}
