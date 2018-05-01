package pl.parser.Implementation;

import com.opencsv.CSVReader;
import org.apache.commons.math3.util.Precision;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pl.parser.Api.IComponent;
import pl.parser.Domain.Station;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.DecimalFormat;
import java.util.List;

public class WRFComponent implements IComponent {
    public static String pathSources = "C:/KSG/WRF/";
    private String sourceFileWRF;

    public String getSourceFileWRF() {
        return sourceFileWRF;
    }

    public int[] getCoordinates(String city) throws ParserConfigurationException, IOException, SAXException {

        File file = new File("C:/KSG/Resources/places.xml");

        double startCoordLatitude = 48.8;
        double startCoordLongiture = 13.24;

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();
        NodeList nodeList = doc.getElementsByTagName("item");

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node nNode = nodeList.item(i);
            Element eElement = (Element) nNode;
            //Index 1 - Station
            Node cElement = eElement.getChildNodes().item(0);
            if (city.equals(cElement.getTextContent())) {
                double lat = Double.parseDouble(eElement.getChildNodes().item(1).getTextContent());//szer
                double lon = Double.parseDouble(eElement.getChildNodes().item(2).getTextContent());//dl

                //Formula for correct calculate row and column for csv file
                int col =  (int)(((lon - startCoordLongiture) * 325) / 12.3);
                int row = (int)(((lat - startCoordLatitude) * 170) / 6.6);

                return new int[]{row, col};
            }
        }
        return new int[]{0,0};
    }

    public static Float readCellFromCSV(String filePath, int row, int col) throws IOException {
        File file = new File(filePath);
        CSVReader reader = new CSVReader(new FileReader(file));
        List<String[]> csvBody = reader.readAll();
        String result = csvBody.get(row-1)[col-1];

        double celsius = Float.parseFloat(result) - 273.15;
        celsius = Precision.round(celsius, 2);
        reader.close();

        return new Float(celsius);
    }

    public Station getTemperature(String nameStation, String dateMeasure) {
        Station station = new Station(nameStation, dateMeasure);
        int[] coordinates = new int[0];
        try {
            coordinates = getCoordinates(nameStation);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        station.initialiazeCoordinates(coordinates);

        String parseDate = dateMeasure.replace("-", "");
        parseDate = parseDate.substring(2, 8);
        final String newParsedata = parseDate;
        //Check for all existing folders for one day and choose newest
        File directory = new File(pathSources);
        File [] files = directory.listFiles((d, name) -> name.contains(newParsedata));

        String newestFolder = "";
        for(File file: files) {
            newestFolder = file.getName();
        }

        station.setSourceFile(pathSources + newestFolder);

        //Set month
        if(parseDate.substring(2, 4).startsWith("0"))
            newestFolder += "/" + parseDate.substring(3, 4);
        else
            newestFolder += "/" + parseDate.substring(2, 4);

        //Set day
        if(parseDate.charAt(4) == '0')
            newestFolder += "/" + parseDate.substring(5, 6);
        else
            newestFolder += "/" + parseDate.substring(4, 6);

        //Set hour and choose properly file
        File file = new File(pathSources + newestFolder + "/" + Integer.parseInt(dateMeasure.substring(11, 13)));
        sourceFileWRF = file.getAbsolutePath();

        try {
            station.setTemperature(readCellFromCSV(pathSources + newestFolder + "/" + file.getName() + "/SHELTER_TEMPERATURE.csv", coordinates[0], coordinates[1]));
        } catch (IOException e) {
            System.out.println("No neccessary files WRF");
        }
        return station;
    }
}