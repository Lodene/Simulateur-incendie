package simulator.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class VehicleReturnTask implements Runnable {
    private JsonNode vehicleData;

    public VehicleReturnTask(JsonNode vehicleData) {
        this.vehicleData = vehicleData;
    }

    @Override
    public void run() {
        try {
            ApiClient apiClient = new ApiClient();
            // Supposons que vehicleData contient les informations d'un seul véhicule

            // Extrait les informations nécessaires de vehicleData
            JsonNode vehicleIdNode = vehicleData.get("vehicle_id");
            JsonNode id_pivot = vehicleData.get("pivot_event_vehicle_id");
            int vehicleId = vehicleIdNode.asInt();
            int pivotId = id_pivot.asInt();
            JsonNode routeProperties = vehicleData.get("routeProperties");

            System.out.println("Vehicle ID: " + vehicleId);

            // Traite chaque segment de route pour ce véhicule
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
            EventVehicle apiResponseTwo = apiClient.Put(
                    "http://localhost:3000/api/vehicle/event/arrived", data,
                    EventVehicle.class);
            System.out.println(apiResponseTwo.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
