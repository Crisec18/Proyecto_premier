package Controllers;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import DTO.Equipos;
import DTO.LigaDTO;
import DTO.PartidosDTO;
import Data.DataEquipos;
import Data.DataGestorLiga;
import Data.DataPartidos;
import Logic.LogicLigas;
import Logic.LogicPartidos;
import Logic.LogicaEquipo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Controller_CPartidos {

    @FXML
    private TableColumn<Equipos, String> ColName;

    @FXML
    private TableColumn<Equipos, String> Colciudad;

    @FXML
    private TableColumn<Equipos, String> Colid;
    @FXML
    private TableColumn<Equipos, String> Colidvisit;

    @FXML
    private TableColumn<Equipos, String> Colnombrevisit;

    @FXML
    private TableView<Equipos> TableTeams;

    @FXML
    private TableView<Equipos> TableVisitante;

    @FXML
    private TextField buscartextfield;

    @FXML
    private Label ciudadvisitante;

    @FXML
    private ComboBox<String> estadiocombo;

    @FXML
    private ComboBox<String> jornadacombo;

    @FXML
    private ComboBox<String> Ligacombo;

    @FXML
    private Label localciudad;

    @FXML
    private Label localname;

    @FXML
    private Label nombrepartido;

    @FXML
    private Label visitantename;

    @FXML
    private TextField bsucartxtfieldvisit;

    @FXML
    private DatePicker fechapicker;


    @FXML
    void Limpiar(ActionEvent event) {
            limpiarformulario();
    }

    @FXML
    private Label locallabel;

    @FXML
    private Label visitlabel;

    private final DataPartidos datosPartidos= DataPartidos.getInstance(Path.of("Data/partidos.xml"));
    private final LogicPartidos loq = new LogicPartidos(datosPartidos);
    private final DataEquipos dataEquipos = DataEquipos.getInstance(Path.of("Data/equipos.xml"));
    private final LogicaEquipo logicaEquipo = new LogicaEquipo(dataEquipos);
    private final DataGestorLiga dataGestorLiga = DataGestorLiga.getInstance(Path.of("Data/ligas.xml"));
    private final LogicLigas loqligas = new LogicLigas(dataGestorLiga);

    @FXML
    void buscar(ActionEvent event) {
        String busquedatxt = buscartextfield.getText().toLowerCase();
        try{
            DataEquipos.getInstance(Path.of("Data/ligas.xml")).getEquiposfiltrados().setPredicate(p->{
                if(busquedatxt.isEmpty()) return true;
                return (p.idEquipoProperty().get().toLowerCase().contains(busquedatxt) || p.nombreEquipoProperty().get().toLowerCase().contains(busquedatxt)
                        || p.ciudadEquipoProperty().get().toLowerCase().contains(busquedatxt));


            });
        }
        catch (Exception e) {
            mostrarErrores("Error al buscar local...", e);
        }

    }
    @FXML
    void buscarvisitante(ActionEvent event) {
        String busquedatxt = bsucartxtfieldvisit.getText().toLowerCase();
        try{
            DataEquipos.getInstance(null).getFiltradovisitante().setPredicate(p->{
                if(busquedatxt.isEmpty()) return true;
                return (p.idEquipoProperty().get().toLowerCase().contains(busquedatxt) || p.nombreEquipoProperty().get().toLowerCase().contains(busquedatxt)
                        || p.ciudadEquipoProperty().get().toLowerCase().contains(busquedatxt));


            });
        }
        catch (Exception e) {
            mostrarErrores("Error al buscar visitante...", e);
        }
    }

    @FXML
    void guardar(ActionEvent event) {
        try {

            if (validarformulario() != null) {
                mostrarErrores("Error de validación", new Exception(validarformulario()));
                return;
            }
            Equipos local = TableTeams.getSelectionModel().getSelectedItem();
            Equipos visitante = TableVisitante.getSelectionModel().getSelectedItem();

            if(local!= null  &&  visitante != null){
                if(local.idEquipoProperty().get().equals(visitante.idEquipoProperty().get())){
                    mostrarErrores("Error de seleccion",new Exception(validarformulario()));
                    return;
                }
            }else{
                mostrarErrores("Error de seleccion",new Exception(validarformulario()));
                return;
            }


            if (local.jugadosProperty().get() >=3){
                mostrarErrores("Error de equipo local", new Exception("El equipo local ya ha jugado el maximo de partidos"));
                return;
            }
            if (visitante.jugadosProperty().get() >= 3){
                mostrarErrores("Error de equipo visitante", new Exception("El equipo visitante ya ha jugado el maximo de partidos"));
                return;
            }
            if (validarformulario() != null) {
                mostrarErrores("Error de validacion", new Exception(validarformulario()));
                return;
            }
            String nombreLiga = Ligacombo.getSelectionModel().getSelectedItem();
            String jornada = jornadacombo.getSelectionModel().getSelectedItem();
            boolean localYaEnLiga = dataGestorLiga.equipoYaEnLiga(local, nombreLiga);
            boolean visitanteYaEnLiga = dataGestorLiga.equipoYaEnLiga(visitante, nombreLiga);

            if (!localYaEnLiga) {
                if (dataGestorLiga.equipoEnOtraLiga(local, nombreLiga, datosPartidos)) {
                    mostrarErrores("Error de ligas", new Exception("El equipo local ya pertenece a otra liga diferente"));
                    return;
                }
            }

            if (!visitanteYaEnLiga) {
                if (dataGestorLiga.equipoEnOtraLiga(visitante, nombreLiga, datosPartidos)) {
                    mostrarErrores("Error de ligas", new Exception("El equipo visitante ya pertenece a otra liga diferente"));
                    return;
                }
            }

            if (dataGestorLiga.verificarcamposequipos(nombreLiga)){
                mostrarErrores("Error de ligas", new Exception("Liga llena (Espacio para equipos limitado)"));
                return;
            }
            if(dataGestorLiga.verificarcampospartido(nombreLiga)){
                mostrarErrores("Error de ligas", new Exception("Liga llena (Espacio para partidos limitado)"));
                return;
            }
            LigaDTO liga1 = DataGestorLiga.getInstance(Path.of("Data/ligas.xml")).getLigas().stream()
                    .filter(l -> l.nombreLigaProperty().get().equals(nombreLiga))
                    .findFirst()
                    .orElse(null);
            if(liga1.getLigatamano() >= 10){
                mostrarErrores("Error de liga", new Exception("Liga llena"));
                return;
            }
            // refactorizar esto para el proximo entregable
            LocalDate fechaSeleccionada = fechapicker.getValue();
            if (fechaSeleccionada != null) {
                int anioSeleccionado = fechaSeleccionada.getYear();
                int anioActual = LocalDate.now().getYear();

                if (anioSeleccionado < 1980) {
                    mostrarErrores("Error de fecha", new Exception("La fecha no puede ser anterior al año 1980"));
                    return;
                }
                if (anioSeleccionado > anioActual) {
                    mostrarErrores("Error de fecha", new Exception("La fecha no puede ser posterior al año " + anioActual));
                    return;
                }
            }

            String partidonombre = local.nombreEquipoProperty().get() + " vs " + visitante.nombreEquipoProperty().get();
            boolean partidoDuplicado = datosPartidos.getPartidos().stream()
                    .anyMatch(p ->
                            p.getlocal().idEquipoProperty().get().equals(local.idEquipoProperty().get()) &&
                                    p.getvisitante().idEquipoProperty().get().equals(visitante.idEquipoProperty().get()) &&
                                    p.getJornadas().get().equals(jornada) &&
                                    p.getliga().get().equals(nombreLiga)
                    );
            if (partidoDuplicado) {
                mostrarErrores("Partido duplicado", new Exception("Ya existe un partido entre estos equipos en la misma jornada y liga"));
                return;
            }

            PartidosDTO nuevoPartido = datosPartidos.agregarPartido(
                    partidonombre,
                    local,
                    visitante,
                    jornadacombo.getSelectionModel().getSelectedItem(),
                    estadiocombo.getSelectionModel().getSelectedItem(),
                    fechapicker.getValue());


            nuevoPartido.setliga(nombreLiga);
            nuevoPartido.getlocal().setPartidosjugados(nuevoPartido.getlocal().jugadosProperty().get() + 1);
            nuevoPartido.getvisitante().setPartidosjugados(nuevoPartido.getvisitante().jugadosProperty().get() + 1);

            for (LigaDTO liga : DataGestorLiga.getInstance(Path.of("Data/ligas.xml")).getLigas()) {
                if (liga.nombreLigaProperty().get().equals(nombreLiga)) {

                    if (!liga.getequipos().contains(local)) {
                        liga.getequipos().add(local);
                        liga.aumentarcapacidadliga();
                    }
                    if (!liga.getequipos().contains(visitante)) {
                        liga.getequipos().add(visitante);
                        liga.aumentarcapacidadliga();
                    }
                    liga.getpartidos().add(nuevoPartido);

                    break;
                }
            }
                try {
                    loq.guardar(datosPartidos.getPartidos());
                    logicaEquipo.guardar(dataEquipos.getEquipos());

                } catch (Exception e) {
                    mostrarErrores("Error al guardar partidos en XML", e);
                }

            limpiarformulario();
        }catch (Exception e){
            mostrarErrores("Error al guardar el partido", new Exception(validarformulario()) );
        }
    }

    void limpiarformulario(){
        locallabel.setText(". . . . . .");
        visitlabel.setText(". . . . . .");
        localname.setText("Equipo#1");
        localciudad.setText(". . . . . . . .");
        ciudadvisitante.setText(". . . . . . . .");
        visitantename.setText("Equipo#2");
        estadiocombo.getSelectionModel().clearSelection();
        jornadacombo.getSelectionModel().clearSelection();
        Ligacombo.getSelectionModel().clearSelection();
        fechapicker.setValue(null);
    }
    @FXML
    void initialize() {
        try{
            dataGestorLiga.getLigas().setAll(loqligas.cargarligas());
            dataGestorLiga.actualizarContadorId();
        }catch (Exception e){
            mostrarErrores("Error al inicializar la escena", e);
        }

        // Configurar columnas de tabla de equipos locales
        Colid.setCellValueFactory(data -> data.getValue().idEquipoProperty());
        ColName.setCellValueFactory(data -> data.getValue().nombreEquipoProperty());
        Colidvisit.setCellValueFactory(data -> data.getValue().idEquipoProperty());
        Colnombrevisit.setCellValueFactory(data -> data.getValue().nombreEquipoProperty());
        
        // Asignar datos a las tablas
        TableTeams.setItems(DataEquipos.getInstance(Path.of("Data/equipos.xml")).getEquiposfiltrados());
        TableVisitante.setItems(DataEquipos.getInstance(Path.of("Data/equipos.xml")).getFiltradovisitante());
        try {
            datosPartidos.getPartidos().setAll(loq.cargarpartidos());
            datosPartidos.actualizarContadorId();
        } catch (Exception e) {
            mostrarErrores("Error al cargar partidos", e);
        }

        for (int i = 1; i <= 3; i++) {
            jornadacombo.getItems().add("Jornada " + i);
        }

        try {
            for (LigaDTO liga : dataGestorLiga.getLigas()) {
                Ligacombo.getItems().add(liga.nombreLigaProperty().get());
            }
        } catch (Exception e) {
            mostrarErrores("Error al cargar ligas", e);
        }

    }

    private String validarformulario(){
        if (Ligacombo.getSelectionModel().getSelectedItem() == null) {
            return "Liga no seleccionada o inexistente";
        }
        if(TableTeams.getSelectionModel().getSelectedItem() != null && TableVisitante.getSelectionModel().getSelectedItem() != null){
            if(TableTeams.getSelectionModel().getSelectedItem().idEquipoProperty().get().equals(
                    TableVisitante.getSelectionModel().getSelectedItem().idEquipoProperty().get())){
                return "No se puede seleccionar el mismo equipo como local y visitante";
            }
        }
        if(TableTeams.getSelectionModel().getSelectedItem() == null){
            return "Equipo local no seleccionado";
        }
        if(TableVisitante.getSelectionModel().getSelectedItem() == null){
            return "Equipo visitante no seleccionado";
        }
        if(DataEquipos.getInstance(null).getEquipos().isEmpty()){
            return "No hay equipos registrados";
        }
        if (estadiocombo.getSelectionModel().getSelectedItem() == null) {
            return "Estadio no seleccionado";
        }
        if (jornadacombo.getSelectionModel().getSelectedItem() == null) {
            return "Jornada no seleccionada";
        }
        if (fechapicker.getValue() == null) {
            return "Fecha no seleccionada";
        }
        return null;
    }


    private void setlocals(Equipos local){
        localname.setText(local.nombreEquipoProperty().get());
        localciudad.setText(local.ciudadEquipoProperty().get());
        locallabel.setText(local.getNombre());

        estadiocombo.getItems().clear();
        estadiocombo.getItems().add(local.estadioEquipoProperty().get());

        Equipos visitante = TableVisitante.getSelectionModel().getSelectedItem();
        if(visitante != null && !visitante.estadioEquipoProperty().get().equals(local.estadioEquipoProperty().get())){
            estadiocombo.getItems().add(visitante.estadioEquipoProperty().get());
        }
    }
    
    private void setvisitante(Equipos visitante){
        visitantename.setText(visitante.nombreEquipoProperty().get());
        visitlabel.setText(visitante.getNombre());
        ciudadvisitante.setText(visitante.ciudadEquipoProperty().get());

        if(estadiocombo.getItems().isEmpty()){
            estadiocombo.getItems().add(visitante.estadioEquipoProperty().get());
        } else {

            if(!estadiocombo.getItems().contains(visitante.estadioEquipoProperty().get())){
                estadiocombo.getItems().add(visitante.estadioEquipoProperty().get());
            }
        }
    }
    private LigaDTO buscarLigaPorNombre(String nombreLiga) {
        return DataGestorLiga.getInstance(Path.of("Data/ligas.xml")).getLigas().stream()
                .filter(l -> l.nombreLigaProperty().get().equals(nombreLiga))
                .findFirst()
                .orElse(null);
    }

    private boolean existePartidoDuplicado(Equipos local, Equipos visitante, String jornada, String nombreLiga) {
        return DataPartidos.getInstance(Path.of("Data/partidos.xml")).getPartidos().stream()
                .anyMatch(p ->
                        p.getlocal().idEquipoProperty().get().equals(local.idEquipoProperty().get()) &&
                                p.getvisitante().idEquipoProperty().get().equals(visitante.idEquipoProperty().get()) &&
                                p.getJornadas().get().equals(jornada) &&
                                p.getliga().get().equals(nombreLiga)
                );
    }

    private void crearYAsociarPartido(String partidonombre, Equipos local, Equipos visitante, String jornada, String nombreLiga) {
        DataPartidos.getInstance(null).agregarPartido(
                partidonombre,
                local,
                visitante,
                jornada,
                estadiocombo.getSelectionModel().getSelectedItem(),
                fechapicker.getValue()
        );
        PartidosDTO nuevoPartido = DataPartidos.getInstance(null).getPartidoPorNombre(partidonombre);
        nuevoPartido.setliga(nombreLiga);
        nuevoPartido.getlocal().setPartidosjugados(nuevoPartido.getlocal().jugadosProperty().get() + 1);
        nuevoPartido.getvisitante().setPartidosjugados(nuevoPartido.getvisitante().jugadosProperty().get() + 1);

        for (LigaDTO liga : DataGestorLiga.getInstance(Path.of("Data/ligas.xml")).getLigas()) {
            if (liga.nombreLigaProperty().get().equals(nombreLiga)) {
                if (!liga.getequipos().contains(local)) {
                    liga.getequipos().add(local);
                }
                if (!liga.getequipos().contains(visitante)) {
                    liga.getequipos().add(visitante);
                }
                liga.getpartidos().add(nuevoPartido);
                liga.aumentarcapacidadliga();
                break;
            }
        }
    }

    @FXML
    void obtenerlocal(MouseEvent event) {
        Equipos equipolocal = TableTeams.getSelectionModel().getSelectedItem();
        if(equipolocal == null){
            return;
        }
        setlocals(equipolocal);
    }

    @FXML
    void obtenervisitante(MouseEvent event) {
        Equipos equipovisitante = TableVisitante.getSelectionModel().getSelectedItem();
        if(equipovisitante == null){
            return;
        }
        setvisitante(equipovisitante);

    }

    @FXML
    void CreacionPscene(ActionEvent event) {
        cambiarEscena(event, "Creacion_liga.fxml");
    }

    @FXML
    void CreationEScene(ActionEvent event) {
        cambiarEscena(event, "creacion_equipos.fxml");
    }

    @FXML
    void Modificationscene(ActionEvent event) {
        cambiarEscena(event, "modificacion_equipo.fxml");
    }

    @FXML
    void RegistrarScene(ActionEvent event) {
        cambiarEscena(event, "registrar_resultados.fxml");

    }

    @FXML
    void TablaposicionesScene(ActionEvent event) {
        cambiarEscena(event, "TablaPremier.fxml");

    }
    @FXML
    void tablapartidosscene(ActionEvent event) {
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
