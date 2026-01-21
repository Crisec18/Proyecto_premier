package Controllers;

import DTO.Equipos;
import Data.DataEquipos;
import Data.DataGestorLiga;
import Logic.LogicLigas;
import Logic.LogicaEquipo;
import Data.Datasingleton;
import Logic.LogicaEquipo;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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
import java.util.concurrent.atomic.AtomicInteger;

public class Controller_CEquipos {

    @FXML
    private TableColumn<Equipos, String> ColID;

    @FXML
    private TableView<Equipos> TableTeams;

    @FXML
    private TableColumn<Equipos, String> colAnnio;

    @FXML
    private TableColumn<Equipos, String> Colciudad;

    @FXML
    private TableColumn<Equipos, String> Colestadio;

    @FXML
    private TableColumn<Equipos, String> Colname;

    @FXML
    private TextField ciudadtextfield;

    @FXML
    private TextField estadiotextfield;

    @FXML
    private TextField nombretxtfield;

    @FXML
    private DatePicker fechacreacion;

    private final DataEquipos datosEquipos = DataEquipos.getInstance(null);
    private final LogicaEquipo loq = new LogicaEquipo(datosEquipos);;
    private final DataGestorLiga datosliga = DataGestorLiga.getInstance(Path.of("Data/ligas.xml"));
    private final LogicLigas logicaLiga = new LogicLigas(datosliga);

    @FXML
    public void initialize() {
        ColID.setCellValueFactory(data-> data.getValue().idEquipoProperty());
        Colname.setCellValueFactory(data->data.getValue().nombreEquipoProperty());
        Colestadio.setCellValueFactory(data->data.getValue().estadioEquipoProperty());
        Colciudad.setCellValueFactory(data->data.getValue().ciudadEquipoProperty());
        colAnnio.setCellValueFactory(data ->
                new SimpleStringProperty(
                        String.valueOf(data.getValue().getAnnio().getYear())
                )
        );
        //se estaba dejando votado el annioDeFundacion

        TableTeams.setItems(datosEquipos.getEquipos());
        cargar();
    }

//aplicar lo de los errores
    @FXML
    void guardar(ActionEvent event) {
        try {
            boolean existe = DataEquipos.getInstance(Path.of("Data/equipos.xml")).getEquipos().stream()
                    .anyMatch(equipo
                            -> equipo.nombreEquipoProperty().get().equals(nombretxtfield.getText()));
            if (existe) {
                mostrarErrores("Error de Existencia", new Exception("Equipo existente"));
                return;
            }
            if (validarformulario() != null) {
                mostrarErrores("Error de validacion", new Exception(validarformulario()));
                return;
            }
            datosEquipos.agregarequipo(
                    nombretxtfield.getText(),
                    estadiotextfield.getText(),
                    ciudadtextfield.getText(),
                    fechacreacion.getValue());



            // Se guarda el estado actual en XML
            try {
                loq.guardar(datosEquipos.getEquipos());
                TableTeams.refresh();
            }catch (Exception e){
            mostrarErrores("Error al guardar equipos", e);}
            limpiarformulario();
        } catch (Exception e) {
            mostrarErrores("Se produjo un error general al guardar...", e);
        }

    }
//por revisar
@FXML
void eliminar(ActionEvent event) {
    Equipos seleccionado = TableTeams.getSelectionModel().getSelectedItem();

    if (seleccionado == null) {
        mostrarErrores("Eliminar", new Exception("Debe seleccionar un equipo"));
        return;
    }
    datosEquipos.getEquipos().remove(seleccionado);
    try {
        loq.guardar(datosEquipos.getEquipos());
    } catch (Exception e) {
        mostrarErrores("Error al guardar cambios", e);
    }
    TableTeams.refresh();
    limpiarformulario();
}

    private void cargar(){
        try {
            datosEquipos.getEquipos().setAll(loq.cargarEquipos());
            datosEquipos.actualizarContadorId();
        } catch (Exception e) {
            mostrarErrores("Error al cargar equipos", e);
        }
    }

    @FXML
    void limpiar(ActionEvent event) {
        limpiarformulario();
    }



    private String validarformulario(){
        if (nombretxtfield.getText() == null || nombretxtfield.getText().trim().length()<3) {
            return "Nombre invalido";
        }
        if(ciudadtextfield.getText() == null || ciudadtextfield.getText().trim().length()<3){
            return "Ciudad invalida";
        }
        if (estadiotextfield.getText()== null || estadiotextfield.getText().trim().length()<3){
            return "Estadio invalido";
        }
        if(fechacreacion.getValue() == null){
            return "Año de fundacion invalido";
        }
        try{
            //validacion de años para que no se pase madre
            int a = fechacreacion.getValue().getYear();
            if(a < 1800 || a > 2026){
                return "Año de fundacion fuera de rango (1800-2026)";
            }
        }
        catch (NumberFormatException e) {
            return "Año de fundacion debe ser un numero";
        }
        return null;
    }

    private void limpiarformulario(){
        nombretxtfield.clear();
        estadiotextfield.clear();
        ciudadtextfield.clear();
        fechacreacion.setValue(null);
    }

    @FXML
    void Creacionpscene(ActionEvent event) {
        cambiarEscena(event,"Creacion_partidos.fxml");
    }

    @FXML
    void Modificacioscene(ActionEvent event) {
        cambiarEscena(event, "modificacion_equipo.fxml");
    }

    @FXML
    void Posicionesscene(ActionEvent event) {
        cambiarEscena(event, "TablaPremier.fxml");
    }

    @FXML
    void resultadoScene(ActionEvent event) {
         cambiarEscena(event, "registrar_resultados.fxml");
    }

    @FXML
    void ligascene(ActionEvent event) {
        cambiarEscena(event, "Creacion_liga.fxml");
    }
    @FXML
    void tablapartidoscene(ActionEvent event) {
        cambiarEscena(event, "Tablapartidos.fxml");
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
        for (StackTraceElement ste : e.getStackTrace()) {
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

