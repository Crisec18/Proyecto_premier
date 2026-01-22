package DTO;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.LinkedList;
import java.util.Map;

public class LigaDTO {
    StringProperty id;
    StringProperty nombre;
    StringProperty region;
    ObservableList<Equipos> equipos;
    ObservableList<PartidosDTO> partidos;
    ObservableList<String> jornada;
    int ligatamano;
    //linkedlist de partidos
    public LigaDTO(String id, String nombre, String region){
        this.id = new SimpleStringProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.region = new SimpleStringProperty(region);
        equipos = FXCollections.observableArrayList();
        partidos = FXCollections.observableArrayList();
        jornada = FXCollections.observableArrayList();

    }
    public ObservableList<Equipos> getequipos(){
        return equipos;
    }
    public ObservableList<PartidosDTO> getpartidos(){return partidos;}
    public ObservableList<String> getjornadas(){return jornada;}

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

    public StringProperty idpartidoProperty() {
        return id;
    }

    public StringProperty getNombre() {
        return nombre;
    }

    public ObservableList<Equipos> getEquipos() {
        return equipos;
    }

    public StringProperty regionligaproperty() {
        return region;
    }
}
