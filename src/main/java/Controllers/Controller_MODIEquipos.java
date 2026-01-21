package Controllers;

import DTO.Equipos;
import Data.DataEquipos;
import Data.DataGestorLiga;
import Data.Datasingleton;
import Logic.LogicLigas;
import Logic.LogicaEquipo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public class Controller_MODIEquipos {

    @FXML
    private TableColumn<Equipos, String> ColName;

    @FXML
    private TableColumn<Equipos, String> ColEstadio;

    @FXML
    private TableColumn<Equipos, String> ColID;

    @FXML
    private TableColumn<Equipos, String> Colciudad;

    @FXML
    private TextField Nombretxt;

    @FXML
    private TableView<Equipos> TableTeams;

    @FXML
    private TextField ciudadtxt;

    @FXML
    private TextField estadiotxt;

    @FXML
    private TextField idequipo;

    @FXML
    private TextField aniofundaciontxt;

    private final DataEquipos datosEquipos = DataEquipos.getInstance(null);
    private final LogicaEquipo loq = new LogicaEquipo(datosEquipos);
    private final DataGestorLiga datosliga = DataGestorLiga.getInstance(Path.of("Data/ligas.xml"));
    private final LogicLigas logicaLiga = new LogicLigas(datosliga);


    @FXML
    void actualizar(ActionEvent event) {
        try {
            Equipos seleccionado = TableTeams.getSelectionModel().getSelectedItem();
            if (seleccionado == null) {
                mostrarErrores("Error de validadcion", new Exception(validarformulario()));
                return;
            }
            if(datosliga.equipoTienePartidos(seleccionado)){
                mostrarErrores("Error de validadacion", new Exception("error de modificacion Equipo asignado a alguna liga"));
                return;
            }
            if (validarformulario() != null) {
                mostrarErrores("Error de validacion",new Exception(validarformulario()) );
                return;
            }
            seleccionado.setNombre(Nombretxt.getText());
            seleccionado.setEstadio(estadiotxt.getText());
            seleccionado.setCiudad(ciudadtxt.getText());
            TableTeams.refresh();
            limpiarformulario();
        } catch (Exception e) {
            mostrarErrores("Se produjo un error al actualizar...", e);
        }

    }
    @FXML
    public void initialize() {

        ColID.setCellValueFactory(data-> data.getValue().idEquipoProperty());
        ColName.setCellValueFactory(data->data.getValue().nombreEquipoProperty());
        ColEstadio.setCellValueFactory(data->data.getValue().estadioEquipoProperty());
        Colciudad.setCellValueFactory(data->data.getValue().ciudadEquipoProperty());
        TableTeams.setItems(DataEquipos.getInstance(null).getEquiposfiltrados());
    }

    @FXML
    void eliminar(ActionEvent event) throws Exception {
            Equipos seleccionado = TableTeams.getSelectionModel().getSelectedItem();
            if(seleccionado== null){
                mostrarErrores("Error de validadcion", new Exception("No hay ningun equipo seleccionado"));
                return;
            }
            if(datosliga.equipoTienePartidos(seleccionado)){
                mostrarErrores("Error de validadcion", new Exception("El equipo tiene partidos asignados en alguna liga"));
                return;
            }
            DataEquipos.getInstance(null).getEquipos().remove(seleccionado);
            limpiar(event);
            TableTeams.refresh();
            loq.guardar(DataEquipos.getInstance(Path.of("Data/equipos.xml")).getEquipos());
    }

    @FXML
    void guardar(ActionEvent event) {

    }

    @FXML
    void limpiar(ActionEvent event) {
        limpiarformulario();
    }
    private String validarformulario(){
        if(DataEquipos.getInstance(null).getEquipos().isEmpty()){
            return "No hay equipos registrados";
        }
        if (Nombretxt.getText() == null || Nombretxt.getText().trim().length()<3) {
            return "Nombre invalido";
        }
        if(estadiotxt.getText() == null || estadiotxt.getText().trim().length()<3){
            return "Estadio invalido";
        }
        if (ciudadtxt.getText()== null || ciudadtxt.getText().trim().length()<3){
            return "Ciudad invalida";
        }
        return null;
    }

    private void limpiarformulario(){
        idequipo.clear();
        Nombretxt.clear();
        estadiotxt.clear();
        ciudadtxt.clear();
    }
    @FXML
    public void buscar(ActionEvent actionEvent) {
        String busquedatxt = idequipo.getText().toLowerCase();
        try{
            DataEquipos.getInstance(null).getEquiposfiltrados().setPredicate(p->{
                if(busquedatxt.isEmpty()) return true;
                return (p.idEquipoProperty().get().toLowerCase().contains(busquedatxt));


            });
        }
        catch (Exception e) {
            mostrarErrores("Se produjo un error al buscar...", e);
        }
    }


    public void obtenerequipo(javafx.scene.input.MouseEvent mouseEvent) {
        Equipos seleccionado = TableTeams.getSelectionModel().getSelectedItem();
        if(seleccionado== null){
            return;
        }
        Nombretxt.setText(seleccionado.nombreEquipoProperty().get());
        estadiotxt.setText(seleccionado.estadioEquipoProperty().get());
        ciudadtxt.setText(seleccionado.ciudadEquipoProperty().get());
    }

    @FXML
    void CreacionEscene(ActionEvent event) {
        cambiarEscena(event, "creacion_equipos.fxml");
    }

    @FXML
    void CreacionLigaScene(ActionEvent event) {
        cambiarEscena(event,"Creacion_liga.fxml");
    }

    @FXML
    void CreacionPscene(ActionEvent event) {
        cambiarEscena(event,"Creacion_partidos.fxml");
    }

    @FXML
    void RegistrarResultadoScene(ActionEvent event) {
        cambiarEscena(event,"registrar_resultados.fxml");
    }

    @FXML
    void TablaposicionesScene(ActionEvent event) {
        cambiarEscena(event, "TablaPremier.fxml");
    }

    @FXML
    void Tabladepartidosscene(ActionEvent event) {
        cambiarEscena(event, "Tablapartidos.fxml");
    }

    private void cambiarEscena(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/practice_fx/proyecto_premier/" + fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            mostrarErrores("Error al cambiar de escena...", e);
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

