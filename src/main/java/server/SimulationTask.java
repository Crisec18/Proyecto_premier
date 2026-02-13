package server;

import DTO.Equipos;
import DTO.PartidosDTO;
import Data.ConnectionFactory;
import Data.DataPartidos;

import java.sql.*;
import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.Callable;

public class SimulationTask implements Callable<PartidosDTO> {
    private final int partidoId;

    public SimulationTask(int partidoId) {
        this.partidoId = partidoId;
    }

    @Override
    public PartidosDTO call() throws Exception {
        Connection conn = ConnectionFactory.getConnection();
        try {
            conn.setAutoCommit(false);

            // Intentar bloquear fila del partido
            String select = "SELECT id, fecha, id_equipo_local, id_equipo_visitante, jornada, estadio, estado, goles_local, goles_visitante, id_liga FROM Partido WHERE id = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(select)) {
                ps.setInt(1, partidoId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.err.println("[SimulationTask] Partido no encontrado en BD: " + partidoId);
                        return null; // no hay fila, no simular
                    }

                    String estado = rs.getString("estado");
                    int golesLocalDb = rs.getInt("goles_local");
                    int golesVisitanteDb = rs.getInt("goles_visitante");
                    if (!"Pendiente".equalsIgnoreCase(estado)) {
                        System.err.println("[SimulationTask] Partido no está en estado 'Pendiente' (id=" + partidoId + ", estado=" + estado + ")");
                        return null; // no simular si no está pendiente
                    }
                    if (golesLocalDb != 0 || golesVisitanteDb != 0) {
                        System.err.println("[SimulationTask] Partido ya tiene goles registrados (id=" + partidoId + ")");
                        return null; // ya tiene resultado
                    }

                    int idLocal = rs.getInt("id_equipo_local");
                    int idVisitante = rs.getInt("id_equipo_visitante");
                    Date fechaSql = rs.getDate("fecha");
                    LocalDate fecha = fechaSql != null ? fechaSql.toLocalDate() : LocalDate.now();
                    int idLiga = rs.getInt("id_liga");

                    // Generar goles (algoritmo simple)
                    Random rnd = new Random();
                    int golesLocal = rnd.nextInt(6);
                    int golesVisitante = rnd.nextInt(6);

                    // Actualizar partido
                    String updatePartido = "UPDATE Partido SET goles_local = ?, goles_visitante = ?, estado = 'Finalizado' WHERE id = ?";
                    try (PreparedStatement up = conn.prepareStatement(updatePartido)) {
                        up.setInt(1, golesLocal);
                        up.setInt(2, golesVisitante);
                        up.setInt(3, partidoId);
                        up.executeUpdate();
                    }

                    // actualizar estadísticas de equipos
                    Equipos local = null;
                    Equipos visitante = null;
                    try (PreparedStatement psEq = conn.prepareStatement("SELECT id, nombre, partidos_jugados, partidos_ganados, partidos_perdidos, goles_a_favor, goles_en_contra, puntos, goles_empatados FROM Equipo WHERE id = ?")) {
                        psEq.setInt(1, idLocal);
                        try (ResultSet rle = psEq.executeQuery()) {
                            if (rle.next()) {
                                local = new Equipos();
                                local.setIdEquipo(rle.getInt("id"));
                                local.setNombre(rle.getString("nombre"));
                                local.setPartidosjugados(rle.getInt("partidos_jugados"));
                                local.setpartidosgandos(rle.getInt("partidos_ganados"));
                                local.setpartidosperdidos(rle.getInt("partidos_perdidos"));
                                local.setGolesafavor(rle.getInt("goles_a_favor"));
                                local.setGolesencontra(rle.getInt("goles_en_contra"));
                                local.setPuntos(rle.getInt("puntos"));
                            }
                        }
                        psEq.setInt(1, idVisitante);
                        try (ResultSet rve = psEq.executeQuery()) {
                            if (rve.next()) {
                                visitante = new Equipos();
                                visitante.setIdEquipo(rve.getInt("id"));
                                visitante.setNombre(rve.getString("nombre"));
                                visitante.setPartidosjugados(rve.getInt("partidos_jugados"));
                                visitante.setpartidosgandos(rve.getInt("partidos_ganados"));
                                visitante.setpartidosperdidos(rve.getInt("partidos_perdidos"));
                                visitante.setGolesafavor(rve.getInt("goles_a_favor"));
                                visitante.setGolesencontra(rve.getInt("goles_en_contra"));
                                visitante.setPuntos(rve.getInt("puntos"));
                            }
                        }
                    }

                    if (local == null || visitante == null) {
                        System.err.println("[SimulationTask] Equipos del partido no encontrados en BD (id=" + partidoId + ")");
                        conn.rollback();
                        return null;
                    }

                    // ajustar contadores
                    local.setPartidosjugados(local.jugadosProperty().get() + 1);
                    visitante.setPartidosjugados(visitante.jugadosProperty().get() + 1);

                    if (golesLocal > golesVisitante) {
                        local.setPuntos(local.puntosProperty().get() + 3);
                        local.setpartidosgandos(local.ganadosProperty().get() + 1);
                        visitante.setpartidosperdidos(visitante.perdidosProperty().get() + 1);
                    } else if (golesVisitante > golesLocal) {
                        visitante.setPuntos(visitante.puntosProperty().get() + 3);
                        visitante.setpartidosgandos(visitante.ganadosProperty().get() + 1);
                        local.setpartidosperdidos(local.perdidosProperty().get() + 1);
                    } else {
                        // empate
                        local.setPartidosempatados(local.empatesProperty().get() + 1);
                        visitante.setPartidosempatados(visitante.empatesProperty().get() + 1);
                        local.setPuntos(local.puntosProperty().get() + 1);
                        visitante.setPuntos(visitante.puntosProperty().get() + 1);
                    }

                    local.setGolesafavor(local.golesFavorProperty().get() + golesLocal);
                    local.setGolesencontra(local.golesContraProperty().get() + golesVisitante);
                    visitante.setGolesafavor(visitante.golesFavorProperty().get() + golesVisitante);
                    visitante.setGolesencontra(visitante.golesContraProperty().get() + golesLocal);

                    // Persistir equipos
                    String updateEquipo = "UPDATE Equipo SET partidos_jugados = ?, partidos_ganados = ?, partidos_perdidos = ?, goles_empatados = ?, goles_a_favor = ?, goles_en_contra = ?, puntos = ? WHERE id = ?";
                    try (PreparedStatement upe = conn.prepareStatement(updateEquipo)) {
                        upe.setInt(1, local.jugadosProperty().get());
                        upe.setInt(2, local.ganadosProperty().get());
                        upe.setInt(3, local.perdidosProperty().get());
                        upe.setInt(4, local.empatesProperty().get());
                        upe.setInt(5, local.golesFavorProperty().get());
                        upe.setInt(6, local.golesContraProperty().get());
                        upe.setInt(7, local.puntosProperty().get());
                        upe.setInt(8, local.getIdEquipo());
                        upe.executeUpdate();
                    }
                    try (PreparedStatement upe2 = conn.prepareStatement(updateEquipo)) {
                        upe2.setInt(1, visitante.jugadosProperty().get());
                        upe2.setInt(2, visitante.ganadosProperty().get());
                        upe2.setInt(3, visitante.perdidosProperty().get());
                        upe2.setInt(4, visitante.empatesProperty().get());
                        upe2.setInt(5, visitante.golesFavorProperty().get());
                        upe2.setInt(6, visitante.golesContraProperty().get());
                        upe2.setInt(7, visitante.puntosProperty().get());
                        upe2.setInt(8, visitante.getIdEquipo());
                        upe2.executeUpdate();
                    }

                    conn.commit();

                    // Construir DTO resultado
                    PartidosDTO dto = new PartidosDTO();
                    dto.setIdPartido(partidoId);
                    dto.setFecha(fecha);
                    dto.setLocal(local);
                    dto.setVisitante(visitante);
                    dto.setJornada(rs.getString("jornada"));
                    dto.setEstadio(rs.getString("estadio"));
                    dto.setEstado("Finalizado");
                    dto.setGolesLocal(golesLocal);
                    dto.setGolesVisitante(golesVisitante);
                    dto.setliga(idLiga);

                    return dto;
                }
            }
        } catch (Exception ex) {
            try { conn.rollback(); } catch (Exception ignore) {}
            throw ex;
        } finally {
            try { conn.close(); } catch (Exception ignore) {}
        }
    }
}