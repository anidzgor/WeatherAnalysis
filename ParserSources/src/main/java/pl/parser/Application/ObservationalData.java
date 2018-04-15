package pl.parser.Application;

import org.apache.commons.math3.util.Precision;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import pl.parser.Domain.Station;
import pl.parser.Implementation.MapCreator;
import pl.parser.Implementation.SynopComponent;
import pl.parser.Implementation.WRFComponent;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ObservationalData {
    private static String observationalData = "C:\\KSG\\Resources\\ObservationalData";
    private String[] cities;
    private MapCreator map;

    public ObservationalData(MapCreator mapCreator) throws IOException, SAXException, ParserConfigurationException {
        map = mapCreator;
        this.cities = mapCreator.getStationFromXML();
    }

    public void predictFiveHours() throws IOException, SAXException, ParserConfigurationException, TransformerException {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.set(2018, 03, 12, 14, 0);  //temporary solution for test, later must be remove
        String date = new SimpleDateFormat("yyyy-MM-dd_HH").format(calendar.getTime());
        String hour = new SimpleDateFormat("HH").format(calendar.getTime());
        String monthWRF = new SimpleDateFormat("M").format(calendar.getTime());
        String dayWRF = new SimpleDateFormat("d").format(calendar.getTime());

        WRFComponent wrfComponent = new WRFComponent();
        SynopComponent synopComponent = new SynopComponent();

        int start = Integer.parseInt(hour);
        int end = start + 5;

        double wrfTable[] = new double[6];
        double synopTable[] = new double[6];
        double predicted[] = new double[6];

        double diff = 0.0;

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("ObservationalDatas");
        doc.appendChild(rootElement);

        for (String nameStation : cities) {
            Station wrf = wrfComponent.getTemperature(nameStation, date);
            Station synop = synopComponent.getTemperature(nameStation, date);
            if(synop == null)
                break;
            double tempSYNOP = synop.getTemperature();

            for(int i = start; i <= end; i++) {

                double tempWRFBefore = wrfComponent.readCellFromCSV( wrf.getSourceFile() + "/" + monthWRF + "/" + dayWRF + "/" + (i - 1) + "/SHELTER_TEMPERATURE.CSV", wrf.getCoordinatesCSV()[0], wrf.getCoordinatesCSV()[1]);
                double tempWRFPresent = wrfComponent.readCellFromCSV(wrf.getSourceFile() + "/" + monthWRF + "/" + dayWRF + "/" + i + "\\SHELTER_TEMPERATURE.CSV", wrf.getCoordinatesCSV()[0], wrf.getCoordinatesCSV()[1]);
                double tempWRFAfter = wrfComponent.readCellFromCSV(wrf.getSourceFile() + "/" + monthWRF + "/" + dayWRF + "/" + (i + 1) + "\\SHELTER_TEMPERATURE.CSV", wrf.getCoordinatesCSV()[0], wrf.getCoordinatesCSV()[1]);

                //Calculate diff
                if(i == start) {
                    diff = tempSYNOP - tempWRFBefore;
                    synopTable[i - start] = tempSYNOP;
                }

                double mean = (tempWRFBefore + tempWRFPresent+ tempWRFAfter) / 3;
                double result = mean + diff;

                wrfTable[i - start] = tempWRFPresent;
                predicted[i - start] = result;

                diff = diff * 0.7;
            }

            //City
            Element city = doc.createElement("City");
            rootElement.appendChild(city);
            Attr attr = doc.createAttribute("name");    // set name of city
            attr.setValue(nameStation);
            city.setAttributeNode(attr);

            for(int i = start; i <= end; i++) {

                Element Hour = doc.createElement("Time");
                city.appendChild(Hour);
                Attr hourAttr = doc.createAttribute("hour");
                hourAttr.setValue(String.valueOf(i));
                Hour.setAttributeNode(hourAttr);

                Element WRF = doc.createElement("WRF");
                WRF.appendChild(doc.createTextNode(String.valueOf(Precision.round(wrfTable[i - start], 2))));
                Hour.appendChild(WRF);

                Element prediction = doc.createElement("Prediction");
                prediction.appendChild(doc.createTextNode(String.valueOf(Precision.round(predicted[i - start], 2))));
                Hour.appendChild(prediction);
            }

//            System.out.println("City: " + nameStation);
//            for(int i = start; i <= end; i++)
//                System.out.println("WRF P: " + Precision.round(wrfTable[i - start], 2)
//                        + ", Predicted: " + Precision.round(predicted[i - start], 2)
//                        + ", SYNOP: " + Precision.round(synopTable[i - start], 2));
        }

        //write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        File pathFolder = new File(observationalData);

        if(!pathFolder.exists())
            pathFolder.mkdir();

        StreamResult result = new StreamResult(new File(pathFolder + "\\" + date + ".xml"));
        transformer.transform(source, result);
    }
}