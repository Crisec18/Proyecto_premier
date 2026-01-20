package DTO;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.LinkedList;

public class LigaDTO {
    StringProperty id;
    StringProperty nombre;
    StringProperty region;
    ObservableList<Equipos> equipos;
    ObservableList<PartidosDTO> partidos;
    int ligatamano;
    //linkedlist de partidos
    public LigaDTO(String id, String nombre, String region){
        this.id = new SimpleStringProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.region = new SimpleStringProperty(region);
        equipos = FXCollections.observableArrayList();
        partidos = FXCollections.observableArrayList();

    }
    public ObservableList<Equipos> getequipos(){
        return equipos;
    }
    public ObservableList<PartidosDTO> getpartidos(){return partidos;}

    public StringProperty idLigaProperty(){
        return id;
    }
    public StringProperty nombreLigaProperty(){
        return nombre;
    }
    public StringProperty regionLigaProperty(){
        return region;
    }
    public StringProperty nombreligaproperty(){
        return nombre;
    }
    public void aumentarcapacidadliga(){
        ligatamano++;
    }
    public int getLigatamano(){
        return ligatamano;
    }


    public StringProperty getNombre() {
        return nombre;
    }

    public ObservableList<Equipos> getEquipos() {
        return equipos;
    }
}
