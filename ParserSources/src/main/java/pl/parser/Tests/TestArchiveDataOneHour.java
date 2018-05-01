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

public class TestArchiveDataOneHour {
    private static int amountTestingSet = 10;
    private static int amountTrainingSet = 50;
    private static String date = "2018-03-09_07";
    public static String pathSources = "C:/KSG/Resources/";
    private static int maxIteration = 50;    //max iteration
    private static String pathCrossValidation = "C:\\KSG\\Resources\\Tests\\ArchiveData\\";

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, TransformerException {

        MapCreator mapCreator = new MapCreator();
        String[] allCities = mapCreator.getStationFromXML();
        SynopComponent synopComponent = new SynopComponent();
        WRFComponent wrfComponent = new WRFComponent();

        boolean flagNoFileSYNOP = false;
        double globalPercentageCorrection = 0.0;
        int amountCheckedFile = 0;

        File folder = new File("C:\\KSG\\SYNOP\\");
        File[] listOfFiles = folder.listFiles();

        List<String> dates = new ArrayList<>(listOfFiles.length);
        for(File file: listOfFiles)
            dates.add(file.getName().substring(0, 13));

        for (String date: dates) {
            int correct = 0;

            int start = Integer.parseInt(date.substring(11, 13));
            if(start == 0 || start == 23)
                continue;

            //Create XML file
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Leave-P-Out-Archive-Data");
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

                    if(station == null) {
                        flagNoFileSYNOP = true;
                        break;
                    }
                    station.initialiazeCoordinates(wrfComponent.getCoordinates(station.getNameStation()));  //Get coordinates
                    stationsWithUnknownTemperature.add(station);
                }

                if (flagNoFileSYNOP) {
                    break;
                }

                //Training Set
                List<String> knownCitiesFromSYNOP = new ArrayList<>(Arrays.asList(allCities));
                knownCitiesFromSYNOP.removeAll(randomlyCities);
                List<Station> stationsWithKnowTemperature = new ArrayList<>();
                for (int c = 0; c < amountTrainingSet; c++) {

                    String currentStation = knownCitiesFromSYNOP.get(c);

                    Station station = new Station(currentStation, date);
                    station.initialiazeCoordinates(wrfComponent.getCoordinates(station.getNameStation()));  //Get coordinates for this station

                    //This station is created for only get a source file WRF
                    Station wrfSource = wrfComponent.getTemperature(knownCitiesFromSYNOP.get(c), date);
                    try {

                        double tempWRFBefore = wrfComponent.readCellFromCSV(wrfSource.getSourceFile() + "/" +
                                        Integer.parseInt(date.substring(5, 7)) + "/" + Integer.parseInt(date.substring(8, 10)) +
                                        "/" + (start - 1) + "\\SHELTER_TEMPERATURE.CSV", wrfSource.getCoordinatesCSV()[0],
                                wrfSource.getCoordinatesCSV()[1]);

                        double tempWRFPresent = wrfComponent.readCellFromCSV(wrfSource.getSourceFile() + "/" +
                                Integer.parseInt(date.substring(5, 7)) + "/" + Integer.parseInt(date.substring(8, 10)) + "/" +
                                start + "\\SHELTER_TEMPERATURE.CSV", wrfSource.getCoordinatesCSV()[0], wrfSource.getCoordinatesCSV()[1]);

                        double tempWRFAfter = wrfComponent.readCellFromCSV(wrfSource.getSourceFile() + "/" +
                                Integer.parseInt(date.substring(5, 7)) + "/" + Integer.parseInt(date.substring(8, 10)) + "/" +
                                (start + 1) + "\\SHELTER_TEMPERATURE.CSV", wrfSource.getCoordinatesCSV()[0], wrfSource.getCoordinatesCSV()[1]);

                        double tempSYNOPBefore = synopComponent.getTemperature(currentStation, date.substring(0, 10) + "_" + (start - 1)).getTemperature();
                        double diff = tempSYNOPBefore - tempWRFBefore;
                        double mean = (tempWRFBefore + tempWRFPresent + tempWRFAfter) / 3;
                        double temperatureOnStation = mean + diff;
                        station.setTemperature(temperatureOnStation);
                        stationsWithKnowTemperature.add(station);

                    } catch (Exception e) {
                        System.out.println("Not founds neccessary files(WRF or SYNOP)");
                        flagNoFileSYNOP = true;
                    }

                    if(flagNoFileSYNOP)
                        break;
                }

                if(flagNoFileSYNOP) {
                    break;
                }

                List<PointMap> points = new ArrayList<>();
                for (Station station : stationsWithKnowTemperature) { //For each know station create point with temperature and coordinates
                    PointMap p = new PointMap(station.getTemperature(), station.getCoordinatesCSV());
                    points.add(p);
                }

                //Map 3 - Model
                String filePath = "Excel/TestArchiveData/Model/" + date.substring(0, 13) + ".csv";
                new File(pathSources + "/Excel/TestArchiveData/Model/").mkdirs();
                mapCreator.createCSVWithInterpolation(filePath, points);
                mapCreator.createMapImage(filePath.substring(27, filePath.length() - 4) + "/", date.substring(0, 13), 'e');

                if(i == (maxIteration - 1)) {
                    //Map 1 - create Map with clear WRF
                    List<PointMap> pointsWRF = new ArrayList<>();
                    for (String cityWRF : mapCreator.getStationFromXML()) {
                        Station wrfSource = wrfComponent.getTemperature(cityWRF, date);
                        try {
                            double tempWRFCity = wrfComponent.readCellFromCSV(wrfSource.getSourceFile() + "/" +
                                            Integer.parseInt(date.substring(5, 7)) + "/" + Integer.parseInt(date.substring(8, 10)) +
                                            "/" + start + "\\SHELTER_TEMPERATURE.CSV", wrfSource.getCoordinatesCSV()[0],
                                    wrfSource.getCoordinatesCSV()[1]);
                            PointMap p = new PointMap(tempWRFCity, wrfSource.getCoordinatesCSV());
                            pointsWRF.add(p);
                        } catch (Exception e) {
                            System.out.println("Problem with 1st map(WRF)");
                        }
                    }
                    String filePathWRF = "Excel/TestArchiveData/WRF/" + date.substring(0, 13) + ".csv";
                    new File(pathSources + "/Excel/TestArchiveData/WRF/").mkdirs();
                    mapCreator.createCSVWithInterpolation(filePathWRF, pointsWRF);
                    mapCreator.createMapImage(filePathWRF.substring(25, filePathWRF.length() - 4) + "/", date.substring(0, 13), 'c');

                    //Map 2 - IDW
                    List<PointMap> pointsIDW = new ArrayList<>();
                    for (String cityIDW : mapCreator.getStationFromXML()) {
                        Station wrfSource = wrfComponent.getTemperature(cityIDW, date);
                        try {
                            double tempWRFCity = wrfComponent.readCellFromCSV(wrfSource.getSourceFile() + "/" +
                                            Integer.parseInt(date.substring(5, 7)) + "/" + Integer.parseInt(date.substring(8, 10)) +
                                            "/" + start + "\\SHELTER_TEMPERATURE.CSV", wrfSource.getCoordinatesCSV()[0],
                                    wrfSource.getCoordinatesCSV()[1]);

                            double tempSYNOP = synopComponent.getTemperature(cityIDW, date.substring(0, 10) + "_" +
                                    start).getTemperature();
                            PointMap p = new PointMap(Math.abs(tempWRFCity - tempSYNOP), wrfSource.getCoordinatesCSV());
                            pointsIDW.add(p);
                        } catch (Exception e) {
                            System.out.println("Problem with 2st map(IDW)");
                        }
                    }
                    String filePathIDW = "Excel/TestArchiveData/IDW/" + date.substring(0, 13) + ".csv";
                    new File(pathSources + "/Excel/TestArchiveData/IDW/").mkdirs();
                    mapCreator.createCSVWithInterpolation(filePathIDW, pointsIDW);
                    mapCreator.createMapImage(filePathIDW.substring(25, filePathWRF.length() - 4) + "/", date.substring(0, 13), 'd');
                }
                //Write iteration to xml file
                Element iteration = doc.createElement("Iteration" + i);
                rootElement.appendChild(iteration);

                double rmsePrediction = 0.0;
                double rmseWRF = 0.0;
                //Get temperature for each station from Testing Set
                for (Station station : stationsWithUnknownTemperature) {
                    double tempFromInterpolation = WRFComponent.readCellFromCSV(pathSources + filePath, station.getCoordinatesCSV()[0],
                            station.getCoordinatesCSV()[1]);
                    double tempValidation = station.getTemperature();

                    //This station is created for only get a source file WRF
                    Station wrfSource = wrfComponent.getTemperature(station.getNameStation(), date);
                    double tempWRFPresent = wrfComponent.readCellFromCSV(wrfSource.getSourceFile() + "/" +
                            Integer.parseInt(date.substring(5, 7)) + "/" + Integer.parseInt(date.substring(8, 10)) + "/" +
                            start + "\\SHELTER_TEMPERATURE.CSV", wrfSource.getCoordinatesCSV()[0], wrfSource.getCoordinatesCSV()[1]);

                    System.out.println(station.getNameStation() + " - Interpolation: " + Precision.round(tempFromInterpolation, 2)
                            + ", Validation: " + tempValidation + ", WRF: " + Precision.round(tempWRFPresent, 2));
                    rmsePrediction += Math.pow(Precision.round(tempFromInterpolation, 2) - Precision.round(tempValidation, 2), 2);

                    rmseWRF += Math.pow(Precision.round(tempWRFPresent, 2) - Precision.round(tempValidation, 2), 2);

                    Element city = doc.createElement("Station");
                    iteration.appendChild(city);
                    Attr attr = doc.createAttribute("data");
                    attr.setValue("Station: " + station.getNameStation() + " - Interpolation: " +
                            Precision.round(tempFromInterpolation, 2) +
                            ", Validation: " + tempValidation + ", WRF: " + Precision.round(tempWRFPresent, 2));
                    city.setAttributeNode(attr);
                }
                rmsePrediction /= amountTestingSet;
                rmseWRF /= amountTestingSet;

                System.out.println("RMSE: " + Math.sqrt(rmsePrediction));
                System.out.println("RMSE_WRF: " + Math.sqrt(rmseWRF));

                Element rmsePred = doc.createElement("RMSE");
                rmsePred.appendChild(doc.createTextNode(String.valueOf(Math.sqrt(rmsePrediction))));
                iteration.appendChild(rmsePred);

                Element rmseWrf = doc.createElement("RMSE_WRF");
                rmseWrf.appendChild(doc.createTextNode(String.valueOf(Math.sqrt(rmseWRF))));
                iteration.appendChild(rmseWrf);

                if(rmsePrediction < rmseWRF)
                    correct++;
            }

            if(flagNoFileSYNOP) {
                flagNoFileSYNOP = false;
                continue;
            }

            Element levelOfCorrection = doc.createElement("Correct");
            levelOfCorrection.appendChild(doc.createTextNode(String.valueOf((((double) correct) / maxIteration) * 100)));
            rootElement.appendChild(levelOfCorrection);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            File pathFolder = new File(pathCrossValidation);

            if (!pathFolder.exists())
                pathFolder.mkdir();

            StreamResult result = new StreamResult(new File(pathFolder + "\\" + date + ".xml"));
            transformer.transform(source, result);

            globalPercentageCorrection += ((((double) correct) / maxIteration) * 100);
            amountCheckedFile++;
            System.out.println("Correct of file: " + (((double) correct) / maxIteration) * 100);
            System.out.println("Correction: " + globalPercentageCorrection / amountCheckedFile);

            correct = 0;
        }
        System.out.println(globalPercentageCorrection / amountCheckedFile);
    }
}
