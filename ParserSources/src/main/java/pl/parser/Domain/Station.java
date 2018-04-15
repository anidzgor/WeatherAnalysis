package pl.parser.Domain;

public class Station {
    private String nameStation;
    private String dateOfObservation;
    private double temperature;
    private int[] coordinatesCSV;
    private String sourceFile;

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Station(String nameStation, String dateOfObservation) {
        this.nameStation = nameStation;
        this.dateOfObservation = dateOfObservation;
    }

    public int[] getCoordinatesCSV() {
        return coordinatesCSV;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getTemperature() {
        return temperature;
    }

    public void initialiazeCoordinates(int [] coord) {
        coordinatesCSV = coord;
     }

    public String getNameStation() {
        return nameStation;
    }

    @Override
    public String toString() {
        return "Station{" +
                "nameStation='" + nameStation + '\'' +
                ", dateOfObservation='" + dateOfObservation + '\'' +
                ", temperature=" + temperature +
                '}';
    }
}