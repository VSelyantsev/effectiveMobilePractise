public class Printer {
    private final Object lock = new Object();
    private boolean evenTurn = true;

    public void printEven(int number) {
        synchronized (lock) {
            if (!evenTurn || number % 2 != 0) {
                try {
                    lock.wait();
                    return;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("Current number " + number + " Executed by " + Thread.currentThread().getName());
            evenTurn = !evenTurn;
            lock.notifyAll();
        }
    }

    public void printOdd(int number) {
        synchronized (lock) {
            if (evenTurn || number % 2 == 0) {
                try {
                    lock.wait();
                    return;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("Current number " + number + " Executed by " + Thread.currentThread().getName());
            evenTurn = !evenTurn;
            lock.notifyAll();
        }
    }
}

