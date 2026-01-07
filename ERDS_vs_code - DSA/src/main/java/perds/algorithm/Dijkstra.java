package perds.algorithm;

import perds.graph.Graph;
import perds.model.Edge;

import java.util.*;

public class Dijkstra {

    private final Graph graph;


    public Dijkstra(Graph graph) {
        this.graph = graph;
    }

    public double shortestPath(String start, String target) {
        if (start == null || target == null) return Double.POSITIVE_INFINITY;
        if (start.equals(target)) return 0.0;
        if (!graph.getNodes().contains(start) || !graph.getNodes().contains(target)) {
            return Double.POSITIVE_INFINITY);
        }

        Map<String, Double> dist = new HashMap<>();
        for (String node : graph.getNodes()) {
            dist.put(node, Double.POSITIVE_INFINITY);

        }
        dist.put(start, 0.0);

        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));
        pq.add(new NodeDistance(start, 0.0));

        set<String> settled = new HashSet<>();

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            String u = current.node;

            if (settled.contains(u)) continue;
            settled.add(u);

            if (u.equals(target)) {
                return dist.get(u);
            }

            for (Edge edge : graph.getEdges(u)) {
                String v = edge.getTo();
                if (settled.contains(v)) continue;

                double alt = dist.get(u) + edge.getWeight();
                if (alt < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, alt);
                    pq.add(new NodeDistance(v, alt));
                }


                
            }
        }

        return Double.POSITIVE_INFINITY;
    }

    private static class NodeDistance {
        private final String node;
        private final double distance;

        private NodeDistance(String node, double distance) {
            this.node = node;
            this.distance = distance;
        }
    }

}
