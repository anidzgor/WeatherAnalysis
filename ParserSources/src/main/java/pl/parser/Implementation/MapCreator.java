package pl.parser.Implementation;

import com.opencsv.CSVWriter;
import org.apache.commons.math3.util.Precision;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pl.parser.Api.IMapCreator;
import pl.parser.Domain.PointMap;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapCreator implements IMapCreator {
    public static double[][] array;
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
                } else if(array[y][x] > 0.0 && array[y][x] <= 0.5) { //yellow light
                    r = 252;
                    g = 252;
                    b = 176;
                } else if(array[y][x] > 0.5 && array[y][x] <= 1.0) { //yellow
                    r = 247;
                    g = 247;
                    b = 45;
                } else if(array[y][x] > 1.0 && array[y][x] <= 1.5) { //orange light
                    r = 252;
                    g = 196;
                    b = 84;
                } else if(array[y][x] > 1.5 && array[y][x] <= 2.0) {  //orange
                    r = 239;
                    g = 149;
                    b = 15;
                } else if(array[y][x] > 2.0 && array[y][x] <= 3.0) {    //red light
                    r = 253;
                    g = 72;
                    b = 17;
                } else if(array[y][x] > 3.0) {
                    r = 232;
                    g = 34;
                    b = 12;
                } else
                    r = g = b = 0;

                Color newColor = new Color(r, g, b);

                //System.out.println(y + " " + x);
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
    }
}
