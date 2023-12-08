package simulator.models;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Sensor {
    private static int nextId = 1;
    private int id;
    private float longitude;
    private float latitude;
    private int intensity;
    private int sleep;

    public Sensor(float longitude, float latitude, int intensity) {
        this.id = generateUniqueId();
        this.longitude = longitude;
        this.latitude = latitude;
        this.intensity = intensity;
        this.sleep = 0;
    }

    public String toString() {
        return "Sensor{" +
                "id=" + id +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", intensity=" + intensity +
                '}';
    }

    private synchronized int generateUniqueId() {
        return nextId++;
    }

    public int getId() {
        return id;
    }

    // Getter et Setter pour longitude
    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    // Getter et Setter pour latitude
    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    // Getter et Setter pour intensity
    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    // Getter et Setter pour sleep
    public int getSleep() {
        return sleep;
    }

    public void setSleep(int sleep) {
        this.sleep = sleep;
    }
}
