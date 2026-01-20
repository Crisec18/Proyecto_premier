package Controllers;

import DTO.Equipos;
import Data.DataEquipos;
import Data.DataGestorLiga;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller_TablaGeneral {
    @FXML
    private ComboBox<String> ligacombo;
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

    @FXML
    public void initialize() {
        txtID.setCellValueFactory(data -> data.getValue().idEquipoProperty());
        tblEquipo.setCellValueFactory(data -> data.getValue().nombreEquipoProperty());
        txtGanados.setCellValueFactory(data -> data.getValue().ganadosProperty());
        txtEmpates.setCellValueFactory(data -> data.getValue().empatesProperty());
        txtPerdidos.setCellValueFactory(data -> data.getValue().perdidosProperty());
        txtGolesAFavor.setCellValueFactory(data -> data.getValue().golesFavorProperty());
        tabGolesContra.setCellValueFactory(data -> data.getValue().golesContraProperty());
        tblPos.setCellValueFactory(data -> data.getValue().puntosProperty());
        try {
            DataGestorLiga.getInstance().getLigas().forEach(liga -> {
                ligacombo.getItems().add(liga.nombreLigaProperty().get());
            });
        } catch (Exception e) {
            mostrarErrores("Error al cargar ligas", e);
        }

        tblLista.setItems(DataGestorLiga.getInstance().getEquiposFiltrados());



        // Configurar el ordenamiento de la tabla
        tblPos.setSortType(TableColumn.SortType.DESCENDING);
        txtGolesAFavor.setSortType(TableColumn.SortType.DESCENDING);
        tblLista.getSortOrder().setAll(tblPos, txtGolesAFavor);
    }


    @FXML
    void buscar(ActionEvent event) {
            String ligaSeleccionada = ligacombo.getSelectionModel().getSelectedItem();

            if (ligaSeleccionada == null || ligaSeleccionada.isEmpty()) {
                tblLista.setItems(DataGestorLiga.getInstance().getEquiposFiltrados());
                return;
            }

            try {
                ObservableList<Equipos> equiposLiga = DataGestorLiga.getInstance().getEquiposPorLiga(ligaSeleccionada);

                tblLista.setItems(equiposLiga);
                tblLista.getSortOrder().setAll(tblPos, txtGolesAFavor);
                tblLista.sort();

            } catch (Exception e) {
                mostrarErrores("Error al filtrar equipos", e);
            }

    }


    @FXML
    void CreacionEquiposcene(ActionEvent event) {
        cambiarEscena(event, "creacion_equipos.fxml");
    }

    @FXML
    void CreacionLigascene(ActionEvent event) {
        cambiarEscena(event, "Creacion_liga.fxml");
    }

    @FXML
    void ModificacionEquiposcene(ActionEvent event) {
        cambiarEscena(event,"modificacion_equipo.fxml");

    }

    @FXML
    void TablaposicionesScenes(ActionEvent event) {
        cambiarEscena(event, "Creacion_partidos.fxml");

    }
    @FXML
    void registratResultadoScene(ActionEvent event) {
        cambiarEscena(event, "registrar_resultados.fxml");

    }
    @FXML
    void tablapartido(ActionEvent event) {
        cambiarEscena(event,"Tablapartidos.fxml");

    }

    private void cambiarEscena(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/practice_fx/proyecto_premier/" + fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void filtrarequipos(ActionEvent event) {
        String ligaSeleccionada = ligacombo.getSelectionModel().getSelectedItem();
        if (ligaSeleccionada == null || ligaSeleccionada.isEmpty()) {
            tblLista.setItems(null);
            return;
        }
        try {
            ObservableList<Equipos> equiposLiga = DataGestorLiga.getInstance().getEquiposPorLiga(ligaSeleccionada);
            tblLista.setItems(equiposLiga);
            tblLista.getSortOrder().setAll(tblPos, txtGolesAFavor);
            tblLista.sort();
        } catch (Exception e) {
            mostrarErrores("Error al filtrar equipos", e);
        }

    }
    //para manejar las ventanas de errores
    private void mostrarErrores(String titulo, Exception e){
        //ventana de error
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText("Ocurrio un problema...");
        alerta.setContentText(e.getMessage() != null ? e.getMessage() : "Error inesperado...");
        TextArea detalle = new TextArea();
        detalle.setEditable(false);
        detalle.setWrapText(true);

        //detalle del error
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n\n");
        for(StackTraceElement ste : e.getStackTrace()){
            sb.append("en: ").append(ste.toString()).append("\n");
        }

        //muestra el detalle del error
        detalle.setText(sb.toString());
        TitledPane tb = new TitledPane("Detalle tecnico", detalle);
        tb.setExpanded(false);

        VBox contenido = new VBox(10, new Label("Se encotro un error, verifique el detalle..."), tb);
        contenido.setMaxWidth(Double.MAX_VALUE);

        //mostrar alerta
        alerta.getDialogPane().setExpandableContent(contenido);
        alerta.getDialogPane().setExpanded(false);

        alerta.showAndWait();
    }

}