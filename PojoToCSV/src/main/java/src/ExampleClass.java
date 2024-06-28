package src;


import src.architecture.IgnoreField;

public class ExampleClass {

    private String name;

    @IgnoreField
    private int age;

    public ExampleClass(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
