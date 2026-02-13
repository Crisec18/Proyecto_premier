package Logic;
import DTO.Equipos;
import Data.DataEquipos;
import client.EquipoServiceClientSocket;

import java.io.IOException;
import java.util.List;
import java.sql.SQLException;

public class LogicaEquipo {

    private final String host = "127.0.0.1";
    private final int port = 5050;

    private final DataEquipos datos;

    public LogicaEquipo(DataEquipos datos) {
        this.datos = datos;
    }

    public List<Equipos> cargarEquipos() throws Exception {
        try (var service = new EquipoServiceClientSocket(host, port)) {
            return service.listar();
        }
    }

    public void guardar(List<Equipos> equipos) throws SQLException {
        for(Equipos equipo : equipos){
            if(equipo.getIdEquipo() == 0){
                datos.insertarSQL(equipo);
            }
        }
    }
    public void eliminar(int equipo) throws Exception {
        try (var service = new EquipoServiceClientSocket(host, port)) {
            service.eliminar(equipo);
        }
    }

    public void actualizarEstadisticas(Equipos equipo) throws SQLException {
        try (var service = new EquipoServiceClientSocket(host, port)) {
            service.actualizarEstadisticas(equipo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public int obtenerSiguienteId(List<Equipos> equipos) {
        int max = 0;
        for (Equipos e : equipos) {
            if (e.getIdEquipo() > max) {;
                max = e.getIdEquipo();
            }
        }
        return max + 1;
    }
    public int guardarEquipo(Equipos equipo) throws Exception{
        try (var service = new EquipoServiceClientSocket(host, port)) {
            return service.crear(equipo);
        }
    }

    public void actualizar(Equipos equipo) throws Exception {
        try (var service = new EquipoServiceClientSocket(host, port)) {
            service.actualizar(equipo);
        }
    }

}
