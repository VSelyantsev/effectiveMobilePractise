package src;

public class Main {
    public static void main(String[] args)   {
        Printer printer = new Printer();

        Runnable printEven = () -> {
            for (int i = 0; i < 100; i++) {
                printer.printEven(i);
            }
        };

        Runnable printOdd = () -> {
            for (int i = 1; i < 100; i++) {
                printer.printOdd(i);
            }
        };

        Thread evenThread = new Thread(printEven, "Thread-1");
        Thread oddThread = new Thread(printOdd, "Thread-2");

        evenThread.start();
        oddThread.start();
    }
}
