import exceptions.ClientReadWriteException;
import exceptions.ClientSocketCloseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandlerRequest implements Runnable {

    private final Socket clientSocket;

    private static final String EXIT_COMMAND = "exit";
    private static final Logger logger = Logger.getLogger(ClientHandlerRequest.class.getName());

    public ClientHandlerRequest(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (EXIT_COMMAND.equals(inputLine)) {
                    break;
                }

                System.out.println("Received: " + inputLine);
                out.println("Echo: " + inputLine);
            }
        } catch (IOException e) {
            logger.log(
                    Level.SEVERE,
                    "Client read/write exception: ",
                    new ClientReadWriteException(e.getMessage(), e.getCause())
            );
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.log(
                        Level.SEVERE,
                        "Problem with closing a resource",
                        new ClientSocketCloseException(e.getMessage())
                );
            }
        }
    }
}
