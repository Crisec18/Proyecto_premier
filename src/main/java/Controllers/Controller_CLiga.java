package Controllers;

import DTO.LigaDTO;
import Data.DataEquipos;
import Data.DataGestorLiga;
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

public class Controller_CLiga {

    @FXML
    private TableColumn<LigaDTO, String> Colid;

    @FXML
    private TableColumn<LigaDTO, String> Colname;

    @FXML
    private TableColumn<LigaDTO, String> Colregion;

    @FXML
    private TextField txtnombreliga;

    @FXML
    private TextField txtregionliga;

    @FXML
    private TableView<LigaDTO> Tablaliga;

    @FXML
    void guardar(ActionEvent event) {
        try {
            boolean existe = DataGestorLiga.getInstance().getLigas().stream()
                    .anyMatch(Liga -> Liga.nombreLigaProperty().get().equals(txtnombreliga.getText()));
            if (existe) {
                mostrarErrores("Error de Existencia", new Exception(validarformulario()));
                return;

            }
            if (validarformulario() != null) {
                mostrarErrores("Error de validadcion", new Exception(validarformulario()));
                return;
            }
            DataGestorLiga.getInstance().agregarLiga(txtnombreliga.getText(),
                    txtregionliga.getText());
            Tablaliga.refresh();

        }catch (Exception e){
            mostrarErrores("Se produjo un error al guardar...", e);
        }
    }

    @FXML
    void limpiar(ActionEvent event) {
        limpiarformulario();

    }

    @FXML
    void tablaLigas(MouseEvent event) {
        return;
    }

    @FXML
    void initialize() {
        Colid.setCellValueFactory(data-> data.getValue().idLigaProperty());
        Colname.setCellValueFactory(data->data.getValue().nombreLigaProperty());
        Colregion.setCellValueFactory(data->data.getValue().regionLigaProperty());
        Tablaliga.setItems(DataGestorLiga.getInstance().getLigasfiltradas());
    }

    void limpiarformulario(){
        txtnombreliga.clear();
        txtregionliga.clear();
    }

    private String validarformulario(){
        if (txtnombreliga.getText().isEmpty() || txtnombreliga.getText().trim().length()<3){
            return "Nombre de la Liga no Valida";
        }
        if (txtregionliga.getText().isEmpty() || txtregionliga.getText().trim().length()<3){
            return "La region de la liga no Valida";
        }
        return null;
    }

    @FXML
    void CreacionEscene(ActionEvent event) {
        cambiarEscena(event, "creacion_equipos.fxml");
    }

    @FXML
    void CreacionPscene(ActionEvent event) {
        cambiarEscena(event,"Creacion_partidos.fxml");

    }
    @FXML
    void tablapartidoscene(ActionEvent event) {
        cambiarEscena(event,"Tablapartidos.fxml");
    }

    @FXML
    void ModificacionEscene(ActionEvent event) {
        cambiarEscena(event,"modificacion_equipo.fxml");
    }

    @FXML
    void RegistrarRscene(ActionEvent event) {
        cambiarEscena(event,"registrar_resultados.fxml");

    }

    @FXML
    void TablaposicionesScene(ActionEvent event) {
        cambiarEscena(event,"TablaPremier.fxml");

    }
    private void cambiarEscena(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/practice_fx/proyecto_premier/" + fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            mostrarErrores("Error al cambiar de escena", e);
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
