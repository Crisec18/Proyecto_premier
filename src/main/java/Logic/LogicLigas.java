package Logic;

import DTO.Equipos;
import DTO.LigaDTO;
import Data.DataEquipos;
import Data.DataGestorLiga;

import java.util.List;

public class LogicLigas {
    private final DataGestorLiga datos;

    public LogicLigas(DataGestorLiga datos) {
        this.datos = datos;
    }

    public List<LigaDTO> cargarligas() throws Exception {
        return datos.cargar();
    }

    public void guardar(List<LigaDTO> equipos) throws Exception {
        datos.guardar(equipos);
    }

    public int obtenerSiguienteId(List<Equipos> equipos) {
        int max = 0;
        for (Equipos e : equipos) {
            if (e.getIdEquipo() > max) {;
                max = e.getIdEquipo();
            }
        }
        return max + 1;
    }
}
