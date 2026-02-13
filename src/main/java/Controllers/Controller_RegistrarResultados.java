package Controllers;

import java.io.IOException;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
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
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Controller_RegistrarResultados {

    @FXML
    private TableColumn<PartidosDTO, String> Colid;

    @FXML
    private TableColumn<PartidosDTO, String> Colname;

    @FXML
    private Label EstadioPartido;

    @FXML
    private Label JornadaPartido;

    @FXML
    private Label PuntoslocalLabel;

    @FXML
    private Label PuntosvisitLabel;

    @FXML
    private Label Visitantelabel;

    @FXML
    private TableColumn<PartidosDTO, String> estadopartido;

    @FXML
    private TextField buscartxt;

    @FXML
    private ComboBox<String> combojornada;

    @FXML
    private ComboBox<String> comboligas;

    @FXML
    private Label locallabel;

    @FXML
    private TableView<PartidosDTO> Tablapartidos;

    @FXML
    private TextField txtGFLocal;

    @FXML
    private TextField txtGFvisit;

    @FXML
    private Label LabelGCLocal;

    @FXML
    private Label LabelGCVisit;

    @FXML
    void CreacionPartidoScene(ActionEvent event) {
        cambiarEscena(event,"creacion_partidos.fxml");

    }

    @FXML
    void CreacionligaScene(ActionEvent event) {
        cambiarEscena(event,"Creacion_liga.fxml");

    }

    @FXML
    void ModificarEscene(ActionEvent event) {
        cambiarEscena(event, "modificacion_equipo.fxml");


    }

    @FXML
    void TablaPosicionesScene(ActionEvent event) {
        cambiarEscena(event, "TablaPremier.fxml");

    }
    @FXML
    void tablapartidosscene(ActionEvent event) {
        cambiarEscena(event,"Tablapartidos.fxml");
    }

    private final DataPartidos dataPartidos = DataPartidos.getInstance();
    private final LogicPartidos loq = new LogicPartidos(dataPartidos);

    DataEquipos dataEquipos = DataEquipos.getInstance();
    LogicaEquipo logicEquipos = new LogicaEquipo(dataEquipos);

    DataGestorLiga gestionliga = DataGestorLiga.getInstance();
    LogicLigas log = new LogicLigas(gestionliga);


    @FXML
    void buscar(ActionEvent event) {
        try {
            String busqueda = buscartxt.getText().trim();
            if (busqueda.isEmpty()) {
                Tablapartidos.setItems(dataPartidos.getPartidos());
            }
            else {
                Tablapartidos.setItems(dataPartidos.getPartidosfiltrados());
                dataPartidos.getPartidosfiltrados().setPredicate(partido ->
                        partido.idpartidoProperty().get().toLowerCase().contains(busqueda.toLowerCase())
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void filtrarjornada(ActionEvent event) {
        try{
            String jornadaSeleccionada = combojornada.getSelectionModel().getSelectedItem();


            if (jornadaSeleccionada == null || jornadaSeleccionada.isEmpty()) {
                mostrarErrores("Error de selección", new Exception("Debe seleccionar una jornada antes de filtrar."));
                combojornada.getSelectionModel().clearSelection();
                return;
            }

            String comboliga = comboligas.getSelectionModel().getSelectedItem();
            if(comboliga == null){
                mostrarErrores("Error de selección", new Exception("Debe seleccionar una liga antes de filtrar por jornada."));
                return;
            }

            LigaDTO liga = gestionliga.getLigas().stream()
                    .filter(l -> l.nombreLigaProperty().get().equals(comboliga))
                    .findFirst()
                    .orElse(null);

            if (liga == null) {
                mostrarErrores("Liga no encontrada", new Exception("No se encontro la liga seleccionada."));
                combojornada.getSelectionModel().clearSelection();
                return;
            }

            int idLiga = Integer.parseInt(liga.idLigaProperty().get());
            dataPartidos.getPartidosfiltrados().setPredicate(partido ->
                    partido != null &&
                            partido.jornadasProperty().get().equals(jornadaSeleccionada) &&
                            partido.getliga().get() == idLiga
            );

            Tablapartidos.setItems(dataPartidos.getPartidosfiltrados());

        } catch (Exception e) {
            mostrarErrores("Error al filtrar por jornada", e);
        }
    }


    @FXML
    void creacionEscene(ActionEvent event) {
        cambiarEscena(event,"creacion_equipos.fxml");

    }
    @FXML
    void initialize() {
        Colid.setCellValueFactory(data -> data.getValue().idpartidoProperty());
        Colname.setCellValueFactory(data -> data.getValue().nombrepartidoProperty());
        estadopartido.setCellValueFactory(data -> data.getValue().estadoProperty());

        Tablapartidos.setItems(DataPartidos.getInstance().getPartidosfiltrados());

        comboligas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevoNombre) -> {
            if (nuevoNombre != null) {
                LigaDTO seleccionada = gestionliga.getLigas().stream()
                        .filter(l -> l.nombreLigaProperty().get().equals(nuevoNombre))
                        .findFirst().orElse(null);

                if (seleccionada != null) {
                    gestionliga.filtrarPorLiga(seleccionada);
                    filtrarpartidosPorLiga();
                }
            }
        });
        for (int i = 1; i <= 3; i++) {
            combojornada.getItems().add("Jornada " + i);
        }
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                gestionliga.getLigas().setAll(log.cargarligas());
                dataPartidos.getPartidos().setAll(loq.cargarpartidos());
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            comboligas.getItems().clear();
            for (LigaDTO liga : gestionliga.getLigas()) {
                comboligas.getItems().add(liga.nombreLigaProperty().get());
            }

            dataPartidos.getPartidosfiltrados().setPredicate(partido -> false);
        });

        task.setOnFailed(e -> {
            mostrarErrores("Error al cargar datos iniciales", new Exception(task.getException()));
        });

        new Thread(task).start();
    }
    @FXML
    void fltrarcombo(ActionEvent event) {
        Tablapartidos.setItems(DataPartidos.getInstance().getPartidosfiltrados());
        cargar();
        filtrarpartidosPorLiga();
    }


    private void cargar(){
        try {
            DataPartidos.getInstance().getPartidos().setAll(loq.cargarpartidos());
        } catch (Exception e) {
            mostrarErrores("Error al cargar partidos", e);
        }
    }
    
    private void filtrarpartidosPorLiga() {
        String ligaSeleccionada = comboligas.getSelectionModel().getSelectedItem();
        if (ligaSeleccionada == null || ligaSeleccionada.isEmpty()) {
            dataPartidos.getPartidosfiltrados().setPredicate(partido -> false);
        } else {
            LigaDTO liga = gestionliga.getLigas().stream()
                    .filter(l -> l.nombreLigaProperty().get().equals(ligaSeleccionada))
                    .findFirst()
                    .orElse(null);
            if (liga == null) {
                dataPartidos.getPartidosfiltrados().setPredicate(partido -> false);
            } else {
                int idLiga = Integer.parseInt(liga.idLigaProperty().get());
                dataPartidos.getPartidosfiltrados().setPredicate(partido ->
                        partido != null && partido.getliga().get() == idLiga
                );
            }
        }
    }

    @FXML
    void limpiar() {
        txtGFLocal.clear();
        txtGFvisit.clear();
        LabelGCLocal.setText(". . . . . . . .");
        LabelGCVisit.setText(". . . . . . . .");
        PuntoslocalLabel.setText(". . . . . . . .");
        PuntosvisitLabel.setText(". . . . . . . .");
        combojornada.getSelectionModel().clearSelection();
        comboligas.getSelectionModel().clearSelection();
        locallabel.setText(". . . . . . . . .");
        Visitantelabel.setText(". . . . . . . . .");

    }

    @FXML
    void guardar(ActionEvent event) {
        try {
            // Validar que haya una liga seleccionada
            if (comboligas.getSelectionModel().getSelectedItem() == null) {
                mostrarErrores("Liga no seleccionada", new Exception("Debe seleccionar una liga antes de registrar resultados."));
                return;
            }
            
            PartidosDTO partidos = Tablapartidos.getSelectionModel().getSelectedItem();

            if (partidos == null) {
                mostrarErrores("Partido no seleccionado", new Exception("Debe seleccionar un partido de la tabla."));
                return;
            }
            if (txtGFLocal.getText().trim().isEmpty() || txtGFvisit.getText().trim().isEmpty()) {
                mostrarErrores("Campos vacios", new Exception("Debe ingresar ambos marcadores antes de guardar."));
                return;
            }


            local(partidos.getlocal());
            visitante(partidos.getvisitante());

            int goleslocal = Integer.parseInt(txtGFLocal.getText());
            int golesvisitante = Integer.parseInt(txtGFvisit.getText());

            if (!verificarCamposVacios(goleslocal, golesvisitante)) return;
            if (!numeronegativos(goleslocal, golesvisitante)) return;

            if(!validargoles(goleslocal, golesvisitante)){
                mostrarErrores("Formato invalido", new Exception("Ingrese solo números en los goles."));
                return;
            }
            if(partidos.estadoProperty().get().equals("Finalizado")){
                mostrarErrores("Partido ya registrado", new Exception("El resultado de este partido ya ha sido registrado."));
                return;
            }
            LabelGCLocal.setText((String.valueOf(golesvisitante)));
            LabelGCVisit.setText(String.valueOf(goleslocal));
            JornadaPartido.setText(partidos.jornadasProperty().get());
            if (goleslocal > golesvisitante) {
                partidos.getlocal().setPuntos(partidos.getlocal().puntosProperty().get() + 3);
                partidos.getvisitante().setPuntos(partidos.getvisitante().puntosProperty().get());
                PuntoslocalLabel.setText("+3");
                PuntosvisitLabel.setText("+0");
                partidos.getlocal().setPartidosganados();
                partidos.getvisitante().setPartidosperdidos();
            } else if (golesvisitante > goleslocal) {
                partidos.getlocal().setPuntos(partidos.getlocal().puntosProperty().get());
                partidos.getvisitante().setPuntos(partidos.getvisitante().puntosProperty().get() + 3);
                PuntoslocalLabel.setText("+0");
                PuntosvisitLabel.setText("+3");
                partidos.getvisitante().setPartidosganados();
                partidos.getlocal().setPartidosperdidos();
            } else {
                partidos.getlocal().setPuntos(partidos.getlocal().puntosProperty().get() + 1);
                partidos.getvisitante().setPuntos(partidos.getvisitante().puntosProperty().get() + 1);
                PuntoslocalLabel.setText("+1");
                PuntosvisitLabel.setText("+1");
            }

            // Actualiza goles a favor y en contra para ambos equipos
            partidos.getlocal().setGolesafavor(partidos.getlocal().golesFavorProperty().get() + goleslocal);
            partidos.getlocal().setGolesencontra(partidos.getlocal().golesContraProperty().get() + golesvisitante);

            partidos.getvisitante().setGolesafavor(partidos.getvisitante().golesFavorProperty().get() + golesvisitante);
            partidos.getvisitante().setGolesencontra(partidos.getvisitante().golesContraProperty().get() + goleslocal);

            partidos.estadoProperty().set("Finalizado");
            partidos.setGolesLocal(goleslocal);
            partidos.setGolesVisitante(golesvisitante);
            javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
                @Override
                protected Void call() throws Exception {
                    loq.actualizarPartido(partidos);
                    logicEquipos.actualizarEstadisticas(partidos.getlocal());
                    logicEquipos.actualizarEstadisticas(partidos.getvisitante());
                    return null;
                }
            };

            task.setOnSucceeded(e -> {
                Tablapartidos.refresh();
                javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3),
                                ev -> limpiar())
                );
                timeline.play();
            });

            task.setOnFailed(e -> {
                mostrarErrores("Error al actualizar partido (remoto)", new Exception(task.getException()));
            });

            new Thread(task).start();

        } catch (Exception e) {
            mostrarErrores("Se produjo un error al guardar...", e);
        }
    }

    @FXML
    void simulaciom(ActionEvent event) {
        // Delegar simulación masiva al servidor para evitar cambios solo en memoria
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                try (client.PartidosServiceClientSocket client = new client.PartidosServiceClientSocket("127.0.0.1", 5050)) {
                    int simulados = client.simularTodosSync(60);
                    // Recargar partidos desde BD
                    var partidos = DataPartidos.getInstance();
                    var logicPartidos = new Logic.LogicPartidos(partidos);
                    var lista = logicPartidos.cargarpartidos();
                    javafx.application.Platform.runLater(() -> {
                        DataPartidos.getInstance().getPartidos().setAll(lista);
                        Tablapartidos.refresh();
                        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                        alerta.setTitle("Simulación completada");
                        alerta.setHeaderText(null);
                        alerta.setContentText("Se han registrado " + simulados + " partidos simulados.");
                        alerta.showAndWait();
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

    private void cambiarEscena(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/practice_fx/proyecto_premier/" + fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            mostrarErrores("Error al cambiar de escena", e);
        }
    }
    private boolean validargoles(int golesLocal, int golesVisitante) {
        if (golesLocal < 0 || golesVisitante < 0) {
            return false;
        }
        return true;
    }
    public void validaciones(){

    }

    public void local(Equipos local){
        locallabel.setText(local.nombreEquipoProperty().get());

    }
    public void visitante(Equipos visitante){
        Visitantelabel.setText(visitante.nombreEquipoProperty().get());
    }

    @FXML
    void obtenerequipos(MouseEvent event) {
        PartidosDTO seleccionado = Tablapartidos.getSelectionModel().getSelectedItem();
        if(seleccionado== null){
            return;
        }
        local(seleccionado.getlocal());
        visitante(seleccionado.getvisitante());
        EstadioPartido.setText(seleccionado.estadioProperty().get());
        JornadaPartido.setText(seleccionado.jornadasProperty().get());
    }

    private boolean verificarCamposVacios(int golesLocal, int golesVisit) {
        if (golesLocal < 0 || golesVisit < 0) {
            mostrarErrores("Campos vacíos", new Exception("Debe ingresar puntaje para ambos equipos."));
            return false;
        }
        return true;
    }
    private boolean numeronegativos(int golesLocal, int golesVisit) {
        if (golesLocal < 0 || golesVisit < 0) {
            mostrarErrores("Formato inválido", new Exception("Ingrese solo numeros enteros positivos en los goles."));
            return false;
        }
        return true;
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

