package perds.model;

public class Edge {
    private String to;
    private double weight;

    public Edge(String to, double weight) {
        this.to = to;
        this.weight = weight;
    }

    public String getTo() {
        return to;
    }

    public double getWeight() {
        return weight;
    }
}
