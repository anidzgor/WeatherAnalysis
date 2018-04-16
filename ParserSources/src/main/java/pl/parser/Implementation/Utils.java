package pl.parser.Implementation;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import pl.parser.Domain.PointMap;
import pl.parser.Domain.Station;
import pl.parser.Visualizer.GenerateChart;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class Utils {

    public static String pathSources = "C:/KSG/SYNOP/";

    public static List<String> convertFilesToList() {

        File directory = new File(pathSources);
        File[] files = directory.listFiles();

        List<String> listFiles = new ArrayList<String>();
        for(File file: files) {
            listFiles.add(file.toString().substring(13, 26));
        }

        return listFiles;
    }

    public static void launch(String date) throws IOException, SAXException, ParserConfigurationException {

        MapCreator map = new MapCreator();
        WRFComponent wrfComponent = new WRFComponent();
        SynopComponent synopComponent = new SynopComponent();

        String[] cities = map.getStationFromXML();
        List<PointMap> points = new ArrayList<>();

            //1 measure
            for(String nameStation : cities) {
                Station wrf = wrfComponent.getTemperature(nameStation, date);
                Station synop = synopComponent.getTemperature(nameStation, date);
                PointMap p = new PointMap(Math.abs(wrf.getTemperature() - synop.getTemperature()), wrf.getCoordinatesCSV());
                points.add(p);
            }
            map.createCSVWithInterpolation("Excel/" + date + ".csv", points);
            //map.createMapImage(date);
    }

    public static JsonObject generateChartJSON(String station, int hours) {

        WRFComponent wrfComponent = new WRFComponent();
        SynopComponent synopComponent = new SynopComponent();

        double[] wrfTemp = new double[hours];
        double[] synopTemp = new double[hours];

        String[] datas = {"2018-03-09_05", "2018-03-09_06", "2018-03-09_07", "2018-03-09_08", "2018-03-09_09" };
        int counter = 0;

        for(String date: datas) {
            Station wrf = wrfComponent.getTemperature(station, date);
            Station synop = synopComponent.getTemperature(station, date);

            wrfTemp[counter] = wrf.getTemperature();
            synopTemp[counter++] = synop.getTemperature();
        }
        JsonObject json = GenerateChart.GenerateJSON(wrfTemp, synopTemp);
        return json;
    }


}
