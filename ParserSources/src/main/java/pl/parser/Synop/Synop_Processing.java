package pl.parser.Synop;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pl.parser.Station;

public class Synop_Processing {
    public static String pathSources = "C:/KSG/SYNOP/";

    //Get temperatures from specific hours back
    public static Station getTemperatures(String nameStation, String currentTime, int backHours) {
        //Check day
        String day = currentTime.substring(0, 10);

        File directory = new File(pathSources);
        File[] files = directory.listFiles((d, name) -> name.contains(day));
        File[] fs = new File[0];

        int[] backHoursTable = new int[backHours];

        //if we haven't enough data we should get it from previous day
        if(files.length < backHours) {

        } else {
            int[] hours = new int[files.length];
            int index = 0;
            //Get amount of backHours - oldest hours
            for(File file : files) {
                hours[index++] = Integer.parseInt(file.getName().substring(11, 13));
            }
            Arrays.sort(hours);

            String hourFromString = currentTime.substring(11, 13);
            int hour = Integer.parseInt(hourFromString);
            for(int i = 0; i < backHours; i++) {
                backHoursTable[i] = hour - i;
            }

            //Files which we want to analyze
            fs = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    String prefix;
                    for(int i: backHoursTable) {
                        prefix = "";
                        if(i <= 9 && i >= 0)
                            prefix = "0";
                        if(name.startsWith(day + "_" + prefix + i))
                            return true;
                    }
                    return false;
                }
            });
        }//else

        Station station = new Station(nameStation, currentTime);

        for(File file : fs) {

            if(file.getName().substring(11, 13).equals("00"))
                continue;

            file = new File(file.getPath() + "/" + file.getPath().substring(13) + ".xml");

            if(!file.exists())
                continue;

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

                        int []table = new int[]{hourOfMeasure};
                        station.initializeAnalyzesHours(table);


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

        return station;
    }

    //Analyze one day from hour 0 to current
    public static Station getStation(String nameStation, final String date) {
        Station station = new Station(nameStation, date);

        //Files from one day
        File directory = new File(pathSources);
        File [] files = directory.listFiles((d, name) -> name.contains(date));

        for(File file : files) {

            if(file.getName().substring(11, 13).equals("00"))
                continue;

            file = new File(file.getPath() + "/" + file.getPath().substring(13) + ".xml");

            if(!file.exists())
                continue;

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
