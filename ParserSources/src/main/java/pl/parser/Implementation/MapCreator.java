package pl.parser.Implementation;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.commons.math3.util.Precision;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pl.parser.Api.IMapCreator;
import pl.parser.Domain.PointMap;
import pl.parser.Domain.Station;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapCreator implements IMapCreator {
    public static double[][] array;
    public static String pathSynop = "C:/KSG/SYNOP/";
    public static String pathResources = "C:/KSG/Resources/";
    public static String pathVisualization = "Visualization/";

    public String[] getStationFromXML(String path) throws ParserConfigurationException, IOException, SAXException {
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

    public void predict(String sourceFileWRF) throws IOException {

        //Handle file WRF for prediction - do correct
        String fileWRF = "";
        int hour = 0;

        if(sourceFileWRF.charAt(sourceFileWRF.length() - 2) == '\\') {
            hour = Integer.parseInt(sourceFileWRF.substring(sourceFileWRF.length() - 1)) + 2;
            fileWRF = sourceFileWRF.substring(0, sourceFileWRF.length() - 1) + hour;
        } else if(sourceFileWRF.charAt(sourceFileWRF.length() - 3) == '\\') {
            hour = Integer.parseInt(sourceFileWRF.substring(sourceFileWRF.length() - 2)) + 2;
            fileWRF = sourceFileWRF.substring(0, sourceFileWRF.length() - 2) + hour;
        }

        //Add error correct(table array) to new file WRF
        File file = new File(fileWRF + "\\SHELTER_TEMPERATURE.csv");
        CSVReader reader = new CSVReader(new FileReader(file));
        List<String[]> csvBody = reader.readAll();

        for(int i = 0; i < 170; i++) {
            for(int j = 0; j < 325; j++) {
                String result = csvBody.get(i)[j];

                if(Float.parseFloat(result) == 0.0 || Float.parseFloat(result) == 0) {
                    array[i][j] = Float.parseFloat(result);
                    continue;
                }

                if(array[i][j] > 0.0)
                    array[i][j] = Float.parseFloat(result) - array[i][j];
                else if(array[i][j] < 0.0) {
                    array[i][j] = Float.parseFloat(result) + array[i][j] * (-1);
                } else
                    array[i][j] = Float.parseFloat(result);

                array[i][j] = Precision.round(array[i][j], 2);
            }
        }

        reader.close();

        CSVWriter writer = new CSVWriter(new FileWriter(pathResources + "\\Excel\\predict.csv"));
        List<String[]> entries = new ArrayList<>();
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

    public void comparePredictedTemperatures(List<Station> stations,
                                             String date,
                                             WRFComponent wrf,
                                             SynopComponent synop) throws IOException {

        CSVReader reader = new CSVReader(new FileReader(pathResources + "\\Excel\\predict.csv"));
        List<String[]> csvBody = reader.readAll();

        for(Station station: stations) {

            //WRF prediction temperature
            double celsiusPredicted = wrf.readCellFromCSV(pathResources + "\\Excel\\predict.csv", station.getCoordinatesCSV()[0],
                    station.getCoordinatesCSV()[1]);

            double celsiusSYNOP = (synop.getTemperatures(station.getNameStation(), date)).getTemperature();

            System.out.println("Station: " + station.getNameStation() + " WRF[" + station.getCoordinatesCSV()[0]
                    + ", " + station.getCoordinatesCSV()[1] + "]: " + celsiusPredicted + " - SYNOP: " + celsiusSYNOP);
        }
        reader.close();
    }

    public void createCSV(String filePath, List<PointMap> points) throws IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(pathResources + filePath));
        List<String[]> entries = new ArrayList<>();
        array = new double[170][];
        for(int i = 0; i < 170; i++) {
            array[i] = new double[325];
            Arrays.fill(array[i], 0.0);
        }
        //We have build array double about dimensions 170x325

        //Points contain value between wrf and synop, and keep coordinations of cities which we save in csv
        //Points are base to calcuate interpolation in points which we don't have information about measures
        for(PointMap p: points) {
            array[p.getCoordinates(0)][p.getCoordinates(1)] = Precision.round(p.getValuePoint(), 2);
            //System.out.println(p.getValuePoint());
            //System.out.println("Y: " + p.getCoordinates(0) + "X: " + p.getCoordinates(1));
        }

        int radius = 100;
        boolean basePoint;

        //Inverse distance weighting
        for(int i = 0; i < 170; i++) {
            for(int j = 0; j < 325; j++) {

                //if we will find a base point, we shouldn't calculate new value for it, but ommit it
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

                    IDWL += (p.getValuePoint() / Math.pow(distance, 2));
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

    public void createMapImage(String date) throws IOException {
        BufferedImage image = new BufferedImage(650, 510, BufferedImage.TYPE_INT_RGB);

        Color[][]colors = new Color[510][650];

        int width = 325;
        int height = 170;

        int widthImage = 650;
        int heightImage = 510;

        int r,g,b;

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                if(array[y][x] == 0.0) {    //white
                    r = g = b = 255;
                } else if(array[y][x] > -3.0 && array[y][x] <= -2.0) { //yellow light
                    r = 249;
                    g = 248;
                    b = 207;
                }
                else if(array[y][x] > -2.0 && array[y][x] <= -1.0) { //yellow light
                    r = 241;
                    g = 244;
                    b = 183;
                } else if(array[y][x] > -1.0 && array[y][x] <= 0.0) { //yellow
                    r = 235;
                    g = 242;
                    b = 104;
                } else if(array[y][x] > 0.0 && array[y][x] <= 1.0) { //orange light
                    r = 239;
                    g = 174;
                    b = 83;
                } else if(array[y][x] > 1.0 && array[y][x] <= 2.0) {  //orange
                    r = 242;
                    g = 152;
                    b = 26;
                } else if(array[y][x] > 2.0 && array[y][x] <= 3.0) {    //red light
                    r = 198;
                    g = 61;
                    b = 1;
                } else if(array[y][x] > 3.0 ) {
                    r = 66;
                    g = 22;
                    b = 2;
                } else
                    r = g = b = 0;

                Color newColor = new Color(r, g, b);

                colors[heightImage - 1 - 3 * y][2 * x] = newColor;
                colors[heightImage - 1 - 3 * y][2 * x + 1] = newColor;
                colors[heightImage - 2 - 3 * y][2 * x] = newColor;
                colors[heightImage - 2 - 3 * y][2 * x + 1] = newColor;
                colors[heightImage - 3 - 3 * y][2 * x] = newColor;
                colors[heightImage - 3 - 3 * y][2 * x + 1] = newColor;

                image.setRGB(2 * x, heightImage - 1 - 3 * y, newColor.getRGB());
                image.setRGB(2* x + 1, heightImage - 1 - 3 * y, newColor.getRGB());
                image.setRGB(2 * x, heightImage - 2 - 3 * y, newColor.getRGB());
                image.setRGB(2 * x + 1, heightImage - 2 - 3 * y, newColor.getRGB());
                image.setRGB(2 * x, heightImage - 3 - 3 * y, newColor.getRGB());
                image.setRGB(2 * x + 1, heightImage - 3 - 3 * y, newColor.getRGB());
            }

        String folderForLayers = pathResources + pathVisualization + "Layers/";
        new File(folderForLayers).mkdirs();
        ImageIO.write(image, "JPG", new File(folderForLayers + date + ".jpg"));

        overlay(date, folderForLayers);
    }

    public void overlay(String date, String folder) throws IOException {
        // load source images
        BufferedImage image = ImageIO.read(new File(folder + date + ".jpg"));
        BufferedImage overlay = ImageIO.read(new File(pathResources + pathVisualization + "layer.png"));

        // create the new image, canvas size is the max. of both image sizes
        int w = Math.max(image.getWidth(), overlay.getWidth());
        int h = Math.max(image.getHeight(), overlay.getHeight());
        BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        // paint both images, preserving the alpha channels
        Graphics g = combined.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.drawImage(overlay, 0, 0, null);

        //Save as new image
        String folderForMaps = pathResources + pathVisualization + "Maps/";
        new File(folderForMaps).mkdirs();

        ImageIO.write(combined, "PNG", new File(folderForMaps + date + ".png"));
        //ImageIO.write(combined, "PNG", new File("C:\\Users\\ASUS\\Desktop\\WeatherPredictionProject\\webvisualizer\\src\\main\\resources\\static\\images\\picture.png"));
    }
}
