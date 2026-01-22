package Data;

import DTO.Equipos;
import DTO.LigaDTO;
import DTO.PartidosDTO;
import javafx.collections.FXCollections;
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

public class DataGestorLiga {
    private static DataGestorLiga instance;
    private final Path RutaligasXML;


    private final AtomicInteger idcounter = new AtomicInteger(1);
    private ObservableList<LigaDTO> ligas;
    private ObservableList<Equipos> todoslosequipos;


    private FilteredList<LigaDTO> ligasfiltradas;
    private final FilteredList<Equipos> equiposFiltrados;


    public DataGestorLiga(Path RutaligasXML) {
        this.RutaligasXML = RutaligasXML;
        todoslosequipos = FXCollections.observableArrayList();
        ligas = javafx.collections.FXCollections.observableArrayList();
        ligasfiltradas = new FilteredList<>(ligas);
        equiposFiltrados = new FilteredList<>(todoslosequipos);

    }

    public static DataGestorLiga getInstance(Path RutaligasXML) {
        if (instance == null) {
            instance = new DataGestorLiga(RutaligasXML);
        }
        return instance;
    }

    public ObservableList<LigaDTO> getLigas() {
        return ligas;
    }

    public FilteredList<LigaDTO> getLigasfiltradas() {
        return ligasfiltradas;
    }

    public void agregarLiga(String nombre, String region) {
        String id = String.valueOf(idcounter.getAndIncrement()); // consultar esto con el profe
        LigaDTO liga = new LigaDTO(id, nombre, region);
        ligas.add(liga);
    }

    public ObservableList<Equipos> getEquiposDeLiga(LigaDTO liga) {
        return liga.getequipos();

    }

    public void agregarEquipoALiga(LigaDTO liga, DTO.Equipos equipo) {
        liga.getequipos().add(equipo);
        todoslosequipos.add(equipo);

    }

    public ObservableList<Equipos> getTodosLosEquipos() {
        return todoslosequipos;
    }

    public FilteredList<Equipos> getEquiposFiltrados() {
        return equiposFiltrados;
    }

    public ObservableList<Equipos> getEquiposPorLiga(String nombreLiga) {
        for (LigaDTO liga : ligas) {
            if (liga.getNombre().get().equals(nombreLiga)) {
                return FXCollections.observableArrayList(liga.getEquipos());
            }
        }
        return FXCollections.observableArrayList();
    }

    public ObservableList<PartidosDTO> getTodosLosPartidos() {
        ObservableList<PartidosDTO> todosLosPartidos = FXCollections.observableArrayList();
        for (LigaDTO liga : ligas) {
            todosLosPartidos.addAll(liga.getpartidos());
        }
        return todosLosPartidos;
    }

    public ObservableList<PartidosDTO> getPartidosPorLiga(String nombreLiga) {
        for (LigaDTO liga : ligas) {
            if (liga.nombreLigaProperty().get().equals(nombreLiga)) {
                return liga.getpartidos();
            }
        }
        return FXCollections.observableArrayList();
    }

    public LigaDTO buscarLigaPorNombre(String nombreLiga) {
        for (LigaDTO liga : ligas) {
            if (liga.nombreLigaProperty().get().equals(nombreLiga)) {
                return liga;
            }
        }
        return null;
    }


    public boolean verificarcamposequipos(String nombreLiga) {
        LigaDTO liga = buscarLigaPorNombre(nombreLiga);
        return liga != null && liga.getpartidos().size() >= 25;
    }

    public boolean verificarcampospartido(String nombreLiga) {
        LigaDTO liga = buscarLigaPorNombre(nombreLiga);
        return liga != null && liga.getequipos().size() >= 15;
    }

    public boolean equipoTienePartidos(Equipos equipo) {
        return DataPartidos.getInstance(null).getPartidos().stream()
                .anyMatch(p ->
                        p.getlocal().idEquipoProperty().get()
                                .equals(equipo.idEquipoProperty().get())
                                || p.getvisitante().idEquipoProperty().get()
                                .equals(equipo.idEquipoProperty().get())
                );
    }


    public boolean equipoEnOtraLiga(Equipos equipo, String ligaActual, DataPartidos dataPartidos) {
        String idEquipo = equipo.idEquipoProperty().get();

        boolean tienePartidosEnOtraLiga = dataPartidos.getPartidos().stream()
                .anyMatch(p -> {
                    boolean esLocal = p.getlocal().idEquipoProperty().get().equals(idEquipo);
                    boolean esVisitante = p.getvisitante().idEquipoProperty().get().equals(idEquipo);
                    boolean enOtraLiga = !p.getliga().get().equals(ligaActual);
                    return (esLocal || esVisitante) && enOtraLiga;
                });

        return tienePartidosEnOtraLiga;
    }

    public boolean equipoYaEnLiga(Equipos equipo, String nombreLiga) {
        LigaDTO liga = buscarLigaPorNombre(nombreLiga);
        if (liga == null) return false;

        String idEquipo = equipo.idEquipoProperty().get();
        return liga.getequipos().stream()
                .anyMatch(e -> e.idEquipoProperty().get().equals(idEquipo));
    }



