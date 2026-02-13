package Data;

import DTO.Equipos;
import DTO.LigaDTO;
import DTO.PartidosDTO;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DataPartidos {
// instnacias SQL usadas
    private static final String SQL_INSERT_PARTIDO =
            "INSERT INTO Partido (fecha, id_equipo_local, id_equipo_visitante, " +
                    "jornada, estadio, estado, goles_local, goles_visitante, id_liga) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_PARTIDOS =
            "SELECT id, fecha, id_equipo_local, id_equipo_visitante, " +
                    "jornada, estadio, estado, goles_local, goles_visitante, id_liga " +
                    "FROM Partido";

    private static final String SQL_UPDATE_PARTIDO =
            "UPDATE Partido SET estado = ?, goles_local = ?, goles_visitante = ? " +
                    "WHERE id = ?";


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


    public PartidosDTO agregarPartido(String partidonombre, Equipos equipo1, Equipos equipo2, String jornada, String Estadio, LocalDate fecha) {
        String id = String.valueOf(idcounter.getAndIncrement());
        PartidosDTO partido = new PartidosDTO(partidonombre, equipo1, equipo2, jornada, id, fecha, Estadio);
        partidos.add(partido);
        return partido;

    }

//Para los contadores de partidos------------------------------------------------------------
    //contador de equipos y refactorizar


    public int contarPartidosPorEstado(String estado) {
        return partidos.stream().filter(p -> p.estadoProperty().get().equals(estado)).toList().size();
    }
    //para los partidos que tienen estado pendiente en una jornada dada
    public boolean estaJornadaEsSimulable(String nombreJornada) {
        return partidos.stream().anyMatch(p -> p.getJornadas().get().equals(nombreJornada) &&
                        p.estadoProperty().get().equalsIgnoreCase("Pendiente"));
    }

    private Equipos buscarEquipoPorId(DataEquipos dataEquipos, String id) {
        return dataEquipos.getEquipos().stream()
                .filter(eq -> eq.idEquipoProperty().getValue().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void actualizarContadorId() {
        int maxId = 0;
        for (PartidosDTO Partidos : partidos) {
            try {
                int idActual = Integer.parseInt(Partidos.idpartidoProperty().getValue());
                if (idActual > maxId) {
                    maxId = idActual;
                }
            } catch (NumberFormatException e) {
            }
        }
        idcounter.set(maxId + 1);
    }

    // SQL metodos

    public void insertarSQL(PartidosDTO partido, int idLiga) throws SQLException {
        try(Connection con = ConnectionFactory.getConnection();
            PreparedStatement ps = con.prepareStatement(SQL_INSERT_PARTIDO, Statement.RETURN_GENERATED_KEYS)
        ){
            ps.setDate(1, Date.valueOf(partido.getfecha()));
            ps.setInt(2, partido.getlocal().getIdEquipo());
            ps.setInt(3, partido.getvisitante().getIdEquipo());
            ps.setString(4, partido.jornadasProperty().getValue());
            ps.setString(5, partido.estadioProperty().getValue());
            ps.setString(6, partido.estadoProperty().getValue());
            ps.setInt(7, partido.getlocal().golesFavorProperty().get());
            ps.setInt(8, partido.getvisitante().golesFavorProperty().get());
            ps.setInt(9, idLiga);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()){
                if(rs.next()){
                    partido.idpartidoProperty().setValue(String.valueOf(rs.getInt(1)));
                }
            }
            partido.setliga(idLiga);
        }catch (SQLException e){
            throw new RuntimeException("Error al insertar partido: " + e.getMessage(), e);
        }
    }

    public List<PartidosDTO> cargarSQL(DataEquipos dataEquipos) throws SQLException {
        List<PartidosDTO> listaPartidos = new ArrayList<>();

        List<Equipos> todosEquipos = dataEquipos.cargarSQL();

        try(Connection con = ConnectionFactory.getConnection();
            PreparedStatement ps = con.prepareStatement(SQL_SELECT_PARTIDOS);
            ResultSet rs = ps.executeQuery()
        ){
            while(rs.next()){
                String id = rs.getString("id");
                LocalDate fecha = rs.getDate("fecha").toLocalDate();
                int idLocal = rs.getInt("id_equipo_local");
                int idVisitante = rs.getInt("id_equipo_visitante");

                Equipos local = todosEquipos.stream()
                        .filter(eq -> eq.getIdEquipo() == idLocal)
                        .findFirst()
                        .orElse(null);

                Equipos visitante = todosEquipos.stream()
                        .filter(eq -> eq.getIdEquipo() == idVisitante)
                        .findFirst()
                        .orElse(null);

                if(local == null || visitante == null) continue;

                PartidosDTO partido = new PartidosDTO(
                        local.getNombre() + " vs " + visitante.getNombre(),
                        local,
                        visitante,
                        rs.getString("jornada"),
                        id,
                        fecha,
                        rs.getString("estadio")
                );
                partido.setGolesLocal(rs.getInt("goles_local"));
                partido.setGolesVisitante(rs.getInt("goles_visitante"));
                partido.estadoProperty().set(rs.getString("estado"));
                partido.setliga(rs.getInt("id_liga"));
                listaPartidos.add(partido);
            }
        }catch (SQLException e){
            throw new RuntimeException("Error al cargar partidos: " + e.getMessage(), e);
        }

        return listaPartidos;
    }

    // Método de compatibilidad: antiguas clases podrían invocar cargarSQLFromDB
    public static List<PartidosDTO> cargarSQLFromDB(DataEquipos dataEquipos) throws SQLException {
        return DataPartidos.getInstance().cargarSQL(dataEquipos);
    }

    public void actualizarSQL(PartidosDTO partido) throws SQLException { // permite actualizar el resultado del controller de registrar resultados
        try(Connection con = ConnectionFactory.getConnection();
            PreparedStatement ps = con.prepareStatement(SQL_UPDATE_PARTIDO)
        ){
            ps.setString(1, partido.estadoProperty().getValue());
            ps.setInt(2, partido.golesLocalProperty().get());      // ✓ Goles del partido
            ps.setInt(3, partido.golesVisitanteProperty().get());
            ps.setInt(4, Integer.parseInt(partido.idpartidoProperty().getValue()));
            ps.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException("Error al actualizar partido: " + e.getMessage(), e);
        }
    }


    public PartidosDTO cargar() {
        return null;
    }
}
