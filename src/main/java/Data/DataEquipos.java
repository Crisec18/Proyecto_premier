package Data;

import DTO.Equipos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.sql.*;

public class DataEquipos {
    //SQL no se incluye el id porque es auto incremental
    private static final String SQL_INSERT_EQUIPO =
            "INSERT INTO Equipo " +
                    "(nombre, estadio, ciudad, " +
                    "annio_fundacion) VALUES (?, ?, ?, ?)";
    private static final String SQL_SELECT_EQUIPOS =
            "SELECT id, nombre, estadio, ciudad, annio_fundacion, " +
                    "partidos_jugados, partidos_ganados, partidos_perdidos, " +
                    "goles_empatados, goles_a_favor, goles_en_contra, puntos " +
                    "FROM Equipo";
    private static final String SQL_UPDATE_EQUIPO =
            "UPDATE Equipo SET nombre = ?, estadio = ?, ciudad = ?, annio_fundacion = ? WHERE id = ?";

    private static final String SQL_UPDATE_ESTADISTICAS =
            "UPDATE Equipo SET partidos_jugados = ?, partidos_ganados = ?, " +
                    "partidos_perdidos = ?, goles_empatados = ?, goles_a_favor = ?, " +
                    "goles_en_contra = ?, puntos = ? WHERE id = ?";
    //XML para equipos
    private static DataEquipos instance;


    private final AtomicInteger idcounter = new AtomicInteger(1);
    private ObservableList<Equipos> equipos;
    private FilteredList<Equipos> equiposfiltrados;
    private FilteredList<Equipos> filtradovisitante;

    //contructor
    public DataEquipos(){
        equipos = FXCollections.observableArrayList();
        equiposfiltrados = new FilteredList<>(equipos);
        filtradovisitante = new FilteredList<>(equipos);
    }
//singlenton con ruta xml
    public static DataEquipos getInstance() {
        if (instance == null) {
            instance = new DataEquipos();
        }
        return instance;
    }

    public ObservableList<Equipos> getEquipos(){
        return equipos;
    }

    public FilteredList<Equipos> getEquiposfiltrados(){
        return equiposfiltrados;
    }

    public FilteredList<Equipos> getFiltradovisitante() {
        return filtradovisitante;
    }






    //GUARDAR EN SQL
    public int insertarSQL(Equipos equi) throws SQLException {
        try(Connection con = ConnectionFactory.getConnection();
        PreparedStatement ps = con.prepareStatement(SQL_INSERT_EQUIPO,Statement.RETURN_GENERATED_KEYS)
        ){
            ps.setString(1, equi.nombreEquipoProperty().getValue());
            ps.setString(2, equi.estadioEquipoProperty().getValue());
            ps.setString(3, equi.ciudadEquipoProperty().getValue());
            ps.setDate(4, Date.valueOf(equi.getAnnio()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()){
                if(rs.next()){
                    int generatedId = rs.getInt(1);
                    equi.idEquipoProperty().setValue(String.valueOf(generatedId));
                    return generatedId;
                }
            }
            return -1;
        }catch (SQLException e){
            throw new RuntimeException("Error al insertar equipo" + e.getMessage(), e);
        }
    }

        //Modificar en SQL
    public void ModificarSQL(Equipos equipo) throws SQLException {


    }

    public List<Equipos> cargarSQL() throws SQLException {
        List<Equipos> listaEquipos = new ArrayList<>();
        try(Connection con = ConnectionFactory.getConnection();
        PreparedStatement ps = con.prepareStatement(SQL_SELECT_EQUIPOS);
        ResultSet rs = ps.executeQuery()
        ){
            while (rs.next()){
                String idEquipo = rs.getString("id");
                String nombreEquipo = rs.getString("nombre");
                String estadioEquipo = rs.getString("estadio");
                String ciudadEquipo = rs.getString("ciudad");
                LocalDate annioFundacion = rs.getDate("annio_fundacion").toLocalDate();

                Equipos equipo = new Equipos(
                        String.valueOf(idEquipo),
                        nombreEquipo,
                        estadioEquipo,
                        ciudadEquipo,
                        annioFundacion
                );
                equipo.setPartidosjugados(rs.getInt("partidos_jugados"));
                equipo.setpartidosgandos(rs.getInt("partidos_ganados"));
                equipo.setpartidosperdidos(rs.getInt("partidos_perdidos"));
                equipo.setPartidosempatados(rs.getInt("goles_empatados"));
                equipo.setGolesafavor(rs.getInt("goles_a_favor"));
                equipo.setGolesencontra(rs.getInt("goles_en_contra"));
                equipo.setPuntos(rs.getInt("puntos"));
                listaEquipos.add(equipo);
            }
        }catch (SQLException e){
            throw new RuntimeException("Error al cargar equipos desde SQL" + e.getMessage(), e);
        }
        return listaEquipos;
    }

    public boolean actualizarSQL(Equipos equipo) throws SQLException {
        try(Connection con = ConnectionFactory.getConnection();
            PreparedStatement ps = con.prepareStatement(SQL_UPDATE_EQUIPO)
        ){
            ps.setString(1, equipo.nombreEquipoProperty().getValue());
            ps.setString(2, equipo.estadioEquipoProperty().getValue());
            ps.setString(3, equipo.ciudadEquipoProperty().getValue());
            ps.setDate(4, Date.valueOf(equipo.getAnnio()));
            ps.setInt(5, equipo.getIdEquipo());
            return ps.executeUpdate()>0;
        }catch (SQLException e){
            throw new RuntimeException("Error al actualizar equipo: " + e.getMessage(), e);
        }
    }

    public boolean actualizarEstadisticasSQL(Equipos equipo) throws SQLException {
        try(Connection con = ConnectionFactory.getConnection();
            PreparedStatement ps = con.prepareStatement(SQL_UPDATE_ESTADISTICAS)
        ){
            ps.setInt(1, equipo.jugadosProperty().get());
            ps.setInt(2, equipo.ganadosProperty().get());
            ps.setInt(3, equipo.perdidosProperty().get());
            ps.setInt(4, equipo.empatesProperty().get());
            ps.setInt(5, equipo.golesFavorProperty().get());
            ps.setInt(6, equipo.golesContraProperty().get());
            ps.setInt(7, equipo.puntosProperty().get());
            ps.setInt(8, equipo.getIdEquipo());

            return ps.executeUpdate() > 0;
        }catch (SQLException e){
            throw new RuntimeException("Error al actualizar estadísticas del equipo: " + e.getMessage(), e);
        }
    }

    public boolean eliminarSQL(int id) {
        try(Connection con = ConnectionFactory.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM Equipo WHERE id = ?")
        ){
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }catch (SQLException e){
            throw new RuntimeException("Error al eliminar equipo: " + e.getMessage(), e);
        }
    }
}



