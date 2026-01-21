package Logic;

import DTO.Equipos;
import DTO.PartidosDTO;
import Data.DataEquipos;
import Data.DataPartidos;

import java.util.List;

public class LogicPartidos {
    private final DataPartidos datos;

    public LogicPartidos(DataPartidos datos) {
        this.datos = datos;
    }

    public List<PartidosDTO> cargarpartidos() throws Exception {
        return datos.cargar();
    }

    public void guardar(List<PartidosDTO> equipos) throws Exception {
        datos.guardar(equipos);
    }

    public int obtenerSiguienteId(List<PartidosDTO> partidos) {
        int max = 0;
        for (PartidosDTO e : partidos) {
            int idActual = Integer.parseInt(e.getIdpartido().getValue());
            if (idActual > max) {
                max = idActual;
            }
        }
        return max + 1;
    }

}
