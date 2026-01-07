package perds.graph;


import perds.model.Edge;
import java.util.*;

public class Graph {

    private final Map<String, List<Edge>> adjacencyList;
    
    
    public Graph() {
        this.adjacencyList = new HashMap<>();
    }

    public void addNode(String node) {
        adjacencyList.putIfAbsent(node, new ArrayList<>());

    }

    public void removeNode(String node) {
        adjacencyList.remove(node);
        for (List<Edge> edges : adjacencyList.values()) {
            edges.removeIf(edge -> edge.getTo().equals(node));

            
        }

    }


    public void addEdge(String from, String to, double weight) {
        addNode(from);
        addNode(to);

        adjacencyList.get(from).add(new Edge(to, weight));
        adjacencyList.get(to).add(new Edge(from, weight));
    }

    public void updateEdgeWeight(String from, String to, double newWeight) {
        for (Edge edge : adjacencyList.getOrDefault(from, List.of())) {
            if (edge.getTo().equals(to)) {
                edge.setWeight(newWeight);

            }
        }

        for (Edge edge : adjacencyList.getOrDefault(to, List.of())) {
            if (edge.getTo().equals(from)) {
                edge.setWeight(newWeight);
            }
        }

    }

    public List<Edge> getEdges(String node) {
        return adjacencyList.getOrDefault(node, List.of());

    }

    public Set<String> getNodes() {
        return adjacencyList.keySet();
    }
}