package Data;

import DTO.Equipos;
import DTO.LigaDTO;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.concurrent.atomic.AtomicInteger;

public class Datasingleton {
    private final AtomicInteger idcounter = new AtomicInteger(1);
 private static Datasingleton instance ;
 //Dtos de equipos crwacion y modificacion
 private ObservableList<Equipos> equipos;
 private FilteredList<Equipos> Equiposfiltrados;

 //Dto de creacion de liga
    private ObservableList<LigaDTO> ligas;

 private Datasingleton(){
     ligas = FXCollections.observableArrayList();
     equipos = FXCollections.observableArrayList();
     Equiposfiltrados = new FilteredList<>(equipos);

 }
 public static Datasingleton getInstance(){
     if (instance == null){
         instance = new Datasingleton();
     }
     return instance;
 }
 public ObservableList<Equipos> getEquipos(){
     return equipos;
 }

}