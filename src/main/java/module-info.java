module practice_fx.proyecto_premier {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.xml;
    requires java.sql;



    opens practice_fx.proyecto_premier to javafx.fxml;
    opens Controllers to javafx.fxml;

    exports practice_fx.proyecto_premier;
    exports Controllers;
}