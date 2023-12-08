package simulator.models;

import java.time.LocalDate;
import java.util.ArrayList;

public class Event {
    private static int nextId = 1;
    private int id;
    private boolean isOver;
    private TypeEvent typeEvent;
    private ArrayList<Sensor> typeSensors;
    private LocalDate created_at;
    private LocalDate updated_at;

    public Event(boolean isOver, TypeEvent typeEvent, Sensor typeSensors) {
        this.id = generateUniqueId();
        this.isOver = isOver;
        this.typeEvent = typeEvent;
        this.typeSensors = new ArrayList<>();
        this.typeSensors.add(typeSensors);
        this.created_at = LocalDate.now();
        this.updated_at = LocalDate.now();
    }

    // Getter et Setter pour id
    public int getId() {
        return id;
    }

    // Getter et Setter pour isOver
    public boolean isOver() {
        return isOver;
    }

    public void setOver(boolean over) {
        isOver = over;
    }

    // Getter et Setter pour typeEvent
    public TypeEvent getTypeEvent() {
        return typeEvent;
    }

    public void setTypeEvent(TypeEvent typeEvent) {
        this.typeEvent = typeEvent;
    }

    // Getter et Setter pour typeSensors
    public Sensor getTypeSensor(int i) {
        return typeSensors.get(i); // Utilisation de la méthode get pour accéder à un élément de l'ArrayList
    }

    public void setTypeSensor(Sensor typeSensor) {
        this.typeSensors.add(typeSensor); // Ajout du TypeSensor à l'ArrayList
    }

    // Getter et Setter pour created_at
    public LocalDate getCreated_at() {
        return created_at;
    }

    // Getter et Setter pour updated_at
    public LocalDate getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDate updated_at) {
        this.updated_at = updated_at;
    }

    private synchronized int generateUniqueId() {
        return nextId++;
    }
}
