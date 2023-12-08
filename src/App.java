import simulator.models.Sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {

    private static final Random random = new Random();
    private static final List<Sensor> sensorList = new ArrayList<>();

    public static void main(String[] args) {

        // Planifie la création d'un nouveau Sensor toutes les 5 secondes
        scheduler.scheduleAtFixedRate(() -> createNewSensor(sensorList), 0, 5, TimeUnit.SECONDS);

        // Planifie l'incrémentation de l'intensité toutes les 10 secondes
        // scheduler.scheduleAtFixedRate(() -> incrementIntensity(sensorList), 10, 10,
        // TimeUnit.SECONDS);
    }

    private static void createNewSensor(List<Sensor> sensorList) {
        float randomLongitude = generateRandomCoordinateFloat(4.8320114f, 4.8902721f);
        float randomLatitude = generateRandomCoordinateFloat(45.7578137f, 45.7719444f);
        int randomIntensity = generateInt(0, 9);

        Sensor sensor = new Sensor(randomLongitude, randomLatitude, randomIntensity);
        sensorList.add(sensor);
        System.err.println("New Sensor: " + sensor.toString());
    }

    // private static void incrementIntensity(List<Sensor> sensorList) {
    // // Code pour incrémenter l'intensité des Sensors existants
    // for (Sensor sensor : sensorList) {
    // int currentIntensity = sensor.getIntensity();
    // if (currentIntensity >= 1 && currentIntensity <= 8) {
    // sensor.setIntensity(currentIntensity + 1);
    // } else if (currentIntensity == 0) {
    // if (sensor.getSleep() == 0) {
    // sensor.
    // } else {
    // sensor.setIntensity(sensor.getIntensity() - 1);
    // }
    // }
    // System.err.println(sensor.toString());
    // }
    // }

    private static float generateRandomCoordinateFloat(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private static int generateInt(int min, int max) {
        return random.nextInt(min) + max;
    }
}
