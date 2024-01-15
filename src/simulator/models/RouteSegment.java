package simulator.models;

import java.util.List;

public class RouteSegment {
    private List<List<Double>> coordinates;
    private double duration;

    // Getters et setters
    public List<List<Double>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }
}
