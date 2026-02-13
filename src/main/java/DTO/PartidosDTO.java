package DTO;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
    IntegerProperty liga;
    private IntegerProperty goleslocal;
    private IntegerProperty golesvisitante;

    public PartidosDTO(String Nombre, Equipos local, Equipos visitante, String jornadas, String idpartido1, LocalDate fecha, String estadio) {
        this.idpartido = new SimpleStringProperty(idpartido1);
        this.fecha = fecha;
        this.local = local;
        this.visitante = visitante;
        this.jornadas = new SimpleStringProperty(jornadas);
        this.Idpartido = new SimpleStringProperty(idpartido1);
        this.Estadio = new SimpleStringProperty(estadio);
        this.nombrepartido = new SimpleStringProperty(Nombre);
        this.estado = new SimpleStringProperty("Pendiente");
        this.liga = new SimpleIntegerProperty(0);
        this.goleslocal = new SimpleIntegerProperty(0);
        this.golesvisitante = new SimpleIntegerProperty(0);
    }
    public PartidosDTO() {
        this.idpartido = new SimpleStringProperty("");
        this.nombrepartido = new SimpleStringProperty("");
        this.fecha = LocalDate.now();
        this.jornadas = new SimpleStringProperty("");
        this.Idpartido = new SimpleStringProperty("");
        this.Estadio = new SimpleStringProperty("");
        this.estado = new SimpleStringProperty("Pendiente");
        this.liga = new SimpleIntegerProperty(0);
        this.goleslocal = new SimpleIntegerProperty(0);
        this.golesvisitante = new SimpleIntegerProperty(0);
    }

    public Equipos getlocal() {
        return local;
    }

    public Equipos getvisitante() {
        return visitante;
    }

    public LocalDate getfecha() {
        return fecha;
    }

    public StringProperty jornadasProperty() {
        return jornadas;
    }

    public StringProperty idpartidoProperty() {
        return Idpartido;
    }

    public StringProperty nombrepartidoProperty() {
        return nombrepartido;
    }

    public StringProperty estadioProperty() {
        return Estadio;
    }

    public StringProperty jornadaproperty() {
        return jornadas;
    }

    public void setestado() {
        estado = new SimpleStringProperty("Finalizado");
    }
    public void setEstado(String estado) {
        this.estado.set(estado);
    }
    public StringProperty estadoProperty() {
        return estado;
    }



    public IntegerProperty golesLocalProperty() { return goleslocal; }
    public IntegerProperty golesVisitanteProperty() { return golesvisitante; }
    public void setGolesLocal(int goles) { this.goleslocal.set(goles); }
    public void setGolesVisitante(int goles) { this.golesvisitante.set(goles); }

    public void setliga(int liga1) {
        liga = new SimpleIntegerProperty(liga1);
    }
    public void setIdPartido(int id) {
        this.idpartido.set(String.valueOf(id));
        this.Idpartido.set(String.valueOf(id));
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setLocal(Equipos local) {
        this.local = local;
    }

    public void setVisitante(Equipos visitante) {
        this.visitante = visitante;
    }

    public void setJornada(String jornada) {
        this.jornadas.set(jornada);
    }

    public void setEstadio(String estadio) {
        this.Estadio.set(estadio);
    }

    public void setNombrePartido(String nombre) {
        this.nombrepartido.set(nombre);
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

    public IntegerProperty getliga() {
        return liga;
    }

    public LocalDate fechaproperty() {
        return fecha;
    }



}
