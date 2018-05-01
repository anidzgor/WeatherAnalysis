package pl.parser.Application;

import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import pl.parser.Domain.PointMap;
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
import java.util.*;

public class ArchiveData {
    private static String pathArchiveData = "C:\\KSG\\Resources\\ArchiveData\\";
    public static String pathSources = "C:/KSG/Resources/";
    private String[] cities;
    private MapCreator map;

    public ArchiveData(MapCreator mapCreator) throws IOException, SAXException, ParserConfigurationException {
        map = mapCreator;
        this.cities = mapCreator.getStationFromXML();
    }

    public void showFiveHoursWithPrediction(String date) throws IOException, ParserConfigurationException, SAXException, TransformerException {

        WRFComponent wrfComponent = new WRFComponent();
        SynopComponent synopComponent = new SynopComponent();

        double tempSYNOPBefore = 0.0;
        double tempSYNOPPresent = 0.0;

        double diff = 0.0;
        int start = Integer.parseInt(date.substring(11, 13));
        int end = start + 5;

        int amountOfTemperatures = 6;
        double wrfTable[] = new double[amountOfTemperatures];
        double synopTable[] = new double[amountOfTemperatures];
        double predicted[] = new double[amountOfTemperatures];

        boolean endSearch = false;

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("ArchiveDatas");
        doc.appendChild(rootElement);

        int counterTrue = 0;
        int counterFalse = 0;

        Map<String, double[]> citiesWithPredictedTemperatures = new HashMap<>();

        for (String nameStation : cities) {
            Station wrf = wrfComponent.getTemperature(nameStation, date);

            if(endSearch)
                break;

            for (int i = start; i <= end; i++) {

                double tempWRFBefore = wrfComponent.readCellFromCSV(wrf.getSourceFile() + "/" + Integer.parseInt(date.substring(5, 7)) + "/" + Integer.parseInt(date.substring(8, 10)) + "/" + (i - 1) + "\\SHELTER_TEMPERATURE.CSV", wrf.getCoordinatesCSV()[0], wrf.getCoordinatesCSV()[1]);
                double tempWRFPresent = wrfComponent.readCellFromCSV(wrf.getSourceFile() + "/" + Integer.parseInt(date.substring(5, 7)) + "/" + Integer.parseInt(date.substring(8, 10)) + "/" + i + "\\SHELTER_TEMPERATURE.CSV", wrf.getCoordinatesCSV()[0], wrf.getCoordinatesCSV()[1]);
                double tempWRFAfter = wrfComponent.readCellFromCSV(wrf.getSourceFile() + "/" + Integer.parseInt(date.substring(5, 7)) + "/" + Integer.parseInt(date.substring(8, 10)) + "/" + (i + 1) + "\\SHELTER_TEMPERATURE.CSV", wrf.getCoordinatesCSV()[0], wrf.getCoordinatesCSV()[1]);

                try {
                    if(i == start) {
                        tempSYNOPBefore = synopComponent.getTemperature(nameStation, date.substring(0, 10) + "_" + (i - 1)).getTemperature();
                        diff = tempSYNOPBefore - tempWRFBefore;
                    }
                    tempSYNOPPresent = synopComponent.getTemperature(nameStation, date.substring(0, 10) + "_" + i).getTemperature();
                } catch (Exception e) {
                    System.out.println("Not found files");
                    endSearch = true;
                    break;
                }

                double mean = (tempWRFBefore + tempWRFPresent + tempWRFAfter) / 3;
                double result = mean + diff;
                wrfTable[i - start] = tempWRFPresent;
                synopTable[i - start] = tempSYNOPPresent;
                predicted[i - start] = result;

                diff = diff * 0.7;
            }

            citiesWithPredictedTemperatures.put(nameStation, Arrays.copyOf(predicted, predicted.length));

            //City
            Element city = doc.createElement("City");
            rootElement.appendChild(city);

            //Set name of city
            Attr attr = doc.createAttribute("name");
            attr.setValue(nameStation);
            city.setAttributeNode(attr);

            if(!endSearch) {
                for(int i = start; i <= end; i++) {

                    Element hour = doc.createElement("Time");
                    city.appendChild(hour);
                    Attr hourAttr = doc.createAttribute("hour");
                    hourAttr.setValue(String.valueOf(i));
                    hour.setAttributeNode(hourAttr);

                    Element WRF = doc.createElement("WRF");
                    WRF.appendChild(doc.createTextNode(String.valueOf(Precision.round(wrfTable[i - start], 2))));
                    hour.appendChild(WRF);

                    Element prediction = doc.createElement("Prediction");
                    prediction.appendChild(doc.createTextNode(String.valueOf(Precision.round(predicted[i - start], 2))));
                    hour.appendChild(prediction);

                    Element SYNOP = doc.createElement("SYNOP");
                    SYNOP.appendChild(doc.createTextNode(String.valueOf(Precision.round(synopTable[i - start], 2))));
                    hour.appendChild(SYNOP);

                    String correction = "false";
                    //Check correction temperature
                    if(Math.abs(wrfTable[i - start] - synopTable[i - start]) > Math.abs(predicted[i - start] - synopTable[i - start])) {
                        counterTrue++;
                        correction = "true";
                    }
                    else
                        counterFalse++;

                    Element correct = doc.createElement("Correct");
                    correct.appendChild(doc.createTextNode(correction));
                    hour.appendChild(correct);
                }
            }
        }

        if(endSearch)
            return;

        //write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        File pathFolder = new File(pathArchiveData);

        if(!pathFolder.exists())
            pathFolder.mkdir();

        StreamResult result = new StreamResult(new File(pathFolder + "\\" + date + ".xml"));
        transformer.transform(source, result);

        System.out.println("True: " + counterTrue + ", False: " + counterFalse + ", Percentage of correction: " +
                (counterTrue / (double)(counterTrue + counterFalse)) * 100 + "%");

        try {
            //Model
            for (int i = 0; i < amountOfTemperatures; i++) {
                List<PointMap> points = new ArrayList<>();
                for (String city : cities) {
                    PointMap p = new PointMap(citiesWithPredictedTemperatures.get(city)[i], wrfComponent.getCoordinates(city));
                    points.add(p);
                }
                String filePath = "Excel/ArchiveData/Model/" + date.substring(0, 11) + start + "/" + date.substring(0, 11) + (start + i) + ".csv";
                new File(pathSources + "/Excel/ArchiveData/Model/" + date.substring(0, 11) + start + "/").mkdirs();
                map.createCSVWithInterpolation(filePath, points);
                map.createMapImage(date.substring(0, 11) + start, date.substring(0, 11) + (start + i), 'a');
            }

            //WRF
            for (int i = 0; i < amountOfTemperatures; i++) {
                List<PointMap> points = new ArrayList<>();
                for (String city : cities) {
                    Station wrf = wrfComponent.getTemperature(city, date);
                    double tempWRF = wrfComponent.readCellFromCSV(wrf.getSourceFile() + "/" + Integer.parseInt(date.substring(5, 7)) + "/" + Integer.parseInt(date.substring(8, 10)) + "/" + (start + i) + "\\SHELTER_TEMPERATURE.CSV", wrf.getCoordinatesCSV()[0], wrf.getCoordinatesCSV()[1]);
                    PointMap p = new PointMap(tempWRF, wrfComponent.getCoordinates(city));
                    points.add(p);
                }
                String filePath = "Excel/ArchiveData/WRF/" + date.substring(0, 11) + start + "/" + date.substring(0, 11) + (start + i) + ".csv";
                new File(pathSources + "/Excel/ArchiveData/WRF/" + date.substring(0, 11) + start + "/").mkdirs();
                map.createCSVWithInterpolation(filePath, points);
                map.createMapImage(date.substring(0, 11) + start, date.substring(0, 11) + (start + i), 'b');
            }

            //IDW
            for (int i = 0; i < amountOfTemperatures; i++) {
                List<PointMap> points = new ArrayList<>();
                for (String city : cities) {
                    Station wrf = wrfComponent.getTemperature(city, date);
                    double tempWRF = wrfComponent.readCellFromCSV(wrf.getSourceFile() + "/" + Integer.parseInt(date.substring(5, 7)) + "/" + Integer.parseInt(date.substring(8, 10)) + "/" + (start + i) + "\\SHELTER_TEMPERATURE.CSV", wrf.getCoordinatesCSV()[0], wrf.getCoordinatesCSV()[1]);
                    double tempSynop = synopComponent.getTemperature(city, date.substring(0, 10) + "_" + (start + i)).getTemperature();
                    PointMap p = new PointMap(Math.abs(tempWRF - tempSynop), wrfComponent.getCoordinates(city));
                    points.add(p);
                }
                String filePath = "Excel/ArchiveData/IDW/" + date.substring(0, 11) + start + "/" + date.substring(0, 11) + (start + i) + ".csv";
                new File(pathSources + "/Excel/ArchiveData/IDW/" + date.substring(0, 11) + start + "/").mkdirs();
                map.createCSVWithInterpolation(filePath, points);
                map.createMapImage(date.substring(0, 11) + start, date.substring(0, 11) + (start + i), 'i');
            }
        }catch(Exception e) {
            System.out.println("No found file(Synop or WRF)");
        }
    }
}
