package client;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ClientSimulatorMain {
    public static void main(String[] args) {
        // Si se pasa --simulate, ejecutamos la simulación desde este main (modo CLI).
        // Si no, delegamos al Main que lanza la interfaz gráfica.

        boolean simulateOnly = false;
        String host = "localhost";
        int port = 5050;
        int timeoutSeconds = 60;

        for (String a : args) {
            if (a.equalsIgnoreCase("--simulate")) simulateOnly = true;
        }

        // Leer parámetros (más simples): host port timeout si están en la forma --host=... --port=... --timeout=...
        for (String a : args) {
            if (a.startsWith("--host=")) host = a.substring("--host=".length());
            if (a.startsWith("--port=")) {
                try { port = Integer.parseInt(a.substring("--port=".length())); } catch (NumberFormatException ignored) {}
            }
            if (a.startsWith("--timeout=")) {
                try { timeoutSeconds = Integer.parseInt(a.substring("--timeout=".length())); } catch (NumberFormatException ignored) {}
            }
        }

        if (simulateOnly) {
            System.out.println("Cliente simulador (modo CLI): conectando a " + host + ":" + port);
            try (PartidosServiceClientSocket client = new PartidosServiceClientSocket(host, port)) {
                System.out.println("Enviando petición para simular todos los partidos pendientes...");
                int simulados = client.simularTodosSync(timeoutSeconds);
                System.out.println("Simulación finalizada. Partidos simulados: " + simulados);
            } catch (IOException e) {
                System.err.println("Error I/O: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error durante la simulación: " + e.getMessage());
                e.printStackTrace();
                System.exit(2);
            }
            return;
        }

        // Por defecto lanzamos la GUI (delegar a la clase Main de la aplicación JavaFX)
        System.out.println("Arrancando interfaz gráfica (delegando a Main)...");
        practice_fx.proyecto_premier.Main.main(args);
    }
}
