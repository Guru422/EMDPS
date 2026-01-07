package perds.model;

public class DispatchCenter {
    private String id;
    private String location;

    public DispatchCenter(String id, String location) {
        this.id = id;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }
}
