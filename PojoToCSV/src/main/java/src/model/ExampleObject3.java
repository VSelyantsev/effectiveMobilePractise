package src.model;

import src.architecture.IgnoreField;

public class ExampleObject3 {

    @IgnoreField
    private int numberOne;

    @IgnoreField
    private int numberTwo;

    @IgnoreField
    private int numberThree;

    public ExampleObject3(int numberOne, int numberTwo, int numberThree) {
        this.numberOne = numberOne;
        this.numberTwo = numberTwo;
        this.numberThree = numberThree;
    }
}
