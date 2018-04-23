package pl.parser.Tests;

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
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public class TestCompareResults {

    private static int maxIteration = 1000;    //max iteration
    private static int amountRandomlyCities = 20;
    private static int restCitiesFromSYNOP = 40;
    private static String date = "2018-04-16_10";
    private static String pathCrossValidation = "C:\\KSG\\Resources\\Tests\\";

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        Random random = new Random();

        MapCreator mapCreator = new MapCreator();
        String[] allCities = mapCreator.getStationFromXML();
        SynopComponent synopComponent = new SynopComponent();
        WRFComponent wrfComponent = new WRFComponent();

        int start = Integer.parseInt(date.substring(11, 13));
        int end = start + 5;
        double differenceBetweenWRFAndSYNOPBefore = 0.0;
        int amountOfTemperatures = 6;
        double wrfTable[] = new double[amountOfTemperatures];
        double synopTable[] = new double[amountOfTemperatures];
        double predicted[] = new double[amountOfTemperatures];

        //This station is only need because we need a source a file of WRF, which we must to know beacuase we want predict
        Station wrfFile = wrfComponent.getTemperature(allCities[0], date);

        List<Station> stationsWithKnowTemperature = new ArrayList<>();
        List<Station> stationsWithUnknownTemperature = new ArrayList<>();

        //Create XML file
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("Leave-P-Out-Cross-Validation");
        doc.appendChild(rootElement);

        for(int i = 0; i < maxIteration; i++) {
            //System.out.println("Iteration: " + i);
            List<String> knownCitiesFromSYNOP = new ArrayList<>(Arrays.asList(allCities));
            Map<Station, double[]> citiesWithPredictedTemperatures = new HashMap<>();

            //Randomly choosen cities which we must compute
            IntStream stream = random.ints(0, 60).distinct().limit(20).sorted();
            List<String> randomlyCities = new ArrayList<>(amountRandomlyCities);
            int[] randomlyChoosenSetOfCities = stream.toArray();
            double msePrediction = 0.0;
            double mseWRF = 0.0;

            //Write iteration to xml file
            Element iteration = doc.createElement("Iteration" + i);
            rootElement.appendChild(iteration);

            for(int c = 0; c < amountRandomlyCities; c++) {
                randomlyCities.add(allCities[randomlyChoosenSetOfCities[c]]);

                Station station = new Station(allCities[randomlyChoosenSetOfCities[c]], date);
                station.initialiazeCoordinates(wrfComponent.getCoordinates(station.getNameStation()));
                stationsWithUnknownTemperature.add(station);

                //We calculate a temperature in randomly city for six hours - prediction
                for (int temp = start; temp <= end; temp++) {
                    double tempWRFBefore = wrfComponent.readCellFromCSV(wrfFile.getSourceFile() + "/" +
                            Integer.parseInt(date.substring(5, 7)) + "/" + Integer.parseInt(date.substring(8, 10)) + "/" +
                            (temp - 1) + "\\SHELTER_TEMPERATURE.CSV", station.getCoordinatesCSV()[0], station.getCoordinatesCSV()[1]);

                    double tempWRFPresent = wrfComponent.readCellFromCSV(wrfFile.getSourceFile() + "/" +
                            Integer.parseInt(date.substring(5, 7)) + "/" + Integer.parseInt(date.substring(8, 10)) + "/" +
                            temp + "\\SHELTER_TEMPERATURE.CSV", station.getCoordinatesCSV()[0], station.getCoordinatesCSV()[1]);

                    double tempWRFAfter = wrfComponent.readCellFromCSV(wrfFile.getSourceFile() + "/" +
                            Integer.parseInt(date.substring(5, 7)) + "/" + Integer.parseInt(date.substring(8, 10)) + "/" +
                            (temp + 1) + "\\SHELTER_TEMPERATURE.CSV", station.getCoordinatesCSV()[0], station.getCoordinatesCSV()[1]);

                    double tempSYNOPPresent;
                    try {
                        if(temp == start) {
                            double tempSYNOPBefore = synopComponent.getTemperature(station.getNameStation(), date.substring(0, 10) + "_" + (temp - 1)).getTemperature();
                            differenceBetweenWRFAndSYNOPBefore = tempSYNOPBefore - tempWRFBefore;
                        }
                        tempSYNOPPresent = synopComponent.getTemperature(station.getNameStation(), date.substring(0, 10) + "_" + temp).getTemperature();
                    } catch (Exception e) {
                        System.out.println("Not found files");
                        break;
                    }

                    double mean = (tempWRFBefore + tempWRFPresent + tempWRFAfter) / 3;
                    double result = mean + differenceBetweenWRFAndSYNOPBefore;
                    wrfTable[temp - start] = tempWRFPresent;
                    synopTable[temp - start] = tempSYNOPPresent;
                    predicted[temp - start] = result;

                    differenceBetweenWRFAndSYNOPBefore *= 0.7;
                }
                citiesWithPredictedTemperatures.put(station, Arrays.copyOf(predicted, predicted.length));

                //Calculate MSE
                msePrediction += Math.pow(Precision.round(predicted[0], 2) - Precision.round(synopTable[0], 2), 2);
                mseWRF += Math.pow(Precision.round(wrfTable[0], 2) - Precision.round(synopTable[0], 2), 2);

                Element city = doc.createElement("Station");
                iteration.appendChild(city);
                Attr attr = doc.createAttribute("data");
                attr.setValue("Station: " + station.getNameStation() + " - SYNOP: " + Precision.round(synopTable[0], 2) + ", WRF: " +
                        Precision.round(wrfTable[0], 2) + ", Predicted: " + Precision.round(predicted[0], 2));
                city.setAttributeNode(attr);

//                System.out.println("Station: " + station.getNameStation() + " - SYNOP: " + Precision.round(synopTable[0], 2) +
//                        ", WRF: " + Precision.round(wrfTable[0], 2) + ", Predicted: " + Precision.round(predicted[0], 2));
            }
            msePrediction /= amountRandomlyCities;
            mseWRF /= amountRandomlyCities;
//            System.out.println("MSE_prediction: " + msePrediction);
//            System.out.println("MSE_WRF: " + mseWRF);

            Element msePred = doc.createElement("MSE_PREDICTION");
            msePred.appendChild(doc.createTextNode(String.valueOf(msePrediction)));
            iteration.appendChild(msePred);

            Element mseWrf = doc.createElement("MSE_WRF");
            mseWrf.appendChild(doc.createTextNode(String.valueOf(mseWRF)));
            iteration.appendChild(mseWrf);

            //Cities which are known
//            knownCitiesFromSYNOP.removeAll(randomlyCities);
//            for(int c = 0; c < restCitiesFromSYNOP; c++) {
//                Station station = synopComponent.getTemperature(knownCitiesFromSYNOP.get(c), date);
//                station.initialiazeCoordinates(wrfComponent.getCoordinates(station.getNameStation()));
//                stationsWithKnowTemperature.add(station);
//            }
        }
        //write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        File pathFolder = new File(pathCrossValidation);

        if(!pathFolder.exists())
            pathFolder.mkdir();

        StreamResult result = new StreamResult(new File(pathFolder + "\\" + date + ".xml"));
        transformer.transform(source, result);
    }
}
