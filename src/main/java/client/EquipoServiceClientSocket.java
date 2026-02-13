package client;

import DTO.Equipos;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EquipoServiceClientSocket implements Closeable{
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;

    public EquipoServiceClientSocket(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    private String send(String msg) throws IOException {
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


    public List<Equipos> listar() throws IOException {
        String res = send("LIST");
        ensureOK(res);

        String payload = res.substring(3);
        payload = payload.startsWith("|") ? payload.substring(1) : payload;

        List<Equipos> list = new ArrayList<>();
        if (payload.isBlank()) return list;

        String[] rows = payload.split("\\|\\|");
        for (String row : rows) list.add(parse(row));
        return list;
    }

    public void actualizarEstadisticas(Equipos e) throws IOException {
        String req = "UPDATE_STATS|" + e.getIdEquipo() + "|" +
                e.getPartidosJugados() + "|" + e.getPartidosGanados() + "|" +
                e.getPartidosPerdidos() + "|" + e.getPartidosEmpatados() + "|" +
                e.getGolesFavor() + "|" + e.getGolesContra() + "|" + e.getPuntos();
        String res = send(req);
        ensureOK(res);
    }

    public int crear(Equipos e) throws IOException {
        String req = "CREATE|" + e.getNombre() + "|" + e.getCiudad() + "|" + e.getEstadio() + "|" + e.getAnioFundacion();
        String res = send(req);
        ensureOK(res);

        String payload = res.substring(3);
        payload = payload.startsWith("|") ? payload.substring(1) : payload;
        return Integer.parseInt(payload.replace("ID=", "").trim());
    }

    public void actualizar(Equipos e) throws IOException {
        String req = "UPDATE|" + e.getIdEquipo() + "|" + e.getNombre() + "|" + e.getCiudad() + "|" +
                e.getEstadio() + "|" + e.getAnioFundacion();
        String res = send(req);
        ensureOK(res);
    }

    public void eliminar(int id) throws IOException {
        String res = send("DELETE|" + id);
        ensureOK(res);
    }

    private Equipos parse(String row) {
        // id;nombre;ciudad;estadio;anio
        String[] c = row.split(";", -1);
        Equipos e = new Equipos();
        e.setIdEquipo(Integer.parseInt(c[0]));
        e.setNombre(c[1]);
        e.setCiudad(c[2]);
        e.setEstadio(c[3]);
        e.setAnioFundacion(Integer.parseInt(c[4]));
        return e;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}

