package client;

import DTO.Equipos;
import DTO.PartidosDTO;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PartidosServiceClientSocket implements Closeable {
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;

    public PartidosServiceClientSocket(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    // Hacer synchronized para prevenir interleaving de requests/responses cuando hay polling
    private synchronized String send(String msg) throws IOException {
        out.write(msg);
        out.write("\n");
        out.flush();
        return in.readLine();
    }

    private void ensureOK(String res) {
        if (res == null) throw new RuntimeException("Sin respuesta del servidor.");
        if (res.startsWith("ERR|")) throw new RuntimeException(res.substring(4));
        if (!res.startsWith("OK|")) throw new RuntimeException("Respuesta inválida: " + res);
    }


    public List<PartidosDTO> listarpartidos() throws IOException {
        String res = send("LISTMATCHES");
        ensureOK(res);

        String payload = res.substring(3);
        payload = payload.startsWith("|") ? payload.substring(1) : payload;

        List<PartidosDTO> list = new ArrayList<>();
        if (payload.isBlank()) return list;

        String[] rows = payload.split("\\|\\|");
        for (String row : rows) list.add(parse(row));
        return list;
    }

    public int crearPartido(PartidosDTO partido, int idLiga) throws IOException {
        String req = "CREATEPARTIDO|" +
                partido.getlocal().getIdEquipo() + "|" +
                partido.getvisitante().getIdEquipo() + "|" +
                partido.jornadasProperty().get() + "|" +
                partido.estadioProperty().get() + "|" +
                partido.getfecha() + "|" +
                partido.estadoProperty().get() + "|" +
                partido.golesLocalProperty().get() + "|" +
                partido.golesVisitanteProperty().get() + "|" +
                idLiga;
        String res = send(req);
        ensureOK(res);
        String payload = res.substring(3);
        payload = payload.startsWith("|") ? payload.substring(1) : payload;
        return Integer.parseInt(payload.replace("ID=", "").trim());
    }
    public void actualizarPartido(PartidosDTO partido) throws IOException {
        String req = "UPDATEPARTIDO|" +
                partido.idpartidoProperty().get() + "|" +
                partido.estadoProperty().get() + "|" +
                partido.golesLocalProperty().get() + "|" +
                partido.golesVisitanteProperty().get();

        String res = send(req);
        ensureOK(res);
    }


    private PartidosDTO parse(String row) {
        // id;fecha;idLocal;nombreLocal;idVisitante;nombreVisitante;jornada;estadio;estado;golesLocal;golesVisitante;idLiga
        String[] c = row.split(";", -1);

        PartidosDTO p = new PartidosDTO();
        p.setIdPartido(Integer.parseInt(c[0]));
        p.setFecha(LocalDate.parse(c[1]));

        // Crear equipos con ID Y NOMBRE
        Equipos local = new Equipos();
        local.setIdEquipo(Integer.parseInt(c[2]));
        local.setNombre(c[3]);

        Equipos visitante = new Equipos();
        visitante.setIdEquipo(Integer.parseInt(c[4]));
        visitante.setNombre(c[5]);

        p.setLocal(local);
        p.setVisitante(visitante);
        p.setJornada(c[6]);
        p.setEstadio(c[7]);
        p.setEstado(c[8]);
        p.setGolesLocal(Integer.parseInt(c[9]));
        p.setGolesVisitante(Integer.parseInt(c[10]));
        p.setliga(Integer.parseInt(c[11]));

        String nombre = local.getNombre() + " vs " + visitante.getNombre();
        p.setNombrePartido(nombre);

        return p;

    }

    // Variante asíncrona que simula todos los partidos pendientes y devuelve el conteo de partidos simulados
    public CompletableFuture<Integer> simularTodosAsync() throws IOException {
        return simularTodosAsync(60); // timeout por defecto 60s
    }

    public CompletableFuture<Integer> simularTodosAsync(int timeoutSeconds) throws IOException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        // Ejecutar la solicitud síncrona en un hilo de fondo para no bloquear el llamador
        Thread t = new Thread(() -> {
            try {
                String req = "SIMULAR_TODOS_SYNC|" + timeoutSeconds;
                String res = send(req);
                if (res == null) {
                    future.completeExceptionally(new IOException("Sin respuesta del servidor al iniciar simulación de todos."));
                    return;
                }
                if (res.startsWith("OK|RESULT|")) {
                    String num = res.substring("OK|RESULT|".length()).trim();
                    try {
                        int n = Integer.parseInt(num);
                        future.complete(n);
                        return;
                    } catch (NumberFormatException ex) {
                        future.completeExceptionally(ex);
                        return;
                    }
                }
                if (res.startsWith("OK|TASK|")) {
                    // servidor devolvió taskId (no es esperado en la variante sync), intentar parsear como pendiente
                    String taskId = res.substring("OK|TASK|".length());
                    future.completeExceptionally(new RuntimeException("Servidor devolvió task (esperado RESULT): " + taskId));
                    return;
                }
                if (res.startsWith("ERR|")) {
                    future.completeExceptionally(new RuntimeException(res.substring(4)));
                    return;
                }
                future.completeExceptionally(new RuntimeException("Respuesta inesperada: " + res));
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });
        t.setDaemon(true);
        t.start();
        return future;
    }

    public int simularTodosSync(int timeoutSeconds) throws IOException, InterruptedException, ExecutionException {
        CompletableFuture<Integer> future = simularTodosAsync(timeoutSeconds);
        try {
            return future.get(timeoutSeconds + 5, TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException te) {
            throw new IOException("Timeout al esperar resultado de simulación de todos", te);
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

}
