package Controllers;

import DTO.Equipos;
import DTO.LigaDTO;
import Data.DataGestorLiga;
import Logic.LogicLigas;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Controller_TablaGeneral {
    @FXML private ComboBox<String> ligacombo;
    @FXML private TableView<Equipos> tblLista;
    @FXML private TableColumn<Equipos, String> tblEquipo;
    @FXML private TableColumn<Equipos, String> txtID;
    @FXML private TableColumn<Equipos, Number> tblPos;
    @FXML private TableColumn<Equipos, Number> txtGanados;
    @FXML private TableColumn<Equipos, Number> txtEmpates;
    @FXML private TableColumn<Equipos, Number> txtPerdidos;
    @FXML private TableColumn<Equipos, Number> txtGolesAFavor;
    @FXML private TableColumn<Equipos, Number> tabGolesContra;
    @FXML private TextField txfBuscarTablaPremier;

    private final Path rutaLigas = Path.of("Data/ligas.xml");
    DataGestorLiga gestionliga = DataGestorLiga.getInstance(rutaLigas);
    LogicLigas log = new LogicLigas(gestionliga);

    private void configurarTabla() {
        txtID.setCellValueFactory(data -> data.getValue().idEquipoProperty());
        tblEquipo.setCellValueFactory(data -> data.getValue().nombreEquipoProperty());
        txtGanados.setCellValueFactory(data -> data.getValue().ganadosProperty());
        txtEmpates.setCellValueFactory(data -> data.getValue().empatesProperty());
        txtPerdidos.setCellValueFactory(data -> data.getValue().perdidosProperty());
        txtGolesAFavor.setCellValueFactory(data -> data.getValue().golesFavorProperty());
        tabGolesContra.setCellValueFactory(data -> data.getValue().golesContraProperty());
        tblPos.setCellValueFactory(data -> data.getValue().puntosProperty());
    }

    @FXML
    public void initialize() {
        configurarTabla();

        try {
            // 1. Cargar Ligas
            gestionliga.getLigas().setAll(log.cargarligas());

            // 2. Rellenar ComboBox
            ligacombo.getItems().clear();
            for (LigaDTO liga : gestionliga.getLigas()) {
                ligacombo.getItems().add(liga.nombreLigaProperty().get());
            }

            // 3. Listener para filtrar la tabla al cambiar de liga
            ligacombo.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevoNombre) -> {
                if (nuevoNombre != null) {
                    LigaDTO seleccionada = gestionliga.getLigas().stream()
                            .filter(l -> l.nombreLigaProperty().get().equals(nuevoNombre))
                            .findFirst().orElse(null);

                    if (seleccionada != null) {
                        gestionliga.filtrarPorLiga(seleccionada);
                        tblLista.sort();
                    }
                }
            });

            // 4. Seleccionar la primera por defecto
            if (!ligacombo.getItems().isEmpty()) {
                ligacombo.getSelectionModel().selectFirst();
            }

        } catch (Exception e) {
            mostrarErrores("Error al inicializar la tabla general", e);
        }

        tblLista.setItems(gestionliga.getEquiposFiltrados());

        // Ordenamiento por defecto (Puntos y Goles)
        tblPos.setSortType(TableColumn.SortType.DESCENDING);
        tblLista.getSortOrder().add(tblPos);

        if (!ligacombo.getItems().isEmpty()) {
            ligacombo.getSelectionModel().selectFirst();
        }
    }

    @FXML
    void buscar(ActionEvent event) {
        String ligaSeleccionada = ligacombo.getSelectionModel().getSelectedItem();
        if (ligaSeleccionada == null || ligaSeleccionada.isEmpty()) return;

        try {
            ObservableList<Equipos> equiposLiga = gestionliga.getEquiposPorLiga(ligaSeleccionada);
            tblLista.setItems(equiposLiga);
            tblLista.sort();
        } catch (Exception e) {
            mostrarErrores("Error al filtrar equipos", e);
        }
    }

    @FXML
    void filtrarequipos(ActionEvent event) {
        buscar(event); // Redirige la acción al método buscar que ya existe
    }

    // --- Navegación ---

    @FXML void CreacionEquiposcene(ActionEvent event) { cambiarEscena(event, "creacion_equipos.fxml"); }
    @FXML void CreacionLigascene(ActionEvent event) { cambiarEscena(event, "Creacion_liga.fxml"); }
    @FXML void ModificacionEquiposcene(ActionEvent event) { cambiarEscena(event, "modificacion_equipo.fxml"); }
    @FXML void TablaposicionesScenes(ActionEvent event) { cambiarEscena(event, "Creacion_partidos.fxml"); }
    @FXML void registratResultadoScene(ActionEvent event) { cambiarEscena(event, "registrar_resultados.fxml"); }
    @FXML void tablapartido(ActionEvent event) { cambiarEscena(event, "Tablapartidos.fxml"); }

    private void cambiarEscena(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/practice_fx/proyecto_premier/" + fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarErrores(String titulo, Exception e) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText("Ocurrió un problema...");
        alerta.setContentText(e.getMessage());
        alerta.showAndWait();
    }
}