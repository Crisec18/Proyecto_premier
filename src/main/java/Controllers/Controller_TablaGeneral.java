package Controllers;

import DTO.Equipos;
import DTO.LigaDTO;
import DTO.PartidosDTO;
import Data.DataEquipos;
import Data.DataGestorLiga;
import Data.DataPartidos;
import Logic.LogicLigas;
import Logic.LogicPartidos;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

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


    DataGestorLiga gestionliga = DataGestorLiga.getInstance();
    LogicLigas log = new LogicLigas(gestionliga);
    // evita mostrar la misma alerta repetidamente al recargar equipos
    private volatile boolean reloadErrorShown = false;
    // evita recargas concurrentes
    private final Object reloadLock = new Object();
    private volatile boolean reloading = false;
    // evita mostrar varias alertas simultáneas
    private final AtomicBoolean alertShowing = new AtomicBoolean(false);

    private void configurarTabla() {
        txtID.setCellValueFactory(data -> data.getValue().idEquipoProperty());
        tblEquipo.setCellValueFactory(data -> data.getValue().nombreEquipoProperty());
        txtGanados.setCellValueFactory(data -> data.getValue().ganadosProperty());
        txtEmpates.setCellValueFactory(data -> data.getValue().empatesProperty());
        txtPerdidos.setCellValueFactory(data -> data.getValue().perdidosProperty());
        txtGolesAFavor.setCellValueFactory(data -> data.getValue().golesFavorProperty());
        tabGolesContra.setCellValueFactory(data -> data.getValue().golesContraProperty());
        tblPos.setCellValueFactory(data-> data.getValue().puntosProperty());
    }

    @FXML
    public void initialize() {
        configurarTabla();

        try {
            // Cargar equipos desde BD (en background)
            reloadEquiposAsync();

            DataPartidos dataPartidos = DataPartidos.getInstance();
            LogicPartidos logicPartidos = new LogicPartidos(dataPartidos);
            dataPartidos.getPartidos().setAll(logicPartidos.cargarpartidos());

            gestionliga.getLigas().setAll(log.cargarligas());

            ligacombo.getItems().clear();
            for (LigaDTO liga : gestionliga.getLigas()) {
                ligacombo.getItems().add(liga.nombreLigaProperty().get());
            }

            if (!ligacombo.getItems().isEmpty()) {
                ligacombo.getSelectionModel().selectFirst();
            }

            ligacombo.getSelectionModel().selectedItemProperty().addListener((observable) -> {
                String nuevoNombre = ligacombo.getSelectionModel().getSelectedItem();
                if (nuevoNombre != null) {
                    // recargar equipos en background para evitar bloqueos y asegurar datos actualizados
                    reloadEquiposAsync();
                    filtrarequipos(null);
                }
            });

            DataEquipos.getInstance().getEquipos().addListener((ListChangeListener<Equipos>) change -> {
                // cuando la lista de equipos cambie, reconstruir la tabla visible
                refreshTableFromCurrentData();
            });

            DataPartidos.getInstance().getPartidos().addListener((ListChangeListener<PartidosDTO>) change -> {
                refreshTableFromCurrentData();
            });

        } catch (Exception e) {
            mostrarErrores("Error al inicializar la tabla general", e);
        }

    }

    @FXML
    void buscar(ActionEvent event) {
        try {
            String texto = txfBuscarTablaPremier.getText();
            if (texto == null || texto.isBlank()) {
                filtrarequipos(null);
                return;
            }
            // Buscar directamente en DataEquipos por ID (coincidencia parcial) o por nombre
            String q = texto.trim();
            ObservableList<Equipos> todos = DataEquipos.getInstance().getEquipos();
            ObservableList<Equipos> encontrados = FXCollections.observableArrayList();

            // Si la consulta es numérica, buscar por ID exacto y también por containing
            boolean isNumber = true;
            int qnum = 0;
            try {
                qnum = Integer.parseInt(q);
            } catch (NumberFormatException ex) {
                isNumber = false;
            }

            for (Equipos e : todos) {
                String idStr = String.valueOf(e.getIdEquipo());
                String nombre = e.getNombre() != null ? e.getNombre().toLowerCase() : "";
                if (isNumber) {
                    if (e.getIdEquipo() == qnum || idStr.contains(q)) {
                        encontrados.add(e);
                        continue;
                    }
                }
                // búsqueda por nombre parcial (case-insensitive)
                if (!q.isBlank() && nombre.contains(q.toLowerCase())) {
                    encontrados.add(e);
                } else if (idStr.contains(q)) {
                    encontrados.add(e);
                }
            }

            System.out.println("Buscar '" + q + "' -> encontrados=" + encontrados.size());
            Platform.runLater(() -> {
                tblLista.setItems(encontrados);
                tblLista.refresh();
                System.out.println("Tabla tras buscar actualizada (rows=" + tblLista.getItems().size() + ")");
            });
            if (encontrados.isEmpty()) System.out.println("Buscar: no se encontraron equipos para '" + q + "'");
        } catch (Exception e) {
            mostrarErrores("Error al filtrar equipos", e);
        }
    }

    @FXML
    void filtrarequipos(ActionEvent event) {
        String nombreLiga = ligacombo.getSelectionModel().getSelectedItem();
        if (nombreLiga == null) return;

        LigaDTO seleccionada = gestionliga.getLigas().stream()
                .filter(l -> l.nombreLigaProperty().get().equals(nombreLiga))
                .findFirst().orElse(null);

        if (seleccionada != null) {
            int idLiga = Integer.parseInt(seleccionada.idLigaProperty().get());

            // Nota: no recargamos desde BD aquí para evitar disparar los listeners
            // La recarga desde BD se hace explícitamente en el listener de selección de liga.

            ObservableList<Equipos> equiposDeLiga = FXCollections.observableArrayList();
            for (Equipos equipo : DataEquipos.getInstance().getEquipos()) {
                boolean tienePartidosEnLiga = DataPartidos.getInstance().getPartidos().stream()
                        .anyMatch(p -> p != null && p.getliga().get() == idLiga &&
                                (p.getlocal().idEquipoProperty().get().equals(equipo.idEquipoProperty().get()) ||
                                        p.getvisitante().idEquipoProperty().get().equals(equipo.idEquipoProperty().get())));

                if (tienePartidosEnLiga) {
                    equiposDeLiga.add(equipo);
                }
            }
            FXCollections.sort(equiposDeLiga, Comparator
                    .comparingInt((Equipos e) -> e.puntosProperty().get()).reversed()
                    .thenComparingInt(e -> e.ganadosProperty().get()).reversed()
                    .thenComparingInt(e -> e.empatesProperty().get()).reversed());

            tblLista.setItems(equiposDeLiga);
        }
    }

    // Reconstruye la tabla usando los datos actualmente cargados en los singletons
    private void refreshTableFromCurrentData() {
        String nombreLiga = ligacombo.getSelectionModel().getSelectedItem();
        System.out.println("refreshTableFromCurrentData() -> liga seleccionada='" + nombreLiga + "'");
        if (nombreLiga == null) {
            // si no hay liga seleccionada, solo refrescar
            Platform.runLater(() -> tblLista.refresh());
            return;
        }
        LigaDTO seleccionada = gestionliga.getLigas().stream()
                .filter(l -> l.nombreLigaProperty().get().equals(nombreLiga))
                .findFirst().orElse(null);
        if (seleccionada == null) {
            System.out.println("refreshTableFromCurrentData: liga no encontrada en gestionliga: '" + nombreLiga + "'");
            return;
        }
        int idLiga = Integer.parseInt(seleccionada.idLigaProperty().get());

        ObservableList<Equipos> equiposDeLiga = FXCollections.observableArrayList();
        int totalEquipos = DataEquipos.getInstance().getEquipos().size();
        int totalPartidos = DataPartidos.getInstance().getPartidos().size();
        for (Equipos equipo : DataEquipos.getInstance().getEquipos()) {
            boolean tienePartidosEnLiga = DataPartidos.getInstance().getPartidos().stream()
                    .anyMatch(p -> p != null && p.getliga().get() == idLiga &&
                            (p.getlocal().idEquipoProperty().get().equals(equipo.idEquipoProperty().get()) ||
                                    p.getvisitante().idEquipoProperty().get().equals(equipo.idEquipoProperty().get())));
            if (tienePartidosEnLiga) equiposDeLiga.add(equipo);
        }
        System.out.println("refresh: totalEquipos=" + totalEquipos + ", totalPartidos=" + totalPartidos + ", candidatos en liga=" + equiposDeLiga.size());
        FXCollections.sort(equiposDeLiga, Comparator
                .comparingInt((Equipos e) -> e.puntosProperty().get()).reversed()
                .thenComparingInt(e -> e.ganadosProperty().get()).reversed()
                .thenComparingInt(e -> e.empatesProperty().get()).reversed());

        Platform.runLater(() -> applyTableItems(equiposDeLiga, "Tabla de posiciones actualizada"));
    }

    // Establece items y fuerza una actualización visual robusta de TableView
    private void applyTableItems(ObservableList<Equipos> items, String debugMsg) {
        tblLista.setItems(items);
        // refresh simple
        tblLista.refresh();
        // Si por alguna razón la vista no se actualiza, togglear visibilidad de columnas fuerza re-render
        try {
            boolean visible = true;
            if (!tblLista.getColumns().isEmpty()) {
                // toggle la primera columna invis->vis para forzar repaint
                TableColumn<?, ?> c = tblLista.getColumns().get(0);
                visible = c.isVisible();
                c.setVisible(!visible);
                c.setVisible(visible);
            }
        } catch (Exception ex) {
            System.err.println("applyTableItems: error forzando repaint: " + ex.getMessage());
        }
        System.out.println(debugMsg + " (rows=" + (items != null ? items.size() : 0) + ")");
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
            mostrarErrores("Error al cambiar de escena", e);
        }
    }

    private void mostrarErrores(String titulo, Exception e) {
        // Mostrar no bloqueante para evitar nested event loop y recursión
        System.err.println(titulo + ": " + e.getMessage());
        // si ya hay una alerta mostrándose, suprimir
        if (!alertShowing.compareAndSet(false, true)) {
            // ya hay una alerta visible, suprimir esta
            System.err.println("Alerta suprimida porque ya hay otra visible: " + titulo);
            return;
        }
        Platform.runLater(() -> {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle(titulo);
            alerta.setHeaderText("Ocurrió un problema...");
            alerta.setContentText(e.getMessage());
            alerta.setOnHidden(ev -> alertShowing.set(false));
            alerta.show();
        });
    }

    private void reloadEquiposAsync() {
        synchronized (reloadLock) {
            if (reloading) return;
            reloading = true;
        }
        javafx.concurrent.Task<java.util.List<Equipos>> task = new javafx.concurrent.Task<>() {
            @Override
            protected java.util.List<Equipos> call() throws Exception {
                // Leer directamente desde la BD local para reflejar cambios post-simulación
                return DataEquipos.getInstance().cargarSQL();
            }
        };

        task.setOnSucceeded(e -> {
            var lista = task.getValue();
            DataEquipos.getInstance().getEquipos().setAll(lista);
            System.out.println("Recarga equipos: cargados=" + (lista != null ? lista.size() : 0));
            if (lista != null && !lista.isEmpty()) {
                System.out.println("Primeros equipos tras recarga:");
                for (int i = 0; i < Math.min(5, lista.size()); i++) {
                    Equipos eq = lista.get(i);
                    System.out.println("  id=" + eq.getIdEquipo() + ", nombre='" + eq.getNombre() + "', puntos=" + eq.getPuntos());
                }
            }
             // Forzar reconstrucción de la tabla según la liga seleccionada
             refreshTableFromCurrentData();
             reloadErrorShown = false;
             reloading = false;
         });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            System.err.println("Error recargando equipos: " + (ex != null ? ex.getMessage() : "null"));
            if (!reloadErrorShown) {
                reloadErrorShown = true;
                Platform.runLater(() -> mostrarErrores("Error al recargar equipos", new Exception(ex)));
            }
            reloading = false;
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }
}
