package Controllers;

import DTO.Equipos;
import DTO.LigaDTO;
import DTO.PartidosDTO;
import Data.DataGestorLiga;
import Data.DataPartidos;
import Logic.LogicLigas;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class Controller_Tablapartidos {

    @FXML private TableView<PartidosDTO> Tablapartidos;
    @FXML private TableColumn<PartidosDTO, String> Colid, Colpartido, Collocal, Colvisiatente, Colfecha, Colestado;
    @FXML private TableColumn<PartidosDTO, Number> Colpuntajelocal, Colpuntajevisitante;
    @FXML private ComboBox<String> comboliga;

    @FXML private Label lblJugados, lblPendientes, lblSimulables, lblFechasTotales, lblJornada;

    private final Path rutaLigas = Path.of("Data/ligas.xml");
    private final Path rutaPartidos = Path.of("Data/partidos.xml");
    DataGestorLiga gestionliga = DataGestorLiga.getInstance(rutaLigas);
    LogicLigas log = new LogicLigas(gestionliga);

    @FXML
    public void initialize() {
        configurarColumnas();
        try {
            gestionliga.getLigas().setAll(log.cargarligas());
            DataPartidos dp = DataPartidos.getInstance(rutaPartidos);
            dp.getPartidos().setAll(dp.cargar());

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
        Colpuntajelocal.setCellValueFactory(data -> data.getValue().getgoleslocal());
        Colpuntajevisitante.setCellValueFactory(data -> data.getValue().getgolesvisitante());
        Colestado.setCellValueFactory(data -> data.getValue().estadoProperty());
    }

    private void actualizarInterfazPorLiga(String nombreLiga) {
        ObservableList<PartidosDTO> filtrados = gestionliga.getPartidosPorLiga(nombreLiga);
        Tablapartidos.setItems(filtrados);

        gestionliga.getLigas().stream()
                .filter(l -> l.nombreLigaProperty().get().equals(nombreLiga))
                .findFirst()
                .ifPresent(this::actualizarContadores);
    }
    //de momento no hace nada
    @FXML
    void simularProximaJornada(ActionEvent event) {
        String seleccion = comboliga.getSelectionModel().getSelectedItem();
        ObservableList<PartidosDTO> partidosActuales = Tablapartidos.getItems();

        Optional<String> jornadaASimular = partidosActuales.stream()
                .filter(p -> p.estadoProperty().get().equalsIgnoreCase("Pendiente"))
                .map(p -> p.jornadasProperty().get())
                .findFirst();

        if (jornadaASimular.isPresent()) {
            String targetJornada = jornadaASimular.get();

            partidosActuales.stream()
                    .filter(p -> p.jornadasProperty().get().equals(targetJornada) &&
                            p.estadoProperty().get().equalsIgnoreCase("Pendiente"))
                    .forEach(p -> {
                        p.getgoleslocal().set((int) (Math.random() * 5));
                        p.getgolesvisitante().set((int) (Math.random() * 5));
                        p.estadoProperty().set("Finalizado");
                    });

            Tablapartidos.refresh();

            if (seleccion != null) {
                actualizarInterfazPorLiga(seleccion);
            } else {
                mostrarTodosLosPartidos(null);
            }
        }
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
            List<String> jornadas = obtenerJornadasRescate(liga, todos);
            for (String j : jornadas) {
                boolean tienePendientes = todos.stream()
                        .filter(p -> p.getliga().get().equals(liga.nombreLigaProperty().get()) &&
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
        return fuente.stream()
                .filter(p -> p.getliga().get().equals(liga.nombreLigaProperty().get()))
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