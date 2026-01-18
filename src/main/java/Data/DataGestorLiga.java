package Data;

import DTO.Equipos;
import DTO.LigaDTO;
import DTO.PartidosDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.concurrent.atomic.AtomicInteger;

public class DataGestorLiga {
    private static DataGestorLiga instance;

    private final AtomicInteger idcounter = new AtomicInteger(1);
    private ObservableList <LigaDTO> ligas;
    private ObservableList<Equipos> todoslosequipos;
    private ObservableList<LigaDTO> todoslospartidos;


    private FilteredList<LigaDTO>ligasfiltradas;
    private final FilteredList<Equipos> equiposFiltrados;


    public DataGestorLiga(){
        todoslosequipos = FXCollections.observableArrayList();
        todoslospartidos = FXCollections.observableArrayList();
        ligas = javafx.collections.FXCollections.observableArrayList();
        ligasfiltradas = new FilteredList<>(ligas);
        equiposFiltrados = new FilteredList<>(todoslosequipos);

    }
    public static DataGestorLiga getInstance() {
        if (instance == null) {
            instance = new DataGestorLiga();
        }
        return instance;
    }
    public ObservableList<LigaDTO> getLigas(){
        return ligas;
    }
    public FilteredList<LigaDTO> getLigasfiltradas() {
        return ligasfiltradas;
    }
    public void agregarLiga(String nombre, String region){
        String id = String.valueOf(idcounter.getAndIncrement()); // consultar esto con el profe
        LigaDTO liga = new LigaDTO(id, nombre, region);
        ligas.add(liga);
    }
    public ObservableList<Equipos> getEquiposDeLiga(LigaDTO liga){
        return liga.getequipos();

    }
    public void agregarEquipoALiga(LigaDTO liga, DTO.Equipos equipo){
        liga.getequipos().add(equipo);
        todoslosequipos.add(equipo);

    }
    public ObservableList<Equipos> getTodosLosEquipos() {
        return todoslosequipos;
    }

    public FilteredList<Equipos> getEquiposFiltrados() {
        return equiposFiltrados;
    }

    public ObservableList<Equipos> getEquiposPorLiga(String nombreLiga) {
        for (LigaDTO liga : ligas) {
            if (liga.getNombre().get().equals(nombreLiga)) {
                return FXCollections.observableArrayList(liga.getEquipos());
            }
        }
        return FXCollections.observableArrayList();
    }
    public ObservableList<PartidosDTO> getTodosLosPartidos() {
        ObservableList<PartidosDTO> todosLosPartidos = FXCollections.observableArrayList();
        for (LigaDTO liga : ligas) {
            todosLosPartidos.addAll(liga.getpartidos());
        }
        return todosLosPartidos;
    }
    public ObservableList<PartidosDTO> getPartidosPorLiga(String nombreLiga) {
        for (LigaDTO liga : ligas) {
            if (liga.nombreLigaProperty().get().equals(nombreLiga)) {
                return liga.getpartidos();
            }
        }
        return FXCollections.observableArrayList();
    }



}
