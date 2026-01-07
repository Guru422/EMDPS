package perds.model;

public class Incident {
    private String id;
    private String location;
    private String severity;

    public Incident(String id, String location, String severity) {
        this.id = id;
        this.location = location;
        this.severity = severity;
    }

    public String getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public String getSeverity() {
        return severity;
    }
}
