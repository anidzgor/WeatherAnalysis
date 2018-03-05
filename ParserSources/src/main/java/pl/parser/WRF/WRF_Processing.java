package pl.parser.WRF;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pl.parser.Station;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WRF_Processing {
    public static String pathSources = "C:/KSG/WRF/";

    //For one day from hour 0 to current
    public static Station getStation(String nameStation, final String date) throws IOException {
        Station station = new Station(nameStation, date);
        int[] coordinates = new int[0];
        try {
            coordinates = getCoordinates(nameStation);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        String parseDate = date.replace("-", "");
        parseDate = parseDate.substring(2);
        final String newParsedata = parseDate;
        //Check for all existing folders for one day and choose newest
        File directory = new File(pathSources);
        File [] files = directory.listFiles((d, name) -> name.contains(newParsedata));

        String newestFolder = "";
        for(File file: files) {
            newestFolder = file.getName();
        }

        //Set month
        if(parseDate.substring(2, 4).startsWith("0"))
            newestFolder += "/" + parseDate.substring(3, 4);
        else
            newestFolder += "/" + parseDate.substring(2, 4);

        //Set day
        newestFolder += "/" + parseDate.substring(4, 6);

        //Set hours
        directory = new File(pathSources + newestFolder);
        File [] foldersWithHours = directory.listFiles();

        for(File file: foldersWithHours) {
            Float temperature = new Float(0.0);
            try {
                temperature = readCellFromCSV(pathSources + newestFolder + "/" + file.getName() + "/SHELTER_TEMPERATURE.csv", coordinates[0], coordinates[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            station.addMeasureTemperature(Integer.parseInt(file.getName()), temperature);
        }
        return station;
    }

    private static int[] getCoordinates(String city) throws ParserConfigurationException, IOException, SAXException {

        File file = new File("src/main/resources/places.xml");

        double startCoordLatitude = 48.8;
        double startCoordLongiture = 13.2;

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
        String result = csvBody.get(row+1)[col+1];

        double celsius = Float.parseFloat(result) - 273.15;
        DecimalFormat f = new DecimalFormat("#.00");
        //System.out.println("Celsjusz: " + f.format(celsius));
        reader.close();

        return new Float(celsius);
    }

    //Get temperatures from specific hours back
    public static Station getTemperatures(String nameStation, String currentTime, int backHours) {
        Station station = new Station(nameStation, currentTime);
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

        String parseDate = currentTime.replace("-", "");
        parseDate = parseDate.substring(2, 8);
        final String newParsedata = parseDate;
        //Check for all existing folders for one day and choose newest
        File directory = new File(pathSources);
        File [] files = directory.listFiles((d, name) -> name.contains(newParsedata));

        String newestFolder = "";
        for(File file: files) {
            newestFolder = file.getName();
        }

        //Set month
        if(parseDate.substring(2, 4).startsWith("0"))
            newestFolder += "/" + parseDate.substring(3, 4);
        else
            newestFolder += "/" + parseDate.substring(2, 4);

        //Set day
        newestFolder += "/" + parseDate.substring(4, 6);

        //Set hours
        directory = new File(pathSources + newestFolder);

        int[] table = new int[backHours];

        String hourFromString = currentTime.substring(11, 13);
        int hour = Integer.parseInt(hourFromString) - 1;
        for(int i = 0; i < backHours; i++) {
            table[i] = hour - i;
        }

        station.initializeAnalyzesHours(table);

        File [] foldersWithHours = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                for(int i = 0; i < backHours; i++) {
                    if(name.startsWith(table[i] + ""))
                        return true;
                }
                return false;
            }
        });

        for(File file: foldersWithHours) {
            Float temperature = new Float(0.0);
            try {
                temperature = readCellFromCSV(pathSources + newestFolder + "/" + file.getName() + "/SHELTER_TEMPERATURE.csv", coordinates[0], coordinates[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            station.addMeasureTemperature(Integer.parseInt(file.getName()), temperature);
        }

        return station;
    }
}
