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

    public Station getTemperatures(String nameStation, String dateMeasure) {
        Station station = new Station(nameStation, dateMeasure);

        //Get specific file
        File directory = new File(pathSources);
        File[] fileWithFindingDate = directory.listFiles((d, name) -> name.contains(dateMeasure));
        File file = new File(pathSources + fileWithFindingDate[0].getPath().substring(13));

        if(!file.exists())
            return null;

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
