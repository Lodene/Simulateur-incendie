package simulator.models;

import java.sql.Timestamp;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EventSensor {
    @JsonProperty("event_id")
    private long event_id;
    @JsonProperty("sensor_id")
    private long sensor_id;
    @JsonProperty("created_at")
    private Timestamp created_at;
    @JsonProperty("sensor")
    private Sensor sensor;
    @JsonProperty("event")
    private Event event;

    public EventSensor() {
    }

    public EventSensor(long event_id, long sensor_id, Timestamp created_at, Event event, Sensor sensor) {
        this.event_id = event_id;
        this.sensor_id = sensor_id;
        this.created_at = created_at;
        this.event = event;
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Event getEvent() {
        return event;
    }

}
