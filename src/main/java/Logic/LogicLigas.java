package Logic;

import DTO.Equipos;
import DTO.LigaDTO;
import Data.DataEquipos;
import Data.DataGestorLiga;
import client.EquipoServiceClientSocket;
import client.LigaServiceClientSocket;

import java.util.List;

public class LogicLigas {
    private final String host = "127.0.0.1";
    private final int port = 5050;
    private final DataGestorLiga datos;

    public LogicLigas(DataGestorLiga datos) {
        this.datos = datos;
    }

    public List<LigaDTO> cargarligas() throws Exception {
        try (var service = new LigaServiceClientSocket(host, port)) {
            return service.listarligas();
        }
    }

    public int guardarLiga(LigaDTO liga) throws Exception {
        try (var service = new LigaServiceClientSocket(host, port)) {
             return service.crearliga(liga);
        }
    }

}
