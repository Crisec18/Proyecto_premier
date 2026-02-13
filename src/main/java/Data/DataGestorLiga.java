package Data;

import DTO.Equipos;
import DTO.LigaDTO;
import DTO.PartidosDTO;
import javafx.collections.FXCollections;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DataGestorLiga {
    // sentencias SQL
    private static final String SQL_INSERT_LIGA =
            "INSERT INTO Liga (nombre, region) VALUES (?, ?)";

    private static final String SQL_SELECT_LIGAS =
            "SELECT id, nombre, region FROM Liga";



    private static DataGestorLiga instance;
    private final AtomicInteger idcounter = new AtomicInteger(1);
    private ObservableList<LigaDTO> ligas;
    private ObservableList<Equipos> todoslosequipos;


    private FilteredList<LigaDTO> ligasfiltradas;
    private final FilteredList<Equipos> equiposFiltrados;


    public DataGestorLiga() {
        todoslosequipos = FXCollections.observableArrayList();
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

    public ObservableList<LigaDTO> getLigas() {
        return ligas;
    }

    public FilteredList<LigaDTO> getLigasfiltradas() {
        return ligasfiltradas;
    }

    public void agregarLiga(String nombre, String region) {
        String id = String.valueOf(idcounter.getAndIncrement()); // consultar esto con el profe
        LigaDTO liga = new LigaDTO(id, nombre, region);
        ligas.add(liga);
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

    public LigaDTO buscarLigaPorNombre(String nombreLiga) {
        for (LigaDTO liga : ligas) {
            if (liga.nombreLigaProperty().get().equals(nombreLiga)) {
                return liga;
            }
        }
        return null;
    }
    public ObservableList<Equipos> getTodosLosEquipos() {
        return todoslosequipos;
    }


    public boolean verificarcamposequipos(String nombreLiga) {
        LigaDTO liga = buscarLigaPorNombre(nombreLiga);
        return liga != null && liga.getpartidos().size() >= 20;
    }

    public boolean verificarcampospartido(String nombreLiga) {
        LigaDTO liga = buscarLigaPorNombre(nombreLiga);
        return liga != null && liga.getequipos().size() >= 15;
    }

    public boolean equipoTienePartidos(Equipos equipo) {
        return DataPartidos.getInstance().getPartidos().stream()
                .anyMatch(p ->
                        p.getlocal().idEquipoProperty().get()
                                .equals(equipo.idEquipoProperty().get())
                                || p.getvisitante().idEquipoProperty().get()
                                .equals(equipo.idEquipoProperty().get())
                );
    }


    public boolean equipoEnOtraLiga(Equipos equipo, String ligaActual, DataPartidos dataPartidos) {
        String idEquipo = equipo.idEquipoProperty().get();
        LigaDTO liga = this.getLigas().stream()
                .filter(l -> l.nombreLigaProperty().get().equals(ligaActual))
                .findFirst()
                .orElse(null);

        if (liga == null) return false;

        int idLigaActual = Integer.parseInt(liga.idLigaProperty().get());

        boolean tienePartidosEnOtraLiga = dataPartidos.getPartidos().stream()
                .anyMatch(p -> {
                    boolean esLocal = p.getlocal().idEquipoProperty().get().equals(idEquipo);
                    boolean esVisitante = p.getvisitante().idEquipoProperty().get().equals(idEquipo);
                    boolean enOtraLiga = p.getliga().get() != idLigaActual;
                    return (esLocal || esVisitante) && enOtraLiga;
                });

        return tienePartidosEnOtraLiga;
    }

    public boolean equipoYaEnLiga(Equipos equipo, String nombreLiga) {
        LigaDTO liga = buscarLigaPorNombre(nombreLiga);
        if (liga == null) return false;

        String idEquipo = equipo.idEquipoProperty().get();
        return liga.getequipos().stream()
                .anyMatch(e -> e.idEquipoProperty().get().equals(idEquipo));
    }


    public void actualizarContadorId() {
        int maxId = 0;
        for (LigaDTO liga : ligas) {
            try {
                int idActual = Integer.parseInt(liga.idLigaProperty().getValue());
                if (idActual > maxId) {
                    maxId = idActual;
                }
            } catch (NumberFormatException e) {
            }
        }
        idcounter.set(maxId + 1);
    }


    public void filtrarPorLiga(LigaDTO ligaSeleccionada) {
        if (ligaSeleccionada == null) {
            ligasfiltradas.setPredicate(liga -> true);
            equiposFiltrados.setPredicate(equipo -> true);
        } else {
            ligasfiltradas.setPredicate(liga -> liga.equals(ligaSeleccionada));
            equiposFiltrados.setPredicate(equipo ->
                    ligaSeleccionada.getequipos().stream()
                            .anyMatch(e -> e.idEquipoProperty().get().equals(equipo.idEquipoProperty().get()))
            );
        }
    }

    //CREACIONES SQL

    public void cargarLigaCompletaSQL(LigaDTO liga) throws SQLException {
        int idLiga = Integer.parseInt(liga.idLigaProperty().get());

        // 1. Cargar todos los partidos de esta liga
        String SQL_PARTIDOS_LIGA =
                "SELECT p.id, p.fecha, p.jornada, p.estadio, p.estado, " +
                        "p.goles_local, p.goles_visitante, " +
                        "p.id_equipo_local, p.id_equipo_visitante " +
                        "FROM Partido p WHERE p.id_liga = ?";

        // 2. Cargar equipos únicos de esta liga
        String SQL_EQUIPOS_LIGA =
                "SELECT DISTINCT e.id, e.nombre, e.estadio, e.ciudad, e.annio_fundacion, " +
                        "e.partidos_jugados, e.partidos_ganados, e.partidos_perdidos, " +
                        "e.goles_empatados, e.goles_a_favor, e.goles_en_contra, e.puntos " +
                        "FROM Equipo e " +
                        "INNER JOIN Partido p ON (e.id = p.id_equipo_local OR e.id = p.id_equipo_visitante) " +
                        "WHERE p.id_liga = ?";

        try(Connection con = ConnectionFactory.getConnection()) {
            Map<String, Equipos> equiposMap = new HashMap<>();
            try(PreparedStatement ps = con.prepareStatement(SQL_EQUIPOS_LIGA)) {
                ps.setInt(1, idLiga);
                try(ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        String id = rs.getString("id");
                        Equipos equipo = new Equipos(
                                id,
                                rs.getString("nombre"),
                                rs.getString("estadio"),
                                rs.getString("ciudad"),
                                rs.getDate("annio_fundacion").toLocalDate()
                        );
                        equipo.setPartidosjugados(rs.getInt("partidos_jugados"));
                        equipo.setpartidosgandos(rs.getInt("partidos_ganados"));
                        equipo.setpartidosperdidos(rs.getInt("partidos_perdidos"));
                        equipo.setPartidosempatados(rs.getInt("goles_empatados"));
                        equipo.setGolesafavor(rs.getInt("goles_a_favor"));
                        equipo.setGolesencontra(rs.getInt("goles_en_contra"));
                        equipo.setPuntos(rs.getInt("puntos"));

                        liga.getequipos().add(equipo);
                        equiposMap.put(id, equipo);
                    }
                }
            }

            // Luego cargar partidos
            try(PreparedStatement ps = con.prepareStatement(SQL_PARTIDOS_LIGA)) {
                ps.setInt(1, idLiga);
                try(ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        String idPartido = rs.getString("id");
                        Equipos local = equiposMap.get(rs.getString("id_equipo_local"));
                        Equipos visitante = equiposMap.get(rs.getString("id_equipo_visitante"));

                        PartidosDTO partido = new PartidosDTO(
                                local.getNombre() + " vs " + visitante.getNombre(),
                                local,
                                visitante,
                                rs.getString("jornada"),
                                idPartido,
                                rs.getDate("fecha").toLocalDate(),
                                rs.getString("estadio")
                        );
                        partido.estadoProperty().set(rs.getString("estado"));
                        partido.setliga(idLiga);

                        liga.getpartidos().add(partido);
                    }
                }
            }

            // Cargar jornadas únicas
            Set<String> jornadasSet = new HashSet<>();
            for(PartidosDTO p : liga.getpartidos()) {
                jornadasSet.add(p.jornadasProperty().get());
            }
            liga.getjornadas().addAll(jornadasSet);

        } catch (SQLException e) {
            throw new RuntimeException("Error al cargar liga completa: " + e.getMessage(), e);
        }
    }

    public int guardarLigasSQL(LigaDTO Liga) throws SQLException {
            try(Connection con = ConnectionFactory.getConnection();
                PreparedStatement ps = con.prepareStatement(SQL_INSERT_LIGA, Statement.RETURN_GENERATED_KEYS)
            ){
                ps.setString(1, Liga.nombreLigaProperty().getValue());
                ps.setString(2, Liga.regionLigaProperty().getValue());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()){
                    if(rs.next()){
                        int generatedId = rs.getInt(1);
                        Liga.idLigaProperty().setValue(String.valueOf(rs.getInt(1)));
                        return generatedId;
                    }
                }
                return -1;
            }catch (SQLException e){
                throw new RuntimeException("Error al insertar liga: " + e.getMessage(), e);
            }
        }

    public List<LigaDTO> cargarSQL() throws SQLException {
        List<LigaDTO> listaLigas = new ArrayList<>();
        try(Connection con = ConnectionFactory.getConnection();
            PreparedStatement ps = con.prepareStatement(SQL_SELECT_LIGAS);
            ResultSet rs = ps.executeQuery()
        ){
            while (rs.next()){
                LigaDTO liga = new LigaDTO(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("region")
                );
                cargarLigaCompletaSQL(liga);
                listaLigas.add(liga);
            }
        }
        return listaLigas;
    }

    /**
     * Verifica si un equipo ya tiene partidos en una liga diferente a la especificada
     * @param idEquipo ID del equipo a verificar
     * @param idLigaActual ID de la liga actual (se excluye de la búsqueda)
     * @return true si el equipo tiene partidos en otra liga, false en caso contrario
     */
    public boolean equipoEnOtraLigaSQL(int idEquipo, int idLigaActual) throws SQLException {
        String SQL_EQUIPO_EN_OTRA_LIGA = 
            "SELECT COUNT(*) as total FROM Partido " +
            "WHERE (id_equipo_local = ? OR id_equipo_visitante = ?) " +
            "AND id_liga != ?";
        
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_EQUIPO_EN_OTRA_LIGA)) {
            
            ps.setInt(1, idEquipo);
            ps.setInt(2, idEquipo);
            ps.setInt(3, idLigaActual);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }
        }
        return false;
    }

    /**
     * Verifica si un equipo ya está registrado en una liga específica
     * @param idEquipo ID del equipo a verificar
     * @param idLiga ID de la liga
     * @return true si el equipo tiene partidos en esta liga, false en caso contrario
     */
    public boolean equipoYaEnLigaSQL(int idEquipo, int idLiga) throws SQLException {
        String SQL_EQUIPO_EN_LIGA = 
            "SELECT COUNT(*) as total FROM Partido " +
            "WHERE (id_equipo_local = ? OR id_equipo_visitante = ?) " +
            "AND id_liga = ?";
        
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_EQUIPO_EN_LIGA)) {
            
            ps.setInt(1, idEquipo);
            ps.setInt(2, idEquipo);
            ps.setInt(3, idLiga);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }
        }
        return false;
    }
}






