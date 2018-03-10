package pl.parser.Application;

import org.xml.sax.SAXException;
import pl.parser.Domain.Station;
import pl.parser.Implementation.SynopComponent;
//import pl.parser.Implementation.WRFComponent;
import pl.parser.Implementation.MapCreator;
import pl.parser.Domain.PointMap;
import pl.parser.Implementation.WRFComponent;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

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
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH");
        Date date = new Date();
        //System.out.println(dateFormat.format(date)); //2018-02_25-14-49
        //String dateTemporary = "2018-03-09_09";  //For tests

        MapCreator map = new MapCreator();
        WRFComponent wrfComponent = new WRFComponent();
        SynopComponent synopComponent = new SynopComponent();

        String[] cities = map.getStationFromXML("src/main/resources/places.xml");
        List<PointMap> points = new ArrayList<>();

        String table[] = {"2018-03-09_05", "2018-03-09_06", "2018-03-09_07", "2018-03-09_08", "2018-03-09_09" };

        for(int i = 0; i < 5; i++) {
            String dateTemporary = table[i];

            //1 measure
            for(String nameStation : cities) {
                Station wrf = wrfComponent.getTemperatures(nameStation, dateTemporary);
                Station synop = synopComponent.getTemperatures(nameStation, dateTemporary);
                PointMap p = new PointMap(Math.abs(wrf.getTemperature() - synop.getTemperature()), wrf.getCoordinatesCSV());
                points.add(p);
            }
            map.createCSV("Excel/" + dateTemporary + ".csv", points);
            map.createMapImage(dateTemporary);
        }
    }
}
