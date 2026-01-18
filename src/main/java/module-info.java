module practice_fx.proyecto_premier {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.base;
    requires javafx.graphics;
    requires jdk.management.agent;


    opens practice_fx.proyecto_premier to javafx.fxml;
    opens Controllers to javafx.fxml;

    exports practice_fx.proyecto_premier;
    exports Controllers;
}