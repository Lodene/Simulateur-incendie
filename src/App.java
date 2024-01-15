import simulator.models.ApiClient;
import simulator.models.Event;
import simulator.models.EventSensor;
import simulator.models.EventVehicle;
import simulator.models.RouteSegment;
import simulator.models.Sensor;
import simulator.models.Vehicle;
import simulator.models.VehicleReturnTask;
import simulator.models.VehicleTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class App {
    public ApiClient apiClient = new ApiClient();
    private static List<Integer> processingActiveVehicles = new ArrayList<>();
    private static List<Integer> processingReturnVehicles = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Simu is running");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable come_back = new Runnable() {
            public ApiClient apiClient = new ApiClient();

            public void run() {
                String apiResponse = fetchApiResponse("http://localhost:3000/api/vehicle/toreturn");
                if (apiResponse != null) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode rootNode = mapper.readTree(apiResponse);
                        List<JsonNode> vehiclesData = new ArrayList<>();

                        for (JsonNode node : rootNode) {
                            vehiclesData.add(node);
                        }

                        System.out.println(vehiclesData.size());
                        // Lancement des threads pour chaque véhicule
                        for (JsonNode vehicleData : vehiclesData) {
                            try {
                                JsonNode vehicleIdNode = vehicleData.get("vehicle_id");
                                JsonNode id_pivot = vehicleData.get("pivot_event_vehicle_id");
                                int vehicleId = vehicleIdNode.asInt();

                                if (!processingReturnVehicles.contains(vehicleId)) {

                                    int pivotId = id_pivot.asInt();
                                    JsonNode routeProperties = vehicleData.get("routeProperties");

                                    System.out.println("Vehicle ID: " + vehicleId);

                                    for (JsonNode routeNode : routeProperties) {
                                        JsonNode coordinates = routeNode.get("coordinates");
                                        double duration = routeNode.get("duration").asDouble();
                                        double sizeOfCoordinates = coordinates.size();
                                        double pauseDouble = (duration / sizeOfCoordinates) * 1000;
                                        long pause = (long) pauseDouble; // Convertir en long

                                        System.out.println("  Segment Duration: " + duration);
                                        System.out.print("  Coordinates: ");
                                        for (JsonNode coordinatePair : coordinates) {
                                            Vehicle stop = apiClient.getSingle(
                                                    "http://localhost:3000/api/vehicle/isbusy?id=" + vehicleId,
                                                    Vehicle.class);
                                            System.out.println(stop);
                                            double longitude = coordinatePair.get(0).asDouble();
                                            double latitude = coordinatePair.get(1).asDouble();
                                            System.out.print("[" + longitude + ", " + latitude + "] ");

                                            ObjectMapper objectMapper = new ObjectMapper();
                                            ObjectNode sensorNode = objectMapper.createObjectNode();
                                            sensorNode.put("id", vehicleId);
                                            sensorNode.put("longitude", longitude);
                                            sensorNode.put("latitude", latitude);
                                            System.out.println(sensorNode);
                                            Vehicle reponse = apiClient.Put("http://localhost:3000/api/vehicle",
                                                    sensorNode,
                                                    Vehicle.class);
                                            System.out.println("Update vehicule " + vehicleId);
                                            Thread.sleep(pause);
                                        }
                                        System.out.println();
                                        System.out.println("    Temps de pause : " + pause);
                                    }
                                    System.out.println("\n Véhicule arrivé");

                                    ObjectMapper objectMapper = new ObjectMapper();
                                    ObjectNode data = objectMapper.createObjectNode();
                                    data.put("vehicle_on_event_id", pivotId);
                                    System.out.println(data);
                                    EventVehicle apiResponseTwo = apiClient.Put(
                                            "http://localhost:3000/api/vehicle/event/arrived", data,
                                            EventVehicle.class);
                                    System.out.println(apiResponseTwo.toString());
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Runnable put_off_event = new Runnable() {
            public void run() {
                String apiResponse = fetchApiResponse("http://localhost:3000/api/event/tostop");
                System.out.println("coté reel : " + apiResponse);
                String apiResponsesimu = fetchApiResponse("http://localhost:3001/api/event/tostop");
                System.out.println("coté simu : " + apiResponsesimu);
            }
        };

        Runnable incrementSensorIntensity = new Runnable() {
            public ApiClient apiClient = new ApiClient();

            public void run() {
                List<Event> events = apiClient.getList("http://localhost:3001/api/event", Event[].class);
                for (Event event : events) {
                    Integer vehicle_count = returnInt(
                            "http://localhost:3000/api/event/vehicle?eventId=" + event.getId());
                    for (EventSensor eventSensor : event.getSensors()) {
                        Sensor sensor = eventSensor.getSensor();

                        if (sensor.getIntensity() < 9) {
                            if (vehicle_count == 0 || sensor.getIntensity() > 4 && vehicle_count == 2) {
                                sensor.setIntensity(sensor.getIntensity() + 1);

                                // Créer l'objet JSON pour ce capteur spécifique
                                ObjectMapper objectMapper = new ObjectMapper();
                                ObjectNode sensorNode = objectMapper.createObjectNode();
                                sensorNode.put("id", sensor.getId());
                                sensorNode.put("intensity", sensor.getIntensity());

                                ObjectNode json = objectMapper.createObjectNode();
                                json.set("sensor", sensorNode);
                                // Mettre à jour le capteur via l'API
                                Sensor updatedSensor = apiClient.Put("http://localhost:3001/api/sensor", json,
                                        Sensor.class);

                                // Afficher la confirmation de la mise à jour
                                System.out.println("L'intensité du sensor " + updatedSensor.getId()
                                        + " a été augmentée à " + updatedSensor.getIntensity());
                            }
                        }
                    }
                }
            }
        };

        Runnable decrementSensorIntensity = new Runnable() {
            public ApiClient apiClient = new ApiClient();

            public void run() {
                List<Event> events = apiClient.getList("http://localhost:3001/api/event", Event[].class);
                for (Event event : events) {
                    Integer vehicle_count = returnInt(
                            "http://localhost:3000/api/event/vehicle?eventId=" + event.getId());

                    for (EventSensor eventSensor : event.getSensors()) {
                        Sensor sensor = eventSensor.getSensor();

                        if (sensor.getIntensity() > 0) {
                            if (vehicle_count == 2 || sensor.getIntensity() < 5 && vehicle_count == 1) {
                                sensor.setIntensity(sensor.getIntensity() - 1);
                                // Créer l'objet JSON pour ce capteur spécifique
                                ObjectMapper objectMapper = new ObjectMapper();
                                ObjectNode sensorNode = objectMapper.createObjectNode();
                                sensorNode.put("id", sensor.getId());
                                sensorNode.put("intensity", sensor.getIntensity());

                                ObjectNode json = objectMapper.createObjectNode();
                                json.set("sensor", sensorNode);
                                // Mettre à jour le capteur via l'API
                                Sensor updatedSensor = apiClient.Put("http://localhost:3001/api/sensor", json,
                                        Sensor.class);
                                if (sensor.getIntensity() == 0) {
                                    Sensor updatedRealSensor = apiClient.Put("http://localhost:3000/api/sensor", json,
                                            Sensor.class);
                                }

                                // Afficher la confirmation de la mise à jour
                                System.out
                                        .println("L'intensité du sensor " + updatedSensor.getId() + " a été diminuée à "
                                                + updatedSensor.getIntensity());

                            }
                        }
                    }
                }
            }
        };

        // Exécuter la tâche toutes les 10 secondes après un délai initial de 0 seconde
        scheduler.scheduleAtFixedRate(put_off_event, 0, 5, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(decrementSensorIntensity, 0, 5,
                TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(incrementSensorIntensity, 0, 20,
                TimeUnit.SECONDS);
        // scheduler.scheduleAtFixedRate(come_back, 0, 5, TimeUnit.SECONDS);

        while (true) {
            /*
             * 
             * Envoie les véhicules sur intervention
             * 
             */
            String apiResponse = fetchApiResponse("http://localhost:3000/api/vehicle/event");

            // System.out.println(apiResponse);
            if (apiResponse != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(apiResponse);
                    List<JsonNode> vehiclesData = new ArrayList<>();

                    for (JsonNode node : rootNode) {
                        vehiclesData.add(node);
                    }

                    // System.out.println(vehiclesData.size());
                    // Lancement des threads pour chaque véhicule
                    for (JsonNode vehicleData : vehiclesData) {
                        Integer vehicleId = vehicleData.get("vehicle_id").asInt();

                        if (!processingActiveVehicles.contains(vehicleId)) {
                            processingActiveVehicles.add(vehicleId);

                            Thread vehicleThread = new Thread(() -> {
                                try {
                                    VehicleTask vehicleTask = new VehicleTask(vehicleData);
                                    vehicleTask.run(); // Exécuter la tâche du véhicule
                                } finally {
                                    // Ce bloc s'exécutera après la fin de toutes les opérations dans run()
                                    System.out.println("Fin du thread");
                                    processingActiveVehicles.remove(vehicleId);
                                }
                            });
                            vehicleThread.start();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String apiResponseTwo = fetchApiResponse("http://localhost:3000/api/vehicle/toreturn");
            if (apiResponseTwo != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(apiResponseTwo);
                    List<JsonNode> vehiclesData = new ArrayList<>();

                    for (JsonNode node : rootNode) {
                        vehiclesData.add(node);
                    }

                    System.out.println(vehiclesData.size());
                    // Lancement des threads pour chaque véhicule
                    for (JsonNode vehicleData : vehiclesData) {
                        Integer vehicleId = vehicleData.get("vehicle_id").asInt();

                        if (!processingReturnVehicles.contains(vehicleId)) {
                            processingReturnVehicles.add(vehicleId);

                            Thread vehicleThread = new Thread(() -> {
                                try {
                                    VehicleReturnTask vehicleTask = new VehicleReturnTask(vehicleData);
                                    vehicleTask.run(); // Exécute la tâche du véhicule
                                } finally {
                                    // Bloc exécuté après la fin des opérations
                                    System.out.println("Fin du thread de retour du véhicule");
                                    processingReturnVehicles.remove(vehicleId);
                                }
                            });
                            vehicleThread.start();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Thread.sleep(5000);
        }

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

    private static int returnInt(String apiUrl) {
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

            // Supposons que la réponse de l'API est un nombre entier sous forme de texte
            return Integer.parseInt(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // Retourne une valeur par défaut en cas d'erreur, par exemple -1
        }
    }

    private static String fetchApiResponse(String apiUrl) {
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

            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
