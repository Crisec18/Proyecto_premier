package Data;

import DTO.Equipos;
import DTO.LigaDTO;
import DTO.PartidosDTO;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DataPartidos {
    private static DataPartidos instance;
    private final Path RutapartidosXML;
    private final AtomicInteger idcounter = new AtomicInteger(1);
    private ObservableList<PartidosDTO> partidos;
    private FilteredList<PartidosDTO> Partidosfiltrados;

    public DataPartidos(Path rutapartidosXML) {
        RutapartidosXML = rutapartidosXML;
        partidos = javafx.collections.FXCollections.observableArrayList();
        Partidosfiltrados = new FilteredList<>(partidos);
    }

    public static DataPartidos getInstance(Path rutapartidosXML) {
        if (instance == null) {
            instance = new DataPartidos(rutapartidosXML);
        }
        return instance;
    }

    public ObservableList<PartidosDTO> getPartidos() {
        return partidos;

    }

    public FilteredList<PartidosDTO> getPartidosfiltrados() {
        return Partidosfiltrados;
    }


    public PartidosDTO agregarPartido(String partidonombre, Equipos equipo1, Equipos equipo2, String jornada, String Estadio, LocalDate fecha) {
        String id = String.valueOf(idcounter.getAndIncrement());
        PartidosDTO partido = new PartidosDTO(partidonombre, equipo1, equipo2, jornada, id, fecha, Estadio);
        partidos.add(partido);
        return partido;

    }

    public PartidosDTO getPartidoPorNombre(String nombre) {
        return partidos.stream()
                .filter(p -> p.nombrepartidoProperty().get().equals(nombre))
                .findFirst()
                .orElse(null);
    }
//Para los contadores de partidos------------------------------------------------------------
    //contador de equipos
    public int totalEquipos(){
        return DataEquipos.getInstance(null).getEquipos().size();
    }

    public boolean contadorJornadasFinalizadas(String jornada){
        List<PartidosDTO> partidosJornada = partidos.stream()
                .filter(p -> p.jornadasProperty().get().equals(jornada)).toList();

        if(partidosJornada.isEmpty()){
            return false;
        }
        else {
            return  partidosJornada.stream().allMatch(p -> p.estadoProperty().get().equals("Finalizado"));
        }
    }

    public int contarPartidosPorEstado(String estado) {
        return partidos.stream().filter(p -> p.estadoProperty().get().equals(estado)).toList().size();
    }
    //para los partidos que tienen estado pendiente en una jornada dada
    public boolean estaJornadaEsSimulable(String nombreJornada) {
        return partidos.stream().anyMatch(p -> p.getJornadas().get().equals(nombreJornada) &&
                        p.estadoProperty().get().equalsIgnoreCase("Pendiente"));
    }

    private Equipos buscarEquipoPorId(DataEquipos dataEquipos, String id) {
        return dataEquipos.getEquipos().stream()
                .filter(eq -> eq.idEquipoProperty().getValue().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void actualizarContadorId() {
        int maxId = 0;
        for (PartidosDTO Partidos : partidos) {
            try {
                int idActual = Integer.parseInt(Partidos.idpartidoProperty().getValue());
                if (idActual > maxId) {
                    maxId = idActual;
                }
            } catch (NumberFormatException e) {
            }
        }
        idcounter.set(maxId + 1);
    }

    public List<PartidosDTO> cargar() throws Exception {
        if (!Files.exists(RutapartidosXML)) {
            //si no hay, retornar lista vacia
            return new ArrayList<>();
        }
        Document doc;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringComments(true);
        dbf.setNamespaceAware(false);

        DocumentBuilder db = dbf.newDocumentBuilder();

        try (InputStream in = Files.newInputStream(RutapartidosXML)) {
            doc = db.parse(in);
        }

        doc.getDocumentElement().normalize();

        List<PartidosDTO> ListaPartidos = new ArrayList<>();
        NodeList nodolist = doc.getElementsByTagName("partido");
        DataEquipos dataEquipos = DataEquipos.getInstance(Path.of("Data/equipos.xml"));
        //recorrer nodos que son los elementos equipo
        for (int i = 0; i < nodolist.getLength(); i++) {
            Node n = nodolist.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) continue;

            Element e = (Element) n;

            String idPartido = getText(e, "idPartido");
            String nombrePartido = getText(e, "nombrePartido");
            String jornada = getText(e, "jornada");
            String estadio = getText(e, "estadio");
            LocalDate fecha = LocalDate.parse(getText(e, "fecha"));
            String liga = getText(e, "liga");
            String estado = getText(e, "estado");


            // se guardan los id para la referencia
            String idEquipoLocal = getText(e, "idEquipoLocal");
            String idEquipoVisitante = getText(e, "idEquipoVisitante");

            // se buscan en el data correspondiente
            Equipos local = buscarEquipoPorId(dataEquipos, idEquipoLocal);
            Equipos visitante = buscarEquipoPorId(dataEquipos, idEquipoVisitante);

            if (local != null && visitante != null) {
                PartidosDTO partido = new PartidosDTO(
                        nombrePartido,
                        local,
                        visitante,
                        jornada,
                        idPartido,
                        fecha,
                        estadio
                );

                partido.setliga(liga);
                if (estado != null && !estado.isEmpty()) {
                    partido.estadoProperty().set(estado);
                }

                ListaPartidos.add(partido);
            }
        }
        return  ListaPartidos;
    }

    public void guardar(List<PartidosDTO> listaPartidos) throws Exception {
        if (RutapartidosXML.getParent() != null) {
            Files.createDirectories(RutapartidosXML.getParent());
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element root = doc.createElement("partidos");
        doc.appendChild(root);

        for ( PartidosDTO partidos : listaPartidos) {
            Element eq = doc.createElement("partido");
            root.appendChild(eq);

            agregar(doc, eq, "idPartido", partidos.idpartidoProperty().getValue());
            agregar(doc, eq, "nombrePartido", partidos.nombrepartidoProperty().getValue());
            agregar(doc, eq, "jornada", partidos.jornadasProperty().getValue());
            agregar(doc, eq, "estadio", partidos.estadioProperty().getValue());
            agregar(doc, eq, "fecha", partidos.getfecha().toString());
            agregar(doc, eq, "liga", partidos.getliga().getValue());
            agregar(doc , eq, "estado", partidos.estadoProperty().getValue());

            agregar(doc, eq, "idEquipoLocal", partidos.getlocal().idEquipoProperty().getValue());
            agregar(doc, eq, "idEquipoVisitante", partidos.getvisitante().idEquipoProperty().getValue());

        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        //sinceramente esto no se para que es pero el profe lo metio, para algo sera
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        t.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());

        try (OutputStream out = Files.newOutputStream(RutapartidosXML, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
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
