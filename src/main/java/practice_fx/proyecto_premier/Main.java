package practice_fx.proyecto_premier;

import Data.DataEquipos;
import Data.DataPartidos;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        //Rutas de los xml
        DataEquipos.getInstance(Path.of("Data/equipos.xml"));

        //DataPartidos.getInstance(Path.of("Data/partidos.xml"));
        //DataResultados.getInstance(Path.of("Data/resultados.xml"));

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/practice_fx/proyecto_premier/creacion_equipos.fxml")
        );

        Scene scene = new Scene(loader.load());

        stage.setTitle("Proyecto Premier League");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
