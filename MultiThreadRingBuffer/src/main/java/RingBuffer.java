public class RingBuffer {
    private final int[] buffer;
    private int head;
    private int tail;
    private int count;
    private final int size;

    public RingBuffer(int size) {
        this.size = size;
        this.buffer = new int[size];
        // index for next elem to put
        this.head = 0;
        // index for next elem to obtain
        this.tail = 0;
        this.count = 0;
    }

    public synchronized void write(int value) {
        if (count == size) {
            tail = (tail + 1) % size;
            count--;
        }

        buffer[head] = value;
        head = (head + 1) % size;
        count++;
        notifyAll();
    }

    public synchronized int read() throws InterruptedException {
        while (count == 0) {
            wait();
        }

        int actualValue = buffer[tail];
        tail = (tail + 1) % size;
        count--;
        return actualValue;
    }

    public static void main(String[] args) {
        final RingBuffer ringBuffer = new RingBuffer(5);

        Runnable readValues = () -> {
            for (int i = 0; i < 5; i++) {
                try {
                    int actualValue = ringBuffer.read();
                    System.out.println("Consumed: " + actualValue);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        Runnable writeValues = () -> {
            for (int i = 0; i < 5; i++) {
                ringBuffer.write(i);
            }
        };

        Thread readValueThread = new Thread(readValues);
        Thread writeThread = new Thread(writeValues);

        readValueThread.start();
        writeThread.start();
    }
}
