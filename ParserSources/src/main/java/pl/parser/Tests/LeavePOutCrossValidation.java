package pl.parser.Tests;

import org.apache.commons.math3.util.Precision;
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
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class LeavePOutCrossValidation {
    private static int amountTestingSet = 20;
    private static int amountTrainingSet = 40;
    private static String date = "2018-03-09_07";
    public static String pathSources = "C:/KSG/Resources/";
    private static int maxIteration = 60;    //max iteration
    private static String pathCrossValidation = "C:\\KSG\\Resources\\Tests\\";

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, TransformerException {

        MapCreator mapCreator = new MapCreator();
        String[] allCities = mapCreator.getStationFromXML();
        SynopComponent synopComponent = new SynopComponent();
        WRFComponent wrfComponent = new WRFComponent();

        File folder = new File("C:\\KSG\\SYNOP\\");
        File[] listOfFiles = folder.listFiles();

        List<String> dates = new ArrayList<>(listOfFiles.length);
        for(File file: listOfFiles)
            dates.add(file.getName().substring(0, 13));

        for (String date: dates) {

            //Create XML file
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Leave-P-Out-Cross-Validation");
            doc.appendChild(rootElement);

            for (int i = 0; i < maxIteration; i++) {
                //Randomly choose Validation Set
                Random random = new Random();
                IntStream stream = random.ints(0, 60).distinct().limit(20).sorted();
                int[] randomlyChoosenSetOfCities = stream.toArray();

                //Testing Set
                List<String> randomlyCities = new ArrayList<>(amountTestingSet);
                List<Station> stationsWithUnknownTemperature = new ArrayList<>();
                for (int c = 0; c < amountTestingSet; c++) {
                    randomlyCities.add(allCities[randomlyChoosenSetOfCities[c]]);   //It is helpful to extract to sets training and testing
                    Station station = synopComponent.getTemperature(allCities[randomlyChoosenSetOfCities[c]], date);  //Create station based on name from list
                    station.initialiazeCoordinates(wrfComponent.getCoordinates(station.getNameStation()));  //Get coordinates
                    stationsWithUnknownTemperature.add(station);
                }

                //Training Set
                List<String> knownCitiesFromSYNOP = new ArrayList<>(Arrays.asList(allCities));
                knownCitiesFromSYNOP.removeAll(randomlyCities);
                List<Station> stationsWithKnowTemperature = new ArrayList<>();
                for (int c = 0; c < amountTrainingSet; c++) {
                    Station station = synopComponent.getTemperature(knownCitiesFromSYNOP.get(c), date);     //Create station and get temperature synop
                    station.initialiazeCoordinates(wrfComponent.getCoordinates(station.getNameStation()));  //Get coordinates for this station
                    stationsWithKnowTemperature.add(station);
                }

                List<PointMap> points = new ArrayList<>();
                for (Station station : stationsWithKnowTemperature) { //For each know station create point with temperature and coordinates
                    PointMap p = new PointMap(station.getTemperature(), station.getCoordinatesCSV());
                    points.add(p);
                }

                String filePath = "Excel/" + date.substring(0, 13) + ".csv";

                mapCreator.createCSVWithInterpolation(filePath, points);
                mapCreator.createMapImage(date.substring(0, 13), 'p');

                //Write iteration to xml file
                Element iteration = doc.createElement("Iteration" + i);
                rootElement.appendChild(iteration);

                double rmsePrediction = 0.0;
                //Get temperature for each station from Testing Set
                for (Station station : stationsWithUnknownTemperature) {
                    double tempFromInterpolation = WRFComponent.readCellFromCSV(pathSources + filePath, station.getCoordinatesCSV()[0],
                            station.getCoordinatesCSV()[1]);
                    double tempValidation = station.getTemperature();

                    System.out.println(station.getNameStation() + " - Interpolation: " + Precision.round(tempFromInterpolation, 2) + ", Validation: " + tempValidation);
                    rmsePrediction += Math.pow(Precision.round(tempFromInterpolation, 2) - Precision.round(tempValidation, 2), 2);

                    Element city = doc.createElement("Station");
                    iteration.appendChild(city);
                    Attr attr = doc.createAttribute("data");
                    attr.setValue("Station: " + station.getNameStation() + " - Interpolation: " + Precision.round(tempFromInterpolation, 2) + ", Validation: " + tempValidation);
                    city.setAttributeNode(attr);
                }
                rmsePrediction /= amountTestingSet;
                System.out.println("RMSE: " + Math.sqrt(rmsePrediction));
                Element msePred = doc.createElement("RMSE");
                msePred.appendChild(doc.createTextNode(String.valueOf(Math.sqrt(rmsePrediction))));
                iteration.appendChild(msePred);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            File pathFolder = new File(pathCrossValidation);

            if (!pathFolder.exists())
                pathFolder.mkdir();

            StreamResult result = new StreamResult(new File(pathFolder + "\\" + date + ".xml"));
            transformer.transform(source, result);
        }

//        System.out.println("Testing Set: " + randomlyCities.toString());
//        System.out.println("Training Set: " + knownCitiesFromSYNOP.toString());

    }
}
