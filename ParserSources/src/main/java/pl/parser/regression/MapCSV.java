package pl.parser.regression;

import com.opencsv.CSVWriter;
import org.apache.commons.math3.util.Precision;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapCSV {

    public String[] getAllAvailableStation(String path) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(path);
        doc.getDocumentElement().normalize();
        NodeList nodeList = doc.getElementsByTagName("item");

        String[] cities = new String[nodeList.getLength()];

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node nNode = nodeList.item(i);
            Element eElement = (Element) nNode;
            Node cElement = eElement.getChildNodes().item(0);
            String s = cElement.getTextContent();
            cities[i] = s;
        }
        return cities;
    }

    public void createCSV(String filePath, List<PointMap> points) throws IOException {

        CSVWriter writer = new CSVWriter(new FileWriter("C:/KSG/file.csv"));
        List<String[]> entries = new ArrayList<>();
        double[][] array = new double[170][];
        for(int i = 0; i < 170; i++) {
            array[i] = new double[325];
            Arrays.fill(array[i], 0.0);
        }
        //We have build array double about dimensions 170x325

        //Points contain error between wrf and synop, and keep coordinations of cities which we save in csv
        //Points are base to calcuate interpolation in points which we don't have information about measures
        for(PointMap p: points) {
            array[p.getCoordinates(0)][p.getCoordinates(1)] = Precision.round(p.getError(), 2);
            System.out.println(p.getError());
            System.out.println("Y: " + p.getCoordinates(0) + "X: " + p.getCoordinates(1));
        }

        int radius = 100;
        boolean basePoint;

        for(int i = 0; i < 170; i++) {
            for(int j = 0; j < 325; j++) {

                //if we will find a base point, we shouldn't calculate new error for it, but ommit it
                basePoint = false;
                for(PointMap p: points) {
                    if(i == p.getCoordinates(0) && j == p.getCoordinates(1)) {
                        basePoint = true;
                        break;
                    }
                }
                if(basePoint)
                    continue;

                //we must find some base points and store it
                List<PointMap> pointsNeighbor = new ArrayList<>();
                for(PointMap p: points) {
                    if( (p.getCoordinates(0) >= (i - radius/2)) && (p.getCoordinates(0) <= (i + radius/2)) &&
                            (p.getCoordinates(1) >= (j - radius/2)) && (p.getCoordinates(1) <= (j + radius/2)) ) {
                        pointsNeighbor.add(p);
                    }
                }

                if(pointsNeighbor.isEmpty())
                    continue;

                //Calculate Inverse distance weighting
                double IDWL = 0.0;
                double IDWM = 0.0;

                for(PointMap p: pointsNeighbor) {
                    double distance = Math.sqrt(Math.pow((p.getCoordinates(1) - j), 2) +
                            Math.pow((p.getCoordinates(0) - i), 2));

                    IDWL += (p.getError() / Math.pow(distance, 2));
                    IDWM += (1 / Math.pow(distance, 2));
                }

                double calculateError = IDWL / IDWM;
                array[i][j] = Precision.round(calculateError, 2);
            }
        }

        //Parse big double array to String
        for(int i = 0; i < 170; i++) {
            String s = Arrays.toString(array[i]);
            s = s.substring(1, s.length() - 1);
            String[] s_array = s.split(", ");
            entries.add(s_array);
        }

        writer.writeAll(entries);
        writer.close();
    }
}
