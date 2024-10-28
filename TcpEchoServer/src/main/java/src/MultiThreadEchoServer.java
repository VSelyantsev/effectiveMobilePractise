package src;

import src.exceptions.ClientSocketCloseException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiThreadEchoServer {
    private static final int PORT = 7;
    private static final int THREAD_POOL_SIZE = 10;

    private static final Logger logger = Logger.getLogger(MultiThreadEchoServer.class.getName());

    public void createConnection() {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Listening on port: " + PORT);

            while (true) {
                Socket clientSocket = server.accept();
                System.out.println("New Client connected");
                threadPool.execute(new ClientHandlerRequest(clientSocket));
            }
        } catch (Exception e) {
            logger.log(
                    Level.SEVERE,
                    "Problem occurring with Socket: ",
                    new ClientSocketCloseException(e.getMessage())
            );
        }
    }

    public static void main(String[] args) {
        MultiThreadEchoServer server = new MultiThreadEchoServer();
        server.createConnection();
    }
}
