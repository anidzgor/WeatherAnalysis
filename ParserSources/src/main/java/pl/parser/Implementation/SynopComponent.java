package pl.parser.Implementation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math3.util.Precision;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pl.parser.Api.IComponent;
import pl.parser.Domain.Station;

public class SynopComponent implements IComponent {
    public static String pathSources = "C:/KSG/SYNOP/";

    public Station getTemperature(String nameStation, String dateMeasure) {
        Station station = new Station(nameStation, dateMeasure);

        //we have hour beetween 0 and 9
        if(dateMeasure.length() == 12)
            dateMeasure = dateMeasure.substring(0, 11) + "0" + dateMeasure.substring(11, 12);

        //Get specific file
        File directory = new File(pathSources);

        String finalDateMeasure = dateMeasure;
        File[] fileWithFindingDate = directory.listFiles((d, name) -> name.contains(finalDateMeasure));

        if(fileWithFindingDate.length == 0) {
            //Secure if we don't have current file
            //Example
            //Currently is 19:45, so we have only file for 18 hour
            //Is one more example, we have 19:01, we don't have file for 18 hour but for 17th hour

            int oneHourBefore = Integer.parseInt(dateMeasure.substring(11, 13)) - 1;
            String oneHourBeforeTime = "";
            if(oneHourBefore == -1) {
                oneHourBefore = 23;
                oneHourBeforeTime = dateMeasure.substring(0, 11) + oneHourBefore;
            } else if(oneHourBefore >= 0 && oneHourBefore <= 9) {
                oneHourBeforeTime = dateMeasure.substring(0, 11) + "0" + oneHourBefore;
            } else {
                oneHourBeforeTime = dateMeasure.substring(0, 11) + oneHourBefore;
            }

            String finalOneHourBeforeTime = oneHourBeforeTime;
            fileWithFindingDate = directory.listFiles((d, name) -> name.contains(finalOneHourBeforeTime));

            //If we can't find file with one hour before, we must set file with two hours before current time
            String twoHoursBeforeTime = "";
            if(fileWithFindingDate.length == 0) {
                int twoHoursBefore = Integer.parseInt(dateMeasure.substring(11, 13)) - 2;
                if(twoHoursBefore == -1) {
                    twoHoursBefore = 23;
                    twoHoursBeforeTime = dateMeasure.substring(0, 11) + twoHoursBefore;
                } else if(twoHoursBefore >= 0 && twoHoursBefore <= 9) {
                    twoHoursBeforeTime = dateMeasure.substring(0, 11) + "0" + twoHoursBefore;
                } else {
                    twoHoursBeforeTime = dateMeasure.substring(0, 11) + twoHoursBefore;
                }
                String finalTwoHoursBeforeTime = twoHoursBeforeTime;
                fileWithFindingDate = directory.listFiles((d, name) -> name.contains(finalTwoHoursBeforeTime));
            }
        }

        File file;
        try {
            file = new File(pathSources + fileWithFindingDate[0].getPath().substring(13));
        } catch (Exception e) {
            System.out.println("Not found synop files");
            return null;
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("item");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node nNode = nodeList.item(i);
                Element eElement = (Element) nNode;
                //Index 1 - Station
                Node cElement = eElement.getChildNodes().item(1);
                if (station.getNameStation().equals(cElement.getTextContent())) {
                    //int hourOfMeasure = Integer.parseInt(eElement.getChildNodes().item(3).getTextContent());
                    double temp = Float.parseFloat(eElement.getChildNodes().item(4).getTextContent());
                    temp = Precision.round(temp, 2);
                    station.setTemperature(temp);

                    break;
                }
            }
        } catch (SAXException e1) {
            e1.printStackTrace();
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return station;
    }
}
