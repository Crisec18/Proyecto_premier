package Logic;

import DTO.Equipos;
import DTO.PartidosDTO;
import Data.DataEquipos;
import Data.DataPartidos;
import client.LigaServiceClientSocket;
import client.PartidosServiceClientSocket;

import java.util.List;

public class LogicPartidos {
    private final String host = "127.0.0.1";
    private final int port = 5050;
    private final DataPartidos datos;

    public LogicPartidos(DataPartidos datos) {
        this.datos = datos;
    }

    public List<PartidosDTO> cargarpartidos() throws Exception {
        try (var service = new PartidosServiceClientSocket(host, port)) {
            return service.listarpartidos();
        }
    }

    public int guardarPartido(PartidosDTO partido, int ligaid) throws Exception {
        try (var service = new PartidosServiceClientSocket(host, port)) {
            return service.crearPartido(partido, ligaid);
        }
    }
    public void actualizarPartido(PartidosDTO partido) throws Exception {
        try (var service = new PartidosServiceClientSocket(host, port)) {
             service.actualizarPartido(partido);
        }
    }



}
