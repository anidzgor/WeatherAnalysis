package pl.parser.regression;

//This class represents error between WRF and Synop
public class PointMap {
    private double error;
    private int []coordinates;

    public PointMap(double error, int[] coordinates) {
        this.error = error;
        this.coordinates = coordinates;
    }

    //index mean row(0) or column(1)
    public int getCoordinates(int index) {
        return coordinates[index];
    }

    public double getError() {
        return error;
    }
}
