package DTO;

    import javafx.beans.property.*;
    import javafx.beans.value.ObservableValue;

    import java.time.LocalDate;

public class Equipos {
        private StringProperty id;
        private StringProperty nombre;
        private StringProperty ciudad;
        private StringProperty estadio;
        LocalDate annio;

        private IntegerProperty partidosjugados;
        private IntegerProperty partidosganados;
        private IntegerProperty partidosempatados;
        private IntegerProperty partidosperdidos;
        private IntegerProperty golesafavor;
        private IntegerProperty golesencontra;
        private IntegerProperty puntos;

        public Equipos(){
            this.id = new SimpleStringProperty("");
            this.nombre = new SimpleStringProperty("");
            this.estadio = new SimpleStringProperty("");
            this.ciudad = new SimpleStringProperty("");
            this.annio = LocalDate.now();
            this.partidosjugados = new SimpleIntegerProperty(0);
            this.partidosganados = new SimpleIntegerProperty(0);
            this.partidosempatados = new SimpleIntegerProperty(0);
            this.partidosperdidos = new SimpleIntegerProperty(0);
            this.golesafavor = new SimpleIntegerProperty(0);
            this.golesencontra = new SimpleIntegerProperty(0);
            this.puntos = new SimpleIntegerProperty(0);
        }

        public Equipos(String id1, String nombre, String estadio, String ciudad, LocalDate annio) {
            this.id = new SimpleStringProperty(id1);
            this.nombre = new SimpleStringProperty(nombre);
            this.estadio = new SimpleStringProperty(estadio);
            this.ciudad = new SimpleStringProperty(ciudad);
            this.annio = annio;
            this.partidosjugados = new SimpleIntegerProperty(0);
            this.partidosganados = new SimpleIntegerProperty(0);
            this.partidosempatados = new SimpleIntegerProperty(0);
            this.partidosperdidos = new SimpleIntegerProperty(0);
            this.golesafavor = new SimpleIntegerProperty(0);
            this.golesencontra = new SimpleIntegerProperty(0);
            this.puntos = new SimpleIntegerProperty(0);
        }


        // Propiedades sobre equipo
        public StringProperty idEquipoProperty() { return id; }
        public StringProperty nombreEquipoProperty() { return nombre; }
        public StringProperty ciudadEquipoProperty() { return ciudad; }
        public StringProperty estadioEquipoProperty() { return estadio; }
        public LocalDate annioEquipoProperty() { return annio; }
        //sobre las mejengas
        public IntegerProperty ganadosProperty() { return partidosganados; }
        public IntegerProperty empatesProperty() { return partidosempatados; }
        public IntegerProperty perdidosProperty() { return partidosperdidos; }
        public IntegerProperty golesFavorProperty() { return golesafavor; }
        public IntegerProperty golesContraProperty() { return golesencontra; }
        public IntegerProperty puntosProperty() { return puntos; }
        public IntegerProperty jugadosProperty() { return partidosjugados; }

        public void setNombre(String nombre1) { this.nombre.set(nombre1); }
        public void setEstadio(String estadio1) { this.estadio.set(estadio1); }
        public void setCiudad(String ciudad1) { this.ciudad.set(ciudad1); }
        public void setpartidosgandos(int partidosgando){this.partidosganados.set(partidosgando);}
        public void setpartidosperdidos(int partidosperdido){this.partidosperdidos.set(partidosperdido);}

        public void setPartidosganados() { this.partidosganados.set(this.partidosganados.get() + 1);}
        public void setPartidosempatados(int valor) { this.partidosempatados.set(valor); }
        public void setPartidosperdidos() {
        this.partidosperdidos.set(this.partidosperdidos.get() + 1);
        }
        public void setGolesafavor(int valor) { this.golesafavor.set(valor); }
        public void setGolesencontra(int valor) { this.golesencontra.set(valor); }
        public void setPuntos(int valor) { this.puntos.set(valor); }

        public String getNombre() { return nombre.get(); }
        public LocalDate getAnnio() { return annio; }
        public int getPuntos() { return puntos.get(); }

    public void setIdEquipo(int idEquipo) {
        this.id.set(String.valueOf(idEquipo));
    }

    public void setAnioFundacion(int anio) {
        this.annio = LocalDate.of(anio, 1, 1);
    }

    public String getCiudad() { return ciudad.get(); }

    public String getEstadio() { return estadio.get(); }

    public int getAnioFundacion() {
        return annio.getYear();
    }
    //auxiliares
    public int getIdEquipo() {
        try{
            return Integer.parseInt(id.get());
        }catch(NumberFormatException e){
            return 0;
        }
    }
    public void setPartidosjugados(int i) {
        this.partidosjugados.set(i);
    }
    public int getPartidosJugados() {
        return partidosjugados.get();
    }
    public int getPartidosGanados() {
        return partidosganados.get();
    }
    public int getPartidosPerdidos() {
        return partidosperdidos.get();
    }
    public int getPartidosEmpatados() {
        return partidosempatados.get();
    }
    public int getGolesFavor() {
        return golesafavor.get();
    }
    public int getGolesContra() {
        return golesencontra.get();
    }

}