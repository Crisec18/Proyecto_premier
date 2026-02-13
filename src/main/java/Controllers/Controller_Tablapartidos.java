package Controllers;
import DTO.LigaDTO;
import DTO.PartidosDTO;
import Data.DataEquipos;
import Data.DataGestorLiga;
import Data.DataPartidos;
import Logic.LogicLigas;
import Logic.LogicPartidos;
import Logic.LogicaEquipo;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import client.PartidosServiceClientSocket;

public class Controller_Tablapartidos {

    @FXML private TableView<PartidosDTO> Tablapartidos;
    @FXML private TableColumn<PartidosDTO, String> Colid, Colpartido, Collocal, Colvisiatente, Colfecha, Colestado;
    @FXML private TableColumn<PartidosDTO, Number> Colpuntajelocal, Colpuntajevisitante;
    @FXML private ComboBox<String> comboliga;

    @FXML private Label lblJugados, lblPendientes, lblSimulables, lblFechasTotales, lblJornada;

    DataGestorLiga gestionliga = DataGestorLiga.getInstance();
    LogicLigas log = new LogicLigas(gestionliga);
    private LogicPartidos logicPartidos;

    @FXML
    public void initialize() {
        configurarColumnas();
        try {
            gestionliga.getLigas().setAll(log.cargarligas());
            DataPartidos dp = DataPartidos.getInstance();
            logicPartidos = new LogicPartidos(dp);
            DataEquipos dataEquipos = DataEquipos.getInstance();
            LogicaEquipo logicaEquipo = new LogicaEquipo(dataEquipos);
            dataEquipos.getEquipos().setAll(logicaEquipo.cargarEquipos());
            dp.getPartidos().setAll(logicPartidos.cargarpartidos());

            comboliga.getItems().clear();
            gestionliga.getLigas().forEach(l -> comboliga.getItems().add(l.nombreLigaProperty().get()));

            comboliga.getSelectionModel().selectedItemProperty().addListener((obs, ant, nuevo) -> {
                if (nuevo != null) {
                    actualizarInterfazPorLiga(nuevo);
                }
            });

            if (!comboliga.getItems().isEmpty()) {
                comboliga.getSelectionModel().selectFirst();
            }

        } catch (Exception e) {
            mostrarErrores("Error al cargar calendario", e);
        }
    }

