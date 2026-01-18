package Logic;
import DTO.Equipos;
import Data.DataEquipos;
import java.util.List;

public class LogicaEquipo {
    private final DataEquipos datos;

    public LogicaEquipo(DataEquipos datos) {
        this.datos = datos;
    }

    public List<Equipos> cargarEquipos() throws Exception {
        return datos.cargar();
    }

    public void guardar(List<Equipos> equipos) throws Exception {
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
