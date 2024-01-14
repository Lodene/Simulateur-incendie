import simulator.models.ApiClient;
import simulator.models.Event;
import simulator.models.EventSensor;
import simulator.models.Sensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

public class App {

    private static final String EVENT_API_REEL_URL_GET_SENSORS_ON = "http://localhost:3000/api/sensor/active";
    private static final String EVENT_API_REEL_URL_PUT_UPDATE_SENSORS = "http://localhost:3000/api/sensor";
    private static final String EVENT_API_REEL_URL_GET_EVENT_OFF = "http://localhost:3000/api/event/tostop";
    private static final String EVENT_API_REEL_URL_GET_EVENT_SENSOR = "http://localhost:3000/api/sensor?id=";
    private static final String EVENT_API_REEL_URL_GET_CAMIONS_SUR_PLACE = "http://localhost:3000/api/event/vehicle?eventId=";
    private static final String EVENT_API_SIMU_URL_GET_EVENT_OFF = "http://localhost:3001/api/event/tostop";
    private static ApiClient apiClient;

    public static void main(String[] args) throws IOException {
        System.out.println("Simu is running");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable put_off_event = new Runnable() {
            public void run() {
                /*
                 * 
                 * On éteints les events qui doivent etre etient
                 * 
                 */
                List<Event> events_to_off = fetchData(EVENT_API_REEL_URL_GET_EVENT_OFF,
                        Event[].class);
                for (Event event : events_to_off) {
                    System.out.println(event.getId());
                    String data = "{ \"event\": " + event.getId() + " }";
                    System.out.println(data);
                    putDataEvent(EVENT_API_REEL_URL_GET_EVENT_OFF, data, Event.class);
                }

                List<Event> events_simu_to_off = fetchData(EVENT_API_SIMU_URL_GET_EVENT_OFF,
                        Event[].class);
                for (Event event : events_simu_to_off) {
                    System.out.println(event.getId());
                    String data = "{ \"event\": " + event.getId() + " }";
                    System.out.println(data);
                    putDataEvent(EVENT_API_SIMU_URL_GET_EVENT_OFF, data, Event.class);
                }
            }
        };

        Runnable incremente_decrement_sensor = new Runnable() { // Eteint les feux qui doivent etre eteint et incrémente
                                                                // l'intensité toutes les 10s
            public void run() {
                /*
                 * 
                 * On incrémente maitenant les feux allumé au bout des 10 secondes
                 * 
                 */
                List<Sensor> sensors = fetchData(EVENT_API_REEL_URL_GET_SENSORS_ON,
                        Sensor[].class);
                for (Sensor sensor : sensors) {
                    if (sensor.getIntensity() != 0) {
                        System.out.println("test : " + sensor.getId());
                        Sensor event = apiClient.getSingle("http://localhost:3000/api/sensor?id=" + sensor.getId(),
                                Sensor.class);
                        System.out.println(event.getId());
                        System.out.println("ouou");

                        Integer intensite = 0;
                        // if (si des pompiers sont sur place){
                        // intensite = sensor.getIntensity() - 1;
                        // } else {
                        // intensite = sensor.getIntensity() + 1;
                        // }
                        // System.out.println(sensor.getId());
                        String data = "{ \"sensor\" : { \"id\": " + sensor.getId() + ", \"intensity\":" + intensite
                                + " } }";
                        // Sensor updatedSensor = putDataSensor(EVENT_API_REEL_URL_PUT_UPDATE_SENSORS,
                        // data,
                        // Sensor.class);
                        // System.out.println(updatedSensor);
                    }
                }
            }
        };

        Runnable vehicle_pompier_vers_feu = new Runnable() {
            public void run() {

            }
        };

        // Exécuter la tâche toutes les 10 secondes après un délai initial de 0 seconde
        // scheduler.scheduleAtFixedRate(put_off_event, 0, 1, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(incremente_decrement_sensor, 0, 10, TimeUnit.SECONDS);
        // scheduler.scheduleAtFixedRate(vehicle_pompier_vers_feu, 0, 1,
        // TimeUnit.SECONDS);

    }

    private static <T> List<T> fetchData(String apiUrl, Class<T[]> responseType) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            String apiResponse = response.toString();
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return Collections.synchronizedList(Arrays.asList(mapper.readValue(apiResponse, responseType)));
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private static <T> T putDataSensor(String apiUrl, String jsonInputString, Class<T> responseType) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");

            // Écriture des données JSON dans le corps de la requête
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Lecture de la réponse
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            // Désérialisation de la réponse
            String apiResponse = response.toString();
            ObjectMapper mapper = new ObjectMapper();

            // Extraction et désérialisation de l'objet "oneSensor"
            JsonNode rootNode = mapper.readTree(apiResponse);
            JsonNode sensorNode = rootNode.path("oneSensor");
            return mapper.treeToValue(sensorNode, responseType);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // ou une valeur par défaut appropriée
        }
    }

    private static <T> T putDataEvent(String apiUrl, String jsonInputString, Class<T> responseType) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");

            // Écriture des données JSON dans le corps de la requête
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Lecture de la réponse
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            // Désérialisation de la réponse
            String apiResponse = response.toString();
            ObjectMapper mapper = new ObjectMapper();

            // Extraction et désérialisation de l'objet "updatedEvent"
            JsonNode rootNode = mapper.readTree(apiResponse);
            JsonNode updatedEventNode = rootNode.path("updatedEvent");
            return mapper.treeToValue(updatedEventNode, responseType);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // ou une valeur par défaut appropriée
        }
    }

}
