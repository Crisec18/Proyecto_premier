package Data;

import DTO.Equipos;
import DTO.LigaDTO;
import DTO.PartidosDTO;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

public class DataPartidos {
    private static DataPartidos instance;

    private final AtomicInteger idcounter = new AtomicInteger(1);
    private ObservableList<PartidosDTO> partidos;
    private FilteredList<PartidosDTO> Partidosfiltrados;

    public DataPartidos() {
        partidos = javafx.collections.FXCollections.observableArrayList();
        Partidosfiltrados = new FilteredList<>(partidos);
    }

    public static DataPartidos getInstance() {
        if (instance == null) {
            instance = new DataPartidos();
        }
        return instance;
    }

    public ObservableList<PartidosDTO> getPartidos() {
        return partidos;

    }

    public FilteredList<PartidosDTO> getPartidosfiltrados() {
        return Partidosfiltrados;
    }


    public void agregarPartido(String partidonombre, Equipos equipo1, Equipos equipo2, String jornada, String Estadio, LocalDate fecha) {
        String id = String.valueOf(idcounter.getAndIncrement());
        PartidosDTO partido = new PartidosDTO(partidonombre, equipo1, equipo2, jornada, id, fecha, Estadio);
        partidos.add(partido);

    }

    public PartidosDTO getPartidoPorNombre(String nombre) {
        return partidos.stream()
                .filter(p -> p.nombrepartidoProperty().get().equals(nombre))
                .findFirst()
                .orElse(null);
    }
}
