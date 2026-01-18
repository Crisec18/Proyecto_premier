package Data;

import DTO.Equipos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DataEquipos {

    //XML para equipos
    private final Path RutaEquiposXML;
    private static DataEquipos instance;


    private final AtomicInteger idcounter = new AtomicInteger(1);
    private ObservableList<Equipos> equipos;
    private FilteredList<Equipos> equiposfiltrados;
    private FilteredList<Equipos> filtradovisitante;

    //contructor
    public DataEquipos(Path rutaEquiposXML){
        RutaEquiposXML = rutaEquiposXML;
        equipos = FXCollections.observableArrayList();
        equiposfiltrados = new FilteredList<>(equipos);
        filtradovisitante = new FilteredList<>(equipos);
    }
//singlenton con ruta xml
    public static DataEquipos getInstance(Path rutaEquiposXML) {
        if (instance == null) {
            instance = new DataEquipos(rutaEquiposXML);
        }
        return instance;
    }

    public ObservableList<Equipos> getEquipos(){
        return equipos;
    }

    public FilteredList<Equipos> getEquiposfiltrados(){
        return equiposfiltrados;
    }

    public FilteredList<Equipos> getFiltradovisitante() {
        return filtradovisitante;
    }

    public void agregarequipo(String nombre, String estadio, String ciudad, LocalDate annio ){
        String id = String.valueOf(idcounter.getAndIncrement()); // consultar esto con el profe
        Equipos equipo = new Equipos(id, nombre, estadio, ciudad, annio);
        equipos.add(equipo);
    }

//XML...............................................................................................................
    //cargar desde el xml
    public List<Equipos> cargar() throws Exception {
        if (!Files.exists(RutaEquiposXML)) {
            //si no hay, retornar lista vacia
            return new ArrayList<>();
        }
        Document doc;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringComments(true);
        dbf.setNamespaceAware(false);

        DocumentBuilder db = dbf.newDocumentBuilder();

        try (InputStream in = Files.newInputStream(RutaEquiposXML)) {
            doc = db.parse(in);
        }

        doc.getDocumentElement().normalize();

        List<Equipos> listaEquipos = new ArrayList<>();
        NodeList nodolist = doc.getElementsByTagName("equipo");

        //recorrer nodos que son los elementos equipo
        for (int i = 0; i < nodolist.getLength(); i++) {
            Node n = nodolist.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) continue;

            Element e = (Element) n;

            //serializa cada equipo
            String idEquipo = getText(e, "idEquipo");
            String nombreEquipo = getText(e, "nombreEquipo");
            String estadioEquipo = getText(e, "estadioEquipo");
            String ciudadEquipo = getText(e, "ciudadEquipo");
            LocalDate annioFundacion = LocalDate.parse(getText(e, "annioFundacion"));

            equipos.add(new Equipos(
                    String.valueOf(idEquipo),
                    nombreEquipo,
                    estadioEquipo,
                    ciudadEquipo,
                    annioFundacion
            ));
        }
        return  listaEquipos;
    }

    public void guardar(List<Equipos> listaEquipos) throws Exception {
        if (RutaEquiposXML.getParent() != null) {
            Files.createDirectories(RutaEquiposXML.getParent());
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element root = doc.createElement("equipos");
        doc.appendChild(root);

        for (Equipos equipo : listaEquipos) {
            Element eq = doc.createElement("equipo");
            root.appendChild(eq);

            agregar(doc, eq, "idEquipo", equipo.idEquipoProperty().getValue());
            agregar(doc, eq, "nombreEquipo", equipo.nombreEquipoProperty().getValue());
            agregar(doc, eq, "estadioEquipo", equipo.estadioEquipoProperty().getValue());
            agregar(doc, eq, "ciudadEquipo", equipo.ciudadEquipoProperty().getValue());
            agregar(doc, eq, "annioFundacion", String.valueOf(equipo.getAnnio()));
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        //sinceramente esto no se para que es pero el profe lo metio, para algo sera
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        t.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());

        try (OutputStream out = Files.newOutputStream(RutaEquiposXML, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            t.transform(new DOMSource(doc), new StreamResult(out));
        }
    }

//Utilidades para XML................................................
    private String getText(Element e, String tag) {
        NodeList nl = e.getElementsByTagName(tag);
        if (nl.getLength() == 0) return "";
        return nl.item(0).getTextContent();
    }

    private static void agregar(Document doc, Element patent, String tag, String value){
        Element ele = doc.createElement(tag);
        ele.appendChild(doc.createTextNode(value == null ? "" : value));
        patent.appendChild(ele);
    }

    // esto es para pasar de string a int con valor por defecto, pero no se usa de momento
    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception ex) {
            return def;
        }
    }
}



