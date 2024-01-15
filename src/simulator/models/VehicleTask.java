package simulator.models;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class VehicleTask implements Runnable {
    private final JsonNode vehicleData;
    public ApiClient apiClient = new ApiClient();

    public VehicleTask(JsonNode vehicleData) {
        this.vehicleData = vehicleData;
    }

    @Override
    public void run() {
        try {
            JsonNode vehicleIdNode = vehicleData.get("vehicle_id");
            JsonNode id_pivot = vehicleData.get("pivot_event_vehicle_id");
            int vehicleId = vehicleIdNode.asInt();
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
                    double longitude = coordinatePair.get(0).asDouble();
                    double latitude = coordinatePair.get(1).asDouble();
                    System.out.print("[" + longitude + ", " + latitude + "] ");

                    ObjectMapper objectMapper = new ObjectMapper();
                    ObjectNode sensorNode = objectMapper.createObjectNode();
                    sensorNode.put("id", vehicleId);
                    sensorNode.put("longitude", longitude);
                    sensorNode.put("latitude", latitude);
                    System.out.println(sensorNode);
                    Vehicle reponse = apiClient.Put("http://localhost:3000/api/vehicle", sensorNode,
                            Vehicle.class);
                    System.out.println("Update vehicule " + vehicleId);
                    Thread.sleep(pause / 2);
                }
                System.out.println();
                System.out.println("    Temps de pause : " + pause);
            }
            System.out.println("\n Véhicule arrivé");

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode data = objectMapper.createObjectNode();
            data.put("vehicle_on_event_id", pivotId);
            System.out.println(data);
            EventVehicle apiResponse = apiClient.Put("http://localhost:3000/api/vehicle/event/arrived", data,
                    EventVehicle.class);
            System.out.println(apiResponse.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}