    public void actualizarContadorId() {
        int maxId = 0;
        for (LigaDTO liga : ligas) {
            try {
                int idActual = Integer.parseInt(liga.idLigaProperty().getValue());
                if (idActual > maxId) {
                    maxId = idActual;
                }
            } catch (NumberFormatException e) {
            }
        }
        idcounter.set(maxId + 1);
    }
    private Equipos buscarEquipoPorId(DataEquipos dataEquipos, String id) {
        return dataEquipos.getEquipos().stream()
                .filter(eq -> eq.idEquipoProperty().getValue().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<LigaDTO> cargar() throws Exception {
        if (!Files.exists(RutaligasXML)) {
            //si no hay, retornar lista vacia
            return new ArrayList<>();
        }
        Document doc;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringComments(true);
        dbf.setNamespaceAware(false);

        DocumentBuilder db = dbf.newDocumentBuilder();

        try (InputStream in = Files.newInputStream(RutaligasXML)) {
            doc = db.parse(in);
        }

        doc.getDocumentElement().normalize();

        List<LigaDTO> listaLigas = new ArrayList<>();
        NodeList nodolist = doc.getElementsByTagName("liga");
        DataEquipos dataEquipos = DataEquipos.getInstance(Path.of("Data/equipos.xml"));

        DataPartidos dataPartidos = DataPartidos.getInstance(Path.of("Data/partidos.xml"));
        dataPartidos.getPartidos().setAll(dataPartidos.cargar());

        for (int i = 0; i < nodolist.getLength(); i++) {
            Node n = nodolist.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) continue;

            Element ligaElement = (Element) n;

            String idLiga = getText(ligaElement, "idliga");
            String nombreLiga = getText(ligaElement, "nombreliga");
            String region = getText(ligaElement, "region");

            LigaDTO liga = new LigaDTO(idLiga, nombreLiga, region);
            List<String> idsEquiposAgregados = new ArrayList<>();

            // BUSCAR PARTIDOS QUE PERTENECEN A ESTA LIGA
            for (PartidosDTO partido : dataPartidos.getPartidos()) {
                String ligaPartido = partido.getliga().get();
                if (ligaPartido != null && !ligaPartido.isEmpty() && ligaPartido.equals(nombreLiga))  {

                    // Agregar el partido a la liga
                    liga.getpartidos().add(partido);

                    // Agregar los equipos
                    String idLocal = partido.getlocal().idEquipoProperty().getValue();
                    String idVisitante = partido.getvisitante().idEquipoProperty().getValue();

                    if (idsEquiposAgregados.add(idLocal)) {
                        liga.getequipos().add(partido.getlocal());
                    }
                    if (idsEquiposAgregados.add(idVisitante)) {
                        liga.getequipos().add(partido.getvisitante());
                    }
                }
            }

            listaLigas.add(liga);
        }
        return listaLigas;
    }

    public void guardar(List<LigaDTO> listaligas) throws Exception {
        if (RutaligasXML.getParent() != null) {
            Files.createDirectories(RutaligasXML.getParent());
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element root = doc.createElement("ligas");
        doc.appendChild(root);

        for (  LigaDTO ligas : listaligas) {
           Element eq = doc.createElement("liga");
            root.appendChild(eq);
            // info de la liga
            agregar(doc, eq, "idliga", ligas.idLigaProperty().getValue());
            agregar(doc, eq, "nombreliga", ligas.nombreLigaProperty().getValue());
            agregar(doc, eq, "region", ligas.regionligaproperty().getValue());
            // info de los partidos de la liga
            for (PartidosDTO partido : ligas.getpartidos()) {
                Element partidoElement = doc.createElement("partido");
                eq.appendChild(partidoElement);

                // Información del partido
                agregar(doc, partidoElement, "idPartido", partido.idpartidoProperty().getValue());
                agregar(doc, partidoElement, "nombrePartido", partido.nombrepartidoProperty().getValue());
                agregar(doc, partidoElement, "jornada", partido.jornadasProperty().getValue());
                agregar(doc, partidoElement, "estadio", partido.estadioProperty().getValue());
                agregar(doc, partidoElement, "fecha", partido.getfecha().toString());
                agregar(doc, partidoElement, "liga", ligas.nombreLigaProperty().getValue());
                agregar(doc, partidoElement, "estado", partido.estadoProperty().getValue());

                // Referencias a los equipos (solo IDs, no el objeto completo)
                agregar(doc, partidoElement, "idEquipoLocal", partido.getlocal().idEquipoProperty().getValue());
                agregar(doc, partidoElement, "idEquipoVisitante", partido.getvisitante().idEquipoProperty().getValue());
            }
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        //sinceramente esto no se para que es pero el profe lo metio, para algo sera
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        t.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());

        try (OutputStream out = Files.newOutputStream(RutaligasXML, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            t.transform(new DOMSource(doc), new StreamResult(out));
        }
    }

    //Utilidades para XML................................................
    private String getText(org.w3c.dom.Element e, String tag) {
        NodeList nl = e.getElementsByTagName(tag);
        if (nl.getLength() == 0) return "";
        return nl.item(0).getTextContent();
    }

    private static void agregar(Document doc, org.w3c.dom.Element patent, String tag, String value){
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

    public void filtrarPorLiga(LigaDTO ligaSeleccionada) {
        if (ligaSeleccionada == null) {
            ligasfiltradas.setPredicate(liga -> true);
        } else {
            ligasfiltradas.setPredicate(liga -> liga.equals(ligaSeleccionada));
        }
    }
}






