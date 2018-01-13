package pl.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Station {
    public String nameStation;
    public String dateOfObservation;
    public List<Float> temperatures;

    public Station(String nameStation, String dateOfObservation) {
        this.nameStation = nameStation;
        this.dateOfObservation = dateOfObservation;
        temperatures = new ArrayList<Float>(Collections.nCopies(24,0.0f));
    }

    public void setNameStation(String nameStation) {
        this.nameStation = nameStation;
    }

    public void setDateOfObservation(String dateOfObservation) {
        this.dateOfObservation = dateOfObservation;
    }

    public void addMeasureTemperature(Float result) {
        temperatures.add(result);
    }

    @Override
    public String toString() {
        return "Station{" +
                "nameStation='" + nameStation + '\'' +
                ", dateOfObservation='" + dateOfObservation + '\'' +
                ", temperatures=" + temperatures +
                '}';
    }
}