    private void configurarColumnas() {
        Colid.setCellValueFactory(data -> data.getValue().idpartidoProperty());
        Colpartido.setCellValueFactory(data -> data.getValue().nombrepartidoProperty());
        Collocal.setCellValueFactory(data -> data.getValue().getlocal().nombreEquipoProperty());
        Colvisiatente.setCellValueFactory(data -> data.getValue().getvisitante().nombreEquipoProperty());
        Colfecha.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getfecha().toString()));
        Colpuntajelocal.setCellValueFactory(data -> data.getValue().golesLocalProperty());
        Colpuntajevisitante.setCellValueFactory(data -> data.getValue().golesVisitanteProperty());
        Colestado.setCellValueFactory(data -> data.getValue().estadoProperty());
    }

    private void actualizarInterfazPorLiga(String nombreLiga) {
        // Filtrar partidos desde DataPartidos (los que cargaste de BD)
        LigaDTO ligaSeleccionada = gestionliga.getLigas().stream()
                .filter(l -> l.nombreLigaProperty().get().equals(nombreLiga))
                .findFirst()
                .orElse(null);

        if (ligaSeleccionada != null) {
            int idLiga = Integer.parseInt(ligaSeleccionada.idLigaProperty().get());

            ObservableList<PartidosDTO> filtrados = DataPartidos.getInstance()
                    .getPartidos()
                    .filtered(p -> p != null && p.getliga().get() == idLiga);

            Tablapartidos.setItems(filtrados);
            actualizarContadores(ligaSeleccionada);
        }
    }

    @FXML
    void simularProximaJornada(ActionEvent event) {
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                try (PartidosServiceClientSocket client = new PartidosServiceClientSocket("127.0.0.1", 5050)) {
                    // Llamamos a la simulación
                    int simulados = client.simularTodosSync(60); //tiempo de espera ajustable
                    // Recargar partidos desde BD
                    var partidos = logicPartidos.cargarpartidos();
                    // Recargar equipos desde la BD local para actualizar puntos/goles
                    java.util.List<DTO.Equipos> equipos = null;
                    try {
                        equipos = DataEquipos.getInstance().cargarSQL();
                    } catch (Exception ex) {
                        System.err.println("No se pudo recargar equipos tras simulación: " + ex.getMessage());
                    }
                    List<DTO.Equipos> finalEquipos = equipos;
                    javafx.application.Platform.runLater(() -> {
                        DataPartidos.getInstance().getPartidos().setAll(partidos);
                        if (finalEquipos != null) {
                            DataEquipos.getInstance().getEquipos().setAll(finalEquipos);
                        }
                         // refrescar vista
                         if (comboliga.getSelectionModel().getSelectedItem() != null) {
                             actualizarInterfazPorLiga(comboliga.getSelectionModel().getSelectedItem());
                         } else {
                             Tablapartidos.refresh();
                         }
                         Alert a = new Alert(Alert.AlertType.INFORMATION);
                         a.setTitle("Simulación completada");
                         a.setHeaderText(null);
                         a.setContentText("Se simularon " + simulados + " partidos.");
                         a.showAndWait();
                     });
                } catch (IOException ioe) {
                    javafx.application.Platform.runLater(() -> mostrarErrores("Error de conexión al servidor", ioe));
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> mostrarErrores("Error durante la simulación", new Exception(ex)));
                }
                return null;
            }
        };

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            javafx.application.Platform.runLater(() -> mostrarErrores("Error en simulación", new Exception(ex)));
        });

        new Thread(task).start();
    }

    @FXML
    void mostrarTodosLosPartidos(ActionEvent event) {
        ObservableList<PartidosDTO> todos = gestionliga.getTodosLosPartidos();
        Tablapartidos.setItems(todos);
        comboliga.getSelectionModel().clearSelection();

        int partidosJugados = (int) todos.stream()
                .filter(p -> p.estadoProperty().get().equalsIgnoreCase("Finalizado"))
                .count();

        int totalPartidos = todos.size();
        int jornadasSimulables = 0;
        for (LigaDTO liga : gestionliga.getLigas()) {
            int idLiga = Integer.parseInt(liga.idLigaProperty().get());
            List<String> jornadas = obtenerJornadasRescate(liga, todos);
            for (String j : jornadas) {
                boolean tienePendientes = todos.stream()
                        .filter(p -> p.getliga().get() == idLiga &&
                                p.jornadasProperty().get().equals(j))
                        .anyMatch(p -> p.estadoProperty().get().equalsIgnoreCase("Pendiente"));
                if (tienePendientes) jornadasSimulables++;
            }
        }
        actualizarLabelsUI(partidosJugados, jornadasSimulables, totalPartidos);
    }

    public void actualizarContadores(LigaDTO liga) {
        ObservableList<PartidosDTO> partidosDeEstaLiga = Tablapartidos.getItems();
        List<String> listaJornadas = obtenerJornadasRescate(liga, partidosDeEstaLiga);

        int partidosJugados = (int) partidosDeEstaLiga.stream()
                .filter(p -> p.estadoProperty().get().equalsIgnoreCase("Finalizado"))
                .count();

        int jornadasSimulables = 0;
        for (String j : listaJornadas) {
            boolean tienePendientes = partidosDeEstaLiga.stream()
                    .filter(p -> p.jornadasProperty().get().trim().equalsIgnoreCase(j.trim()))
                    .anyMatch(p -> p.estadoProperty().get().equalsIgnoreCase("Pendiente"));
            if (tienePendientes) jornadasSimulables++;
        }

        actualizarLabelsUI(partidosJugados, jornadasSimulables, partidosDeEstaLiga.size());
    }
    //sin uso de momento
    private List<String> obtenerJornadasRescate(LigaDTO liga, List<PartidosDTO> fuente) {
        if (liga.getjornadas() != null && !liga.getjornadas().isEmpty()) return liga.getjornadas();
        int idLiga = Integer.parseInt(liga.idLigaProperty().get());
        return fuente.stream()
                .filter(p -> p.getliga().get()==idLiga)
                .map(p -> p.jornadasProperty().get())
                .distinct().sorted().toList();
    }

    private void actualizarLabelsUI(int j, int s, int t) {
        javafx.application.Platform.runLater(() -> {
            if (lblJugados != null) {
                lblJugados.setText(String.valueOf(j));
                lblPendientes.setText(String.valueOf(t - j));
                lblSimulables.setText(String.valueOf(s));
                lblFechasTotales.setText(String.valueOf(t));
            }
        });
    }

    @FXML
    void obtenerjornada(MouseEvent event) {
        PartidosDTO partidosDTO = Tablapartidos.getSelectionModel().getSelectedItem();
        if (partidosDTO == null) {
            return;
        }
        try {
            var lista = logicPartidos.cargarpartidos();
            String idSel = partidosDTO.idpartidoProperty().get();
            var opt = lista.stream().filter(x -> x.idpartidoProperty().get().equals(idSel)).findFirst();
            if (opt.isPresent()) {
                PartidosDTO actual = opt.get();
                // actualizar campos visibles
                partidosDTO.setGolesLocal(actual.golesLocalProperty().get());
                partidosDTO.setGolesVisitante(actual.golesVisitanteProperty().get());
                partidosDTO.estadoProperty().set(actual.estadoProperty().get());
                partidosDTO.setFecha(actual.getfecha());
                partidosDTO.setEstadio(actual.estadioProperty().get());
                partidosDTO.setJornada(actual.jornadasProperty().get());
            }
        } catch (Exception e) {
            // si falla la recarga, mostrar igual la jornada del objeto en memoria
        }
        lblJornada.setText(partidosDTO.jornadasProperty().get());
    }

    @FXML
    void filtrarpartidos(ActionEvent event) {
        String seleccion = comboliga.getSelectionModel().getSelectedItem();
        if (seleccion != null) {
            actualizarInterfazPorLiga(seleccion);
        }
    }


    @FXML void creacionscene(ActionEvent event) { cambiarEscena(event, "creacion_equipos.fxml"); }
    @FXML void crearPartiscene(ActionEvent event) { cambiarEscena(event, "Creacion_partidos.fxml"); }
    @FXML void crearligascene(ActionEvent event) { cambiarEscena(event, "Creacion_liga.fxml"); }
    @FXML void modificacionscene(ActionEvent event) { cambiarEscena(event, "modificacion_equipo.fxml"); }
    @FXML void registrarresultscene(ActionEvent event) { cambiarEscena(event, "registrar_resultados.fxml"); }
    @FXML void tablaposicionesscene(ActionEvent event) { cambiarEscena(event, "TablaPremier.fxml"); }

    private void cambiarEscena(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/practice_fx/proyecto_premier/" + fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void mostrarErrores(String titulo, Exception e) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setContentText(e.getMessage());
        alerta.showAndWait();
    }
}

