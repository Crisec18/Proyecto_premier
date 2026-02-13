package server;

import DTO.Equipos;
import DTO.LigaDTO;
import DTO.PartidosDTO;
import Data.DataEquipos;
import Data.DataGestorLiga;
import Data.DataPartidos;
import Data.ConnectionFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ClientHandler implements Runnable{

    private final Socket socket;
    private final DataEquipos dao = DataEquipos.getInstance();
    private final DataGestorLiga daoLigas = DataGestorLiga.getInstance();
    private final DataPartidos daoPartidos = DataPartidos.getInstance();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            String req;
            while ((req = in.readLine()) != null) {
                String res = procesar(req);
                out.write(res);
                out.write("\n");
                out.flush();
            }

        } catch (Exception e) {
            System.err.println("Cliente desconectado/error: " + e.getMessage());
        }
    }

    private String procesar(String req) {
        try {
            String[] p = req.split("\\|", -1);
            String cmd = p[0].trim().toUpperCase();

            switch (cmd) {
                case "LIST": {
                    List<Equipos> lista = dao.cargarSQL();
                    String payload = serializarLista(lista);
                    return "OK|" + payload;
                }
                case "CREATE": {
                    Equipos e = new Equipos();
                    e.setNombre(p[1]);
                    e.setCiudad(p[2]);
                    e.setEstadio(p[3]);
                    e.setAnioFundacion(Integer.parseInt(p[4]));
                    int id = dao.insertarSQL(e);
                    return (id > 0) ? "OK|ID=" + id : "ERR|No se pudo insertar";
                }
                case "UPDATE": {
                    Equipos e = new Equipos();
                    e.setIdEquipo(Integer.parseInt(p[1]));
                    e.setNombre(p[2]);
                    e.setCiudad(p[3]);
                    e.setEstadio(p[4]);
                    e.setAnioFundacion(Integer.parseInt(p[5]));
                    boolean ok = dao.actualizarSQL(e);
                    return ok ? "OK|Actualizado" : "ERR|No se pudo actualizar";
                }
                case "DELETE": {
                    int id = Integer.parseInt(p[1]);
                    boolean ok = dao.eliminarSQL(id);
                    return ok ? "OK|Eliminado" : "ERR|No se pudo eliminar";
                }
                case "UPDATE_STATS":
                    if (p.length < 9) return "ERR|Faltan parámetros";

                    Equipos e = new Equipos();
                    e.setIdEquipo(Integer.parseInt(p[1]));
                    e.setPartidosjugados(Integer.parseInt(p[2]));
                    e.setpartidosgandos(Integer.parseInt(p[3]));
                    e.setpartidosperdidos(Integer.parseInt(p[4]));
                    e.setPartidosempatados(Integer.parseInt(p[5]));
                    e.setGolesafavor(Integer.parseInt(p[6]));
                    e.setGolesencontra(Integer.parseInt(p[7]));
                    e.setPuntos(Integer.parseInt(p[8]));

                    boolean ok = dao.actualizarEstadisticasSQL(e);
                    return ok ? "OK|Estadísticas actualizadas" : "ERR|No se pudo actualizar";
                case "LISTLIGA": {
                    List<LigaDTO> lista = daoLigas.cargarSQL();
                    String payload = serializarListaLigas(lista);
                    return "OK|" + payload;
                }
                case "CREATELIGA": {
                    LigaDTO liga = new LigaDTO();
                    liga.setNombre(p[1]);
                    liga.setregion(p[2]);
                    int id = daoLigas.guardarLigasSQL(liga);
                    return (id > 0) ? "OK|ID=" + id : "ERR|No se pudo insertar";
                }
                case "LISTMATCHES": {
                    List<PartidosDTO> lista = daoPartidos.cargarSQL(dao);
                    String payload = serializarListaPartidos(lista);
                    return "OK|" + payload;
                }
                case "SIMULAR_TODOS": {
                    String taskId = TaskManager.get().submit(new SimulationAllTask());
                    return "OK|TASK|" + taskId;
                }
                case "SIMULAR_TODOS_SYNC": {
                    if (p.length < 2) return "ERR|Faltan parámetros (timeout)";
                    int timeout = Integer.parseInt(p[1]);
                    String taskId = TaskManager.get().submit(new SimulationAllTask());
                    java.util.concurrent.Future<?> f = TaskManager.get().getFuture(taskId);
                    try {
                        Object result = f.get(timeout, java.util.concurrent.TimeUnit.SECONDS);
                        if (result instanceof Integer) {
                            int n = (Integer) result;
                            TaskManager.get().remove(taskId);
                            return "OK|RESULT|" + n;
                        } else {
                            TaskManager.get().remove(taskId);
                            return "ERR|Resultado inválido";
                        }
                    } catch (Exception ex) {
                        f.cancel(true);
                        TaskManager.get().remove(taskId);
                        return "ERR|" + ex.getMessage();
                    }
                }
                case "CREATEPARTIDO": {
                    if (p.length < 10) {
                        return "ERR|Faltan parámetros (esperados: 10, recibidos: " + p.length + ")";
                    }

                    try {
                        int idLocal = Integer.parseInt(p[1]);
                        int idVisitante = Integer.parseInt(p[2]);
                        String jornada = p[3];
                        String estadio = p[4];
                        LocalDate fecha = LocalDate.parse(p[5]);
                        String estado = p[6];
                        int golesLocal = Integer.parseInt(p[7]);
                        int golesVisitante = Integer.parseInt(p[8]);
                        int idLiga = Integer.parseInt(p[9]);

                        // Cargar equipos desde BD
                        List<Equipos> todosEquipos = dao.cargarSQL();

                        Equipos local = todosEquipos.stream()
                                .filter(eq -> eq.getIdEquipo() == idLocal)
                                .findFirst()
                                .orElse(null);

                        Equipos visitante = todosEquipos.stream()
                                .filter(eq -> eq.getIdEquipo() == idVisitante)
                                .findFirst()
                                .orElse(null);

                        if (local == null || visitante == null) {
                            return "ERR|Equipos no encontrados (local=" + idLocal + ", visitante=" + idVisitante + ")";
                        }
                        List<PartidosDTO> partidosExistentes = daoPartidos.cargarSQL(dao);
                        boolean partidoDuplicado = partidosExistentes.stream()
                                .anyMatch(pt ->
                                        pt.getlocal().getIdEquipo() == idLocal &&
                                                pt.getvisitante().getIdEquipo() == idVisitante &&
                                                pt.jornadasProperty().get().equals(jornada) &&
                                                pt.getliga().get() == idLiga
                                );

                        if (partidoDuplicado) {
                            return "ERR|Ya existe un partido entre estos equipos en la misma jornada y liga";
                        }
                        for (PartidosDTO pt : partidosExistentes) {
                            if ((pt.getlocal().getIdEquipo() == idLocal || pt.getvisitante().getIdEquipo() == idLocal)
                                    && pt.getliga().get() != idLiga) {
                                return "ERR|El equipo local ya pertenece a otra liga diferente";
                            }
                            if ((pt.getlocal().getIdEquipo() == idVisitante || pt.getvisitante().getIdEquipo() == idVisitante)
                                    && pt.getliga().get() != idLiga) {
                                return "ERR|El equipo visitante ya pertenece a otra liga diferente";
                            }
                        }

                        // Crear partido
                        String nombrePartido = local.getNombre() + " vs " + visitante.getNombre();
                        PartidosDTO partido = new PartidosDTO(nombrePartido, local, visitante, jornada, "0", fecha, estadio);
                        partido.setGolesLocal(golesLocal);
                        partido.setGolesVisitante(golesVisitante);
                        partido.estadoProperty().set(estado);
                        daoPartidos.insertarSQL(partido, idLiga);
                        int id = Integer.parseInt(partido.idpartidoProperty().get());
                        return "OK|ID=" + id;

                    } catch (NumberFormatException x) {
                        return "ERR|Error en formato de números: " + x.getMessage();
                    } catch (Exception z) {
                        return "ERR|Error al crear partido: " + z.getMessage();
                    }

                }
                case "UPDATEPARTIDO": {
                    System.out.println("\n═══════════════════════════════════");
                    System.out.println("[DEBUG] UPDATEPARTIDO recibido");
                    System.out.println("[PARAMS] " + java.util.Arrays.toString(p));

                    if (p.length < 5) {
                        System.err.println("[ERROR] Faltan parámetros: " + p.length + "/5");
                        return "ERR|Faltan parámetros (esperados: 5, recibidos: " + p.length + ")";
                    }

                    try {
                        int idPartido = Integer.parseInt(p[1]);
                        String estado = p[2];
                        int golesLocal = Integer.parseInt(p[3]);
                        int golesVisitante = Integer.parseInt(p[4]);

                        System.out.println("[PARSE] ID:" + idPartido + " Estado:" + estado);
                        System.out.println("[GOLES] Local:" + golesLocal + " Visitante:" + golesVisitante);

                        // actualizar directamente la fila en la BD para evitar depender del cache en memoria
                        String sql = "UPDATE Partido SET estado = ?, goles_local = ?, goles_visitante = ? WHERE id = ?";
                        try (Connection con = ConnectionFactory.getConnection();
                             PreparedStatement ps = con.prepareStatement(sql)) {
                            ps.setString(1, estado);
                            ps.setInt(2, golesLocal);
                            ps.setInt(3, golesVisitante);
                            ps.setInt(4, idPartido);
                            int updated = ps.executeUpdate();
                            if (updated == 0) {
                                System.err.println("[ERROR] Partido no encontrado ID: " + idPartido);
                                return "ERR|Partido no encontrado (ID: " + idPartido + ")";
                            }
                        }

                        try {
                            for (PartidosDTO pDto : daoPartidos.getPartidos()) {
                                try {
                                    int pid = Integer.parseInt(pDto.idpartidoProperty().get());
                                    if (pid == idPartido) {
                                        pDto.estadoProperty().set(estado);
                                        pDto.setGolesLocal(golesLocal);
                                        pDto.setGolesVisitante(golesVisitante);
                                        break;
                                    }
                                } catch (NumberFormatException ignored) {}
                            }
                        } catch (Exception ignored) {}

                        System.out.println("[SUCCESS] Partido actualizado: " + idPartido);
                        System.out.println("[AFTER] Nuevo estado: " + estado + ", Goles: " + golesLocal + "-" + golesVisitante);
                        System.out.println("═══════════════════════════════════\n");
                        return "OK|Partido actualizado";

                    } catch (NumberFormatException m) {
                        System.err.println("[EXCEPTION] Error al parsear números: " + m.getMessage());
                        m.printStackTrace();
                        return "ERR|Error en formato de números: " + m.getMessage();
                    } catch (Exception d) {
                        System.err.println("[EXCEPTION] " + d.getMessage());
                        d.printStackTrace();
                        return "ERR|Error al actualizar partido: " + d.getMessage();
                    }
                }
                default:
                    return "ERR|Comando no soportado: " + cmd;
            }

        } catch (Exception ex) {
            return "ERR|" + ex.getMessage();
        }
    }

    private String serializarLista(List<Equipos> equipos) {
        // Cada equipo: id;nombre;ciudad;estadio;anio y separados por ||
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < equipos.size(); i++) {
            Equipos e = equipos.get(i);
            sb.append(e.getIdEquipo()).append(";")
                    .append(clean(e.getNombre())).append(";")
                    .append(clean(e.getCiudad())).append(";")
                    .append(clean(e.getEstadio())).append(";")
                    .append(e.getAnioFundacion());
            if (i < equipos.size() - 1) sb.append("||");
        }
        return sb.toString();
    }

    private String serializarListaPartidos(List<PartidosDTO> partidos) {
        // id;fecha;idLocal;nombreLocal;idVisitante;nombreVisitante;jornada;estadio;estado;golesLocal;golesVisitante;idLiga
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < partidos.size(); i++) {
            PartidosDTO p = partidos.get(i);
            sb.append(p.idpartidoProperty().get()).append(";")
                    .append(p.getfecha()).append(";")
                    .append(p.getlocal().getIdEquipo()).append(";")
                    .append(clean(p.getlocal().getNombre())).append(";")
                    .append(p.getvisitante().getIdEquipo()).append(";")
                    .append(clean(p.getvisitante().getNombre())).append(";")
                    .append(clean(p.jornadasProperty().get())).append(";")
                    .append(clean(p.estadioProperty().get())).append(";")
                    .append(clean(p.estadoProperty().get())).append(";")
                    .append(p.golesLocalProperty().get()).append(";")
                    .append(p.golesVisitanteProperty().get()).append(";")
                    .append(p.getliga().get());
            if (i < partidos.size() - 1) sb.append("||");
        }
        return sb.toString();
    }

    private String serializarListaLigas(List<LigaDTO> ligas) {
        // Cada liga: id;nombre;region
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ligas.size(); i++) {
            LigaDTO l = ligas.get(i);
            sb.append(l.getIdLiga()).append(";")
                    .append(clean(l.getNombre().get())).append(";")
                    .append(clean(l.getRegion()));
            if (i < ligas.size() - 1) sb.append("||");
        }
        return sb.toString();
    }

    private String clean(String s) {
        if (s == null) return "";
        return s.replace("|", " ").replace(";", " ").replace("||", " ");
    }
}
