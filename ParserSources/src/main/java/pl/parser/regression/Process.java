package pl.parser.regression;

import org.apache.commons.math3.util.Precision;
import org.xml.sax.SAXException;
import pl.parser.Station;
import pl.parser.Synop.Synop_Processing;
import pl.parser.WRF.WRF_Processing;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static javafx.scene.input.KeyCode.L;

public class Process {

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

//        Station synop = Synop_Processing.getTemperatures("Gdańsk", "2018-02-25_20-30-00", 1);
//        System.out.println(synop.toString());
//        Station wrf = WRF_Processing.getTemperatures("Gdańsk", "2018-02-25_20-30-00", 1);
//        System.out.println(wrf.toString());
//
//        double []diff = new double[24];
//        for(int i = 0; i < 24; i++) {
//            diff[i] = Math.abs(wrf.getTemperatures(i) - synop.getTemperatures(i));
//        }
//        System.out.println(Arrays.toString(diff));
//        GenerateJSON.generateJSON(wrf.getTemp(), synop.getTemp());

        //For case when we analyze 1 hour back so we got 1 temperature

        MapCSV map = new MapCSV();
        String[] cities = map.getAllAvailableStation("src/main/resources/places.xml");
        List<PointMap> points = new ArrayList<>();

        for(String nameStation : cities) {
            Station wrf = WRF_Processing.getTemperatures(nameStation, "2018-02-25_15-30-00", 1);
            Station synop = Synop_Processing.getTemperatures(nameStation, "2018-02-25_15-30-00", 1);

            PointMap p = new PointMap(Math.abs(wrf.getTemperatures(wrf.getHoursMeasures()) -
                    synop.getTemperatures(synop.getHoursMeasures())), wrf.getCoordinatesCSV());
            points.add(p);
        }

       map.createCSV("C:/KSG/file.csv", points);
    }
}
