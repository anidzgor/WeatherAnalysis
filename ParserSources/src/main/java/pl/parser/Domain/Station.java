package pl.parser.Domain;

import java.util.Arrays;

public class Station {
    private String nameStation;
    private String dateOfObservation;
    private double[] temperatures;
    private int[] hoursMeasures;
    private int[] coordinatesCSV;

    public Station(String nameStation, String dateOfObservation) {
        this.nameStation = nameStation;
        this.dateOfObservation = dateOfObservation;
        temperatures = new double[24];
        Arrays.fill(temperatures, 0.0f);
    }

    public int[] getCoordinatesCSV() {
        return coordinatesCSV;
    }

    public void initializeAnalyzesHours(int []hoursMeasures) {
        this.hoursMeasures = hoursMeasures;
    }

    public int getHoursMeasures() {
        return hoursMeasures[0];
    }

    public void initialiazeCoordinates(int [] coord) {
        coordinatesCSV = coord;
     }

    public double[] getTemp() {
        return temperatures;
    }

    public double getTemperatures(int hour) {
        return temperatures[hour];
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