package server;

import DTO.PartidosDTO;
import Data.DataEquipos;
import Data.DataPartidos;

import java.util.List;
import java.util.concurrent.Callable;

public class SimulationAllTask implements Callable<Integer> {
    @Override
    public Integer call() {
        int count = 0;
        try {
            DataPartidos dp = DataPartidos.getInstance();
            DataEquipos de = DataEquipos.getInstance();
            List<PartidosDTO> partidos = dp.cargarSQL(de);

            System.out.println("[SimulationAllTask] Partidos encontrados en BD: " + partidos.size());

            for (PartidosDTO p : partidos) {
                try {
                    boolean esPendiente = "Pendiente".equalsIgnoreCase(p.estadoProperty().get());
                    int golesLocal = p.golesLocalProperty().get();
                    int golesVisitante = p.golesVisitanteProperty().get();
                    int puntosLocal = p.getlocal().puntosProperty().get();
                    int puntosVisitante = p.getvisitante().puntosProperty().get();

                    boolean candidato = esPendiente || (golesLocal == 0 && golesVisitante == 0 && puntosLocal == 0 && puntosVisitante == 0);
                    if (!candidato) continue;

                    String idStr = p.idpartidoProperty().get();
                    System.out.println("[SimulationAllTask] Candidato a simular: id=" + idStr + ", estado=" + p.estadoProperty().get() + ", goles=" + golesLocal + "-" + golesVisitante + ", puntos=" + puntosLocal + "/" + puntosVisitante);

                    // reutilizar SimulationTask para simular y persistir
                    SimulationTask task = new SimulationTask(Integer.parseInt(idStr));
                    try {
                        PartidosDTO dto = task.call(); // simula y guarda
                        if (dto != null) {
                            count++;
                            System.out.println("[SimulationAllTask] Partido simulado correctamente: id=" + idStr + ", resultado=" + dto.golesLocalProperty().get() + "-" + dto.golesVisitanteProperty().get());
                        } else {
                            System.out.println("[SimulationAllTask] Partido no simulado (condición/ausente): id=" + idStr);
                        }
                    } catch (Exception ex) {
                        // registramos y continuamos
                        System.err.println("Error al simular partido id=" + idStr + ": " + ex.getMessage());
                    }
                } catch (Exception ex) {
                    System.err.println("Error procesando partido en lista: " + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            System.err.println("Error al cargar partidos para simulación: " + ex.getMessage());
            // devolver el conteo acumulado aunque haya fallado
        }
        System.out.println("[SimulationAllTask] Simulados totales: " + count);
        return count;
    }
}
