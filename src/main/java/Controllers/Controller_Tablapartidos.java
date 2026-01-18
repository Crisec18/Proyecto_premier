package Controllers;

import DTO.PartidosDTO;
import Data.DataGestorLiga;
import Data.DataPartidos;
import javafx.beans.property.SimpleStringProperty;
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
import java.time.LocalDate;

public class Controller_Tablapartidos {

    @FXML
    private TableColumn<PartidosDTO, String> Colestado;

    @FXML
    private TableColumn<PartidosDTO, String> Colfecha;

    @FXML
    private TableColumn<PartidosDTO, String> Colid;

    @FXML
    private TableColumn<PartidosDTO, String> Collocal;

    @FXML
    private TableColumn<PartidosDTO, String> Colpartido;

    @FXML
    private TableColumn<PartidosDTO, Number> Colpuntajelocal;

    @FXML
    private TableColumn<PartidosDTO, Number> Colpuntajevisitante;

    @FXML
    private TableColumn<PartidosDTO, String> Colvisiatente;

    @FXML
    private TableView<PartidosDTO> Tablapartidos;

    @FXML
    private ComboBox<String> comboliga;

    @FXML
    public void initialize() {
        Colid.setCellValueFactory(data -> data.getValue().idpartidoProperty());
        Colpartido.setCellValueFactory(data -> data.getValue().nombrepartidoProperty());
        Collocal.setCellValueFactory(data -> data.getValue().getlocal().nombreEquipoProperty());
        Colvisiatente.setCellValueFactory(data -> data.getValue().getvisitante().nombreEquipoProperty());
        Colfecha.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getfecha().toString())
        );
        Colpuntajelocal.setCellValueFactory(data -> data.getValue().getgoleslocal());
        Colpuntajevisitante.setCellValueFactory(data -> data.getValue().getgolesvisitante());
        Colestado.setCellValueFactory(data -> data.getValue().estadoProperty());
        try {
            DataGestorLiga.getInstance().getLigas().forEach(liga -> {
                comboliga.getItems().add(liga.nombreLigaProperty().get());
            });
        } catch (Exception e) {
            mostrarErrores("Error al cargar ligas", e);
        }
        Tablapartidos.setItems(DataGestorLiga.getInstance().getTodosLosPartidos());
    }

    @FXML
    void creacionscene(ActionEvent event) {
        cambiarEscena(event, "creacion_equipos.fxml");

    }

    @FXML
    void crearPartiscene(ActionEvent event) {
        cambiarEscena(event, "Creacion_partidos.fxml");

    }

    @FXML
    void crearligascene(ActionEvent event) {
        cambiarEscena(event,"Creacion_liga.fxml");

    }

    @FXML
    void filtrarpartidos(ActionEvent event) {
        String ligaSeleccionada = comboliga.getSelectionModel().getSelectedItem();

        if (ligaSeleccionada == null || ligaSeleccionada.isEmpty() || ligaSeleccionada.equals("Todas")) {
            Tablapartidos.setItems(DataGestorLiga.getInstance().getTodosLosPartidos());
            return;
        }

        try {
            ObservableList<PartidosDTO> partidosLiga = DataGestorLiga.getInstance().getPartidosPorLiga(ligaSeleccionada);
            Tablapartidos.setItems(partidosLiga);

        } catch (Exception e) {
            mostrarErrores("Error al filtrar partidos", e);
        }


    }

    @FXML
    void modificacionscene(ActionEvent event) {
        cambiarEscena(event, "modificacion_equipo.fxml");

    }

    @FXML
    void registrarresultscene(ActionEvent event) {
        cambiarEscena(event, "registrar_resultados.fxml");

    }

    @FXML
    void tablaposicionesscene(ActionEvent event) {
        cambiarEscena(event, "TablaPremier.fxml");

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