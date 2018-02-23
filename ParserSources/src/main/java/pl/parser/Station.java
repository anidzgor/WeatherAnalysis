package pl.parser;

import java.util.Arrays;

public class Station {
    private String nameStation;
    private String dateOfObservation;
    private Float[] temperatures;

    public Station(String nameStation, String dateOfObservation) {
        this.nameStation = nameStation;
        this.dateOfObservation = dateOfObservation;
        temperatures = new Float[24];
        Arrays.fill(temperatures, 0.0f);
    }

    public String getNameStation() {
        return nameStation;
    }

    public void addMeasureTemperature(int hour, Float result) {
        temperatures[hour] = result;
    }

    @Override
    public String toString() {
        return "Station{" +
                "nameStation='" + nameStation + '\'' +
                ", dateOfObservation='" + dateOfObservation + '\'' +
                ", temperatures=" + Arrays.toString(temperatures) +
                '}';
    }
}