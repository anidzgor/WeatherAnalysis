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
    public static String pathSources = "C:/KSG/SYNOP/";

    public static void main(String[] args) {
        Station station = getStation("Gdańsk", "2018-02-16");

        if(station != null)
            System.out.println(station.toString());
    }

    public static Station getStation(String nameStation, final String date) {
        Station station = new Station(nameStation, date);

        //Files from one day
        File directory = new File(pathSources);
        File [] files = directory.listFiles((d, name) -> name.contains(date));

        for(File file : files) {
            file = new File(file.getPath() + "/" + file.getPath().substring(13) + ".xml");
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
                        //Index 4 - Temperature(this will be parametrized)
                        int hourOfMeasure = Integer.parseInt(eElement.getChildNodes().item(3).getTextContent());
                        station.addMeasureTemperature(hourOfMeasure, Float.parseFloat(eElement.getChildNodes().item(4).getTextContent()));
                    }
                }
            } catch (SAXException e1) {
                e1.printStackTrace();
            } catch (ParserConfigurationException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }//for all files

        //Add code to handle for 23th hour

        return station;
    }
}
