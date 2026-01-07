package perds.model;

public class ResponseUnit {
    private String id;
    private String type;

    public ResponseUnit(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
