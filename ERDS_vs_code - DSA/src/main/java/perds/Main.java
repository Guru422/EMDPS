package perds;

import perds.controller.DispatchSystem;
import perds.graph.Graph;
import perds.algorithm.Dijkstra;

public class Main {
    public static void main(String[] args) {

        Graph g = new Graph();
        g.addEdge("A", "B", 5);
        g.addEdge("B", "C", 2);
        g.addEdge("A", "C", 10);

        Dijkstra d = new Dijkstra(g);
        System.out.println(d.shortestPath("A", "C"));

        DispatchSystem system = new DispatchSystem();
        system.run();
    }
}

