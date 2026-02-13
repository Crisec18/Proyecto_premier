package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Serverpremier {
    private static final int PORT = 5050;
    private static final ExecutorService POOL = Executors.newFixedThreadPool(30);

    public static void main(String[] args) {
        System.out.println("Servidor PremierLeague TCP en puerto " + PORT);

        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                Socket client = server.accept();
                POOL.submit(new ClientHandler(client));
            }
        } catch (Exception e) {
            System.err.println("Servidor detenido: " + e.getMessage());
        } finally {
            POOL.shutdown();
        }
    }
}
