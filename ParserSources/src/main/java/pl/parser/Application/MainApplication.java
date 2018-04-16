package pl.parser.Application;

import org.xml.sax.SAXException;
import pl.parser.Implementation.MapCreator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class MainApplication {
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, TransformerException {

        MapCreator mapCreator = new MapCreator();

        //Must be parametrized to invoke from command line
        char choose = 'a';
        String date = "2018-04-16_10";

        if(choose == 'a') {  //Archive data
            ArchiveData archiveData = new ArchiveData(mapCreator);
            archiveData.showFiveHoursWithPrediction(date);   //date must be parametrized
        } else if(choose == 'o') {  //Observational data
            ObservationalData observationalData = new ObservationalData(mapCreator);
            observationalData.predictFiveHours();
        }
    }
}
