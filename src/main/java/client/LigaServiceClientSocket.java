package client;

import DTO.Equipos;
import DTO.LigaDTO;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LigaServiceClientSocket implements Closeable {
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;

    public LigaServiceClientSocket(String host, int port) throws IOException {
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


    public List<LigaDTO> listarligas() throws IOException {
        String res = send("LISTLIGA");
        ensureOK(res);

        String payload = res.substring(3);
        payload = payload.startsWith("|") ? payload.substring(1) : payload;

        List<LigaDTO> list = new ArrayList<>();
        if (payload.isBlank()) return list;

        String[] rows = payload.split("\\|\\|");
        for (String row : rows) list.add(parse(row));
        return list;
    }


    public int crearliga(LigaDTO liga) throws IOException {
        String req = "CREATELIGA|" + liga.getNombre().get() + "|" + liga.getRegion();
        String res = send(req);
        ensureOK(res);

        String payload = res.substring(3);
        payload = payload.startsWith("|") ? payload.substring(1) : payload;
        return Integer.parseInt(payload.replace("ID=", "").trim());
    }


    private LigaDTO parse(String row) {
        String[] c = row.split(";", -1);
        LigaDTO e = new LigaDTO();
        e.SetidLiga(Integer.parseInt(c[0]));
        e.setNombre(c[1]);
        e.setregion(c[2]);
        return e;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
