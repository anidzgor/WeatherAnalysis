package pl.parser.Domain;

//This class represents value about specify coordinates
public class PointMap {
    private double valuePoint;
    private int []coordinates;

    public PointMap(double valuePoint, int[] coordinates) {
        this.valuePoint = valuePoint;
        this.coordinates = coordinates;
    }

    //index mean row(0) or column(1)
    public int getCoordinates(int index) {
        return coordinates[index];
    }

    public double getValuePoint() {
        return valuePoint;
    }
}
