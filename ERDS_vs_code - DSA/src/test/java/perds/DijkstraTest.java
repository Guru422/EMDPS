package perds;

import org.junit.jupiter.api.Test;
import perds.algorithm.Dijkstra;
import perds.graph.Graph;

import static org.junit.jupiter.api.Assertions.*;

public class DijkstraTest {

    @Test
    void shortestPathFindsBestRoute() {
        Graph g = new Graph();
        g.addEdge("A", "B", 5);
        g.addEdge("B", "C", 2);
        g.addEdge("A", "C", 10);

        Dijkstra d = new Dijkstra(g);
        assertEquals(7.0, d.shortestPath("A", "C"));
    }

    @Test
    void unreachableReturnsInfinity() {
        Graph g = new Graph();
        g.addNode("A");
        g.addNode("B");

        Dijkstra d = new Dijkstra(g);
        assertTrue(Double.isInfinite(d.shortestPath("A", "B")));
    }
}
