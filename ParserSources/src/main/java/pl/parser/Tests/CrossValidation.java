package pl.parser.Tests;

import org.xml.sax.SAXException;
import pl.parser.Domain.Station;
import pl.parser.Implementation.MapCreator;
import pl.parser.Implementation.SynopComponent;
import pl.parser.Implementation.WRFComponent;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public class CrossValidation {

    private static int maxIteration = 10;    //max iteration
    private static int amountRandomlyCities = 20;
    private static int restCitiesFromSYNOP = 40;
    private static String date = "2018-04-16_10";

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
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

        for(int i = 0; i < maxIteration; i++) {
            System.out.println("Iteration: " + i);
            List<String> knownCitiesFromSYNOP = new ArrayList<>(Arrays.asList(allCities));
            Map<Station, double[]> citiesWithPredictedTemperatures = new HashMap<>();

            //Randomly choosen cities which we must compute
            IntStream stream = random.ints(0, 60).distinct().limit(20).sorted();
            List<String> randomlyCities = new ArrayList<>(amountRandomlyCities);
            int[] randomlyChoosenSetOfCities = stream.toArray();
            double msePrediction = 0.0;
            double mseWRF = 0.0;
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
                msePrediction += Math.pow(predicted[3] - synopTable[3], 2);
                mseWRF += Math.pow(wrfTable[3] - synopTable[3], 2);
            }
            msePrediction /= amountRandomlyCities;
            mseWRF /= amountRandomlyCities;
            System.out.println("MSE_prediction: " + msePrediction);
            System.out.println("MSE_WRF: " + mseWRF);


            //Cities which are known
//            knownCitiesFromSYNOP.removeAll(randomlyCities);
//            for(int c = 0; c < restCitiesFromSYNOP; c++) {
//                Station station = synopComponent.getTemperature(knownCitiesFromSYNOP.get(c), date);
//                station.initialiazeCoordinates(wrfComponent.getCoordinates(station.getNameStation()));
//                stationsWithKnowTemperature.add(station);
//            }
        }
    }
}
