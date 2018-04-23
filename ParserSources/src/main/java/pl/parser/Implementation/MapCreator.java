package pl.parser.Implementation;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.commons.math3.util.Precision;
import org.springframework.stereotype.Component;
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
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class MapCreator implements IMapCreator {
    public static double[][] array;
    public static String pathSynop = "C:/KSG/SYNOP/";
    public static String pathResources = "C:/KSG/Resources/";
    public static String pathVisualization = "Visualization/";

    public String[] getStationFromXML() throws ParserConfigurationException, IOException, SAXException {
        String path = "C:/KSG/Resources/places.xml";

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

    public void createCSVWithInterpolation(String filePath, List<PointMap> points) throws IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(pathResources + filePath));
        List<String[]> entries = new ArrayList<>();
        array = new double[170][];
        for(int i = 0; i < 170; i++) {
            array[i] = new double[325];
            Arrays.fill(array[i], 0.0);
        }

        //Points contain value between wrf and synop, and keep coordinations of cities which we save in csv
        //Points are base to calcuate interpolation in points which we don't have information about measures
        for(PointMap p: points) {
            array[p.getCoordinates(0)][p.getCoordinates(1)] = Precision.round(p.getValuePoint(), 2);
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

        double[][] arrayForFile = new double[170][];
        for(int i = 0; i < 170; i++) {
            arrayForFile[i] = new double[325];
            Arrays.fill(arrayForFile[i], 0.0);
        }

        for(int i = 0; i < 170; i++)
            for(int j = 0; j < 325; j++)
                if(array[i][j] != 0.0)
                    arrayForFile[i][j] = Precision.round(array[i][j] + 273.15, 2);

        //For fill cell where is 0.0 in some cell between points
        for(int i = 0; i < 170; i++)
            for(int j = 0; j < 325; j++)
                if(     (i > 1 && j > 1 && i < 169 & j < 323 && array[i][j] == 0.0 && array[i-1][j] != 0.0 && array[i+1][j] != 0.0) ||
                        (i > 1 && j > 1 && i < 169 & j < 323 && array[i][j] == 0.0 && array[i][j-1] != 0.0 && array[i][j+1] != 0.0) ||
                        (array[i][j] == 0.0 && i > 31 && j > 16 && i < 160 && j < 270))
                    arrayForFile[i][j] = Precision.round(array[i][j] + 273.15, 2);

        //Parse big double array to String
        for(int i = 0; i < 170; i++) {
            String s = Arrays.toString(arrayForFile[i]);
            s = s.substring(1, s.length() - 1);
            String[] s_array = s.split(", ");
            entries.add(s_array);
        }

        writer.writeAll(entries);
        writer.close();
    }

    public void createMapImage(String date, char typeOfImage) throws IOException {
        BufferedImage image = new BufferedImage(650, 580, BufferedImage.TYPE_INT_RGB);

        //Fill background white color
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint ( new Color (255,255,255) );
        graphics.fillRect ( 0, 0, image.getWidth(), image.getHeight() );

        Color[][]colors = new Color[510][650];

        int width = 325;
        int height = 170;

        int widthImage = 650;
        int heightImage = 510;

        int r = 0, g = 0, b = 0;

        DoubleStream streamMax = Arrays.stream(array).flatMapToDouble(x -> Arrays.stream(x));
        double max = streamMax.max().getAsDouble();
        DoubleStream streamMin = Arrays.stream(array).flatMapToDouble(x -> Arrays.stream(x));
        double min = streamMin.min().getAsDouble();

        //12 types of color
        double amountTypes = (max - min) / 12.0;

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                if(array[y][x] >= min && array[y][x] < (min + amountTypes)) {
                    r = 160;
                    g = 0;
                    b = 200;
                }
                else if(array[y][x] >= (min + amountTypes) && array[y][x] < (min + amountTypes * 2)) {
                    r = 130;
                    g = 0;
                    b = 220;
                } else if(array[y][x] >= (min + amountTypes * 2) && array[y][x] < (min + amountTypes * 3)) {
                    r = 30;
                    g = 60;
                    b = 255;
                } else if(array[y][x] >= (min + amountTypes * 3) && array[y][x] < (min + amountTypes * 4)) {
                    r = 0;
                    g = 160;
                    b = 255;
                } else if(array[y][x] >= (min + amountTypes * 4) && array[y][x] < (min + amountTypes * 5)) {
                    r = 0;
                    g = 200;
                    b = 200;
                } else if(array[y][x] >= (min + amountTypes * 5) && array[y][x] < (min + amountTypes * 6)) {
                    r = 0;
                    g = 210;
                    b = 140;
                } else if(array[y][x] >= (min + amountTypes * 6) && array[y][x] < (min + amountTypes * 7)) {
                    r = 160;
                    g = 230;
                    b = 50;
                } else if(array[y][x] >= (min + amountTypes * 7) && array[y][x] < (min + amountTypes * 8)) {
                    r = 230;
                    g = 220;
                    b = 50;
                } else if(array[y][x] >= (min + amountTypes * 8) && array[y][x] < (min + amountTypes * 9)) {
                    r = 230;
                    g = 175;
                    b = 45;
                } else if(array[y][x] >= (min + amountTypes * 9) && array[y][x] < (min + amountTypes * 10)) {
                    r = 240;
                    g = 130;
                    b = 40;
                } else if(array[y][x] >= (min + amountTypes * 10) && array[y][x] < (min + amountTypes * 11)) {
                    r = 250;
                    g = 60;
                    b = 60;
                } else if(array[y][x] >= (min + amountTypes * 11) && array[y][x] < (min + amountTypes * 12)) {
                    r = 240;
                    g = 0;
                    b = 130;
                }

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

        overlay(date, folderForLayers, max, min, typeOfImage);
    }

    public void overlay(String date, String folder, double max, double min, char typeOfImage) throws IOException {
        // load source images
        BufferedImage image = ImageIO.read(new File(folder + date + ".jpg"));
        BufferedImage overlay = ImageIO.read(new File(pathResources + pathVisualization + "layer.png"));
        BufferedImage gauge = ImageIO.read(new File(pathResources + pathVisualization + "gauge.png"));

        // create the new image, canvas size is the max of both image sizes
        int w = Math.max(image.getWidth(), overlay.getWidth());
        int h = Math.max(image.getHeight(), overlay.getHeight());
        BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        // paint both images, preserving the alpha channels
        Graphics g = combined.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.drawImage(overlay, 0, 0, null);
        g.drawImage(gauge, 50, h - 60, null);

        g.setColor(Color.BLACK);
        double counter = (max - min) / 12.0;
        for(int i = 1; i <= 12; i++) {
            double value = Precision.round(min + counter * (i - 1), 2);
            g.drawString(String.valueOf(value), i * 45, h - 20);
        }

        //Save as new image
        String folderForMaps = pathResources + pathVisualization + "Maps/";
        new File(folderForMaps).mkdirs();
        new File(folderForMaps + "/Observational/").mkdirs();
        new File(folderForMaps + "/Archive/").mkdirs();
        new File(folderForMaps + "/LeavePOutCrossValidation/").mkdirs();

        if(typeOfImage == 'o')
            ImageIO.write(combined, "PNG", new File(folderForMaps + "/Observational/" + date + ".png"));
        else if(typeOfImage == 'a')
            ImageIO.write(combined, "PNG", new File(folderForMaps + "/Archive/" + date + ".png"));
        else if(typeOfImage == 'p')
            ImageIO.write(combined, "PNG", new File(folderForMaps + "/LeavePOutCrossValidation/" + date + ".png"));
    }
}
