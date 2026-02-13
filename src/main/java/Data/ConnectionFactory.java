package Data;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class ConnectionFactory {

    private static final String PROPS_FILE = "db.properties";
    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream in = ConnectionFactory.class.getClassLoader().getResourceAsStream(PROPS_FILE)) {
            if (in == null) throw new RuntimeException("No se encontró " + PROPS_FILE + " en resources.");
            Properties p = new Properties();
            p.load(in);

            url = p.getProperty("db.url");
            user = p.getProperty("db.user");
            password = p.getProperty("db.password");

            if (url == null || user == null) {
                throw new RuntimeException("db.url y db.user son obligatorios en " + PROPS_FILE);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error cargando configuración de BD: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo conectar a MySQL: " + e.getMessage(), e);
        }
    }
}
