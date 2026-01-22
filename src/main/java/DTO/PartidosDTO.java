package DTO;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

import java.time.LocalDate;

public class PartidosDTO {
    StringProperty idpartido;
    StringProperty nombrepartido;
    LocalDate fecha;
    Equipos local;
    Equipos visitante;
    StringProperty jornadas;
    StringProperty Idpartido;
    StringProperty Estadio;
    StringProperty estado;
    StringProperty liga;
    public PartidosDTO(String Nombre,Equipos local, Equipos visitante, String jornadas, String idpartido1 , LocalDate fecha , String estadio){
        this.idpartido = new SimpleStringProperty(idpartido1);
        this.fecha = fecha;
        this.local = local;
        this.visitante = visitante;
        this.jornadas = new SimpleStringProperty(jornadas);
        this.Idpartido = new SimpleStringProperty(idpartido1);
        this.Estadio = new SimpleStringProperty(estadio);
        this.nombrepartido = new SimpleStringProperty(Nombre);
        this.estado = new SimpleStringProperty("Pendiente");
        this.liga = new SimpleStringProperty("");
    }
    public Equipos getlocal(){
        return local;
    }
    public Equipos getvisitante(){
        return visitante;
    }
    public LocalDate getfecha(){
        return fecha;
    }
    public StringProperty jornadasProperty(){
        return jornadas;
    }
    public StringProperty idpartidoProperty(){
        return Idpartido;
    }
    public StringProperty nombrepartidoProperty(){
        return nombrepartido;
    }
    public StringProperty estadioProperty(){
        return Estadio;
    }
    public StringProperty jornadaproperty(){
        return jornadas;
    }
    public void setestado(){
        estado = new SimpleStringProperty("Finalizado");
    }
    public StringProperty estadoProperty(){
        return estado;
    }
    public void setgoleslocal(int golesLocal, int golesVisitante){
        local.setGolesafavor(local.golesFavorProperty().get() + golesLocal);
        local.setGolesencontra(local.golesContraProperty().get() + golesVisitante);
        local.setPartidosganados();
        visitante.setPartidosperdidos();
    }
    public void setgolesvisitante(int golesVisitante, int golesLocal){
        visitante.setGolesafavor(visitante.golesFavorProperty().get() + golesVisitante);
        visitante.setGolesencontra(visitante.golesContraProperty().get() + golesLocal);
        visitante.setPartidosganados();
        local.setPartidosperdidos();
    }
    public void setliga(String liga1){
        liga = new SimpleStringProperty(liga1);
    }


    public IntegerProperty getgoleslocal() {
        return local.golesFavorProperty();
    }

    public IntegerProperty getgolesvisitante() {
        return visitante.golesFavorProperty();
    }

   public StringProperty getIdpartido() {
        return Idpartido;
    }
    public StringProperty getNombrepartido() {
        return nombrepartido;
    }
    public StringProperty getEstadio() {
        return Estadio;
    }

    public StringProperty getJornadas() {
        return jornadas;
    }
    public StringProperty getliga(){
        return liga;
    }

    public LocalDate fechaproperty() {
        return fecha;
    }


}
