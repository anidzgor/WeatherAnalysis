package pl.parser.Api;

import org.xml.sax.SAXException;
import pl.parser.Domain.PointMap;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

public interface IMapCreator {
    //Get names station from xml
    String[] getStationFromXML() throws ParserConfigurationException, IOException, SAXException;

    //Create csv(170x325) with base points, and fill csv with interpolation between this points
    void createCSVWithInterpolation(String filePath, List<PointMap> points) throws IOException;
}
