package pl.parser;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SynopXML {
    public static void main(String[] args) {
        Station station = getStation("Gdańsk", "2017-12-10");
        station.toString();
    }

    public static Station getStation(String nameStation, String date) {
        Station station = new Station(nameStation, date);
        //For all files from specific day
        String filePath = "C:/KSGDownload/SYNOP/2017-12-10_18-51-30/2017-12-10_18-51-30.xml";
        File xmlFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("item");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node nNode = nodeList.item(i);
                Element eElement = (Element) nNode;
                //Index 1 - Station
                Node cElement = eElement.getChildNodes().item(1);

                if (station.equals(cElement.getTextContent())) {
                    //Index 4 - Temperature(this will be parametrized)
                    station.addMeasureTemperature(Float.parseFloat(eElement.getChildNodes().item(4).getTextContent()));
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
