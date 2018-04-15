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

        MapCreator map = new MapCreator();
        WRFComponent wrfComponent = new WRFComponent();
        SynopComponent synopComponent = new SynopComponent();

        String[] cities = map.getStationFromXML();
        List<PointMap> points = new ArrayList<>();

        String table[] = {"2018-03-09_01", "2018-03-09_01", "2018-03-09_02", "2018-03-09_03",
                "2018-03-09_04", "2018-03-09_05", "2018-03-09_06", "2018-03-09_07",
                "2018-03-09_08", "2018-03-09_09", "2018-03-09_10", "2018-03-09_11",
                "2018-03-09_12", "2018-03-09_13", "2018-03-09_14", "2018-03-09_15",
                "2018-03-09_16", "2018-03-09_17", "2018-03-09_18", "2018-03-09_19", "2018-03-09_20",
                "2018-03-09_21", "2018-03-09_22", "2018-03-10_00", "2018-03-10_01", "2018-03-10_02",
                "2018-03-10_03", "2018-03-10_04", "2018-03-10_05", "2018-03-10_06", "2018-03-10_07",
                "2018-03-10_08", "2018-03-10_09", "2018-03-10_10", "2018-03-10_11", "2018-03-10_12",
                "2018-03-10_13", "2018-03-10_14", "2018-03-10_15", "2018-03-10_16", "2018-03-10_17",
                "2018-03-10_18", "2018-03-10_19", "2018-03-10_20", "2018-03-10_21", "2018-03-10_22"};

        for(int i = 0; i < 1; i++) {
            String dateTemporary = table[i];
            List<Station> stations = new ArrayList<>();

            //1 measure
            for(String nameStation : cities) {
                Station wrf = wrfComponent.getTemperature(nameStation, dateTemporary);
                Station synop = synopComponent.getTemperature(nameStation, dateTemporary);
                //System.out.println("City: " + nameStation + " - WRF: " + wrf.getTemperature() + ", SYNOP: " + synop.getTemperature());
                PointMap p = new PointMap(wrf.getTemperature() - synop.getTemperature(), wrf.getCoordinatesCSV());
                points.add(p);
                stations.add(wrf);
            }
            map.createCSVWithInterpolation("Excel/" + dateTemporary + ".csv", points);
            map.createMapImage(dateTemporary);
            map.predict(wrfComponent.getSourceFileWRF());
            //map.comparePredictedTemperatures(stations, dateTemporary, wrfComponent, synopComponent);
        }
    }
}
