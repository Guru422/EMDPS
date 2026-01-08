package perds.controller;

import perds.algorithm.Dijkstra;
import perds.algorithm.Predictor;
import perds.graph.Graph;
import perds.model.DispatchCenter;
import perds.model.Incident;
import perds.model.ResponseUnit;

import java.util.*;

public class DispatchSystem {

    private final Graph graph;
    private final Predictor predictor;
    private final List<DispatchCenter> centers;

    private final Map<String, Deque<ResponseUnit>> availableUnitsByCenterId;
    private final Map<String, ActiveDispatch> activeDispatchesByIncidentId;

    private final PriorityQueue<Incident> incidentQueue;
    private int incidentCounter;

    public DispatchSystem() {
        this.graph = new Graph();
        this.predictor = new Predictor();
        this.centers = new ArrayList<>();
        this.availableUnitsByCenterId = new HashMap<>();
        this.activeDispatchesByIncidentId = new HashMap<>();
        this.incidentQueue = new PriorityQueue<>(
                Comparator.comparingInt((Incident i) -> severityRank(i.getSeverity())).reversed()
        );
        this.incidentCounter = 1;

        setupDemoNetwork();
        setupDemoCentersAndUnits();
    }

    public void run() {
        Random rng = new Random();

        for (int tick = 1; tick <= 20; tick++) {
            if (rng.nextDouble() < 0.60) {
                Incident incident = createRandomIncident(rng);
                incidentQueue.add(incident);
                predictor.record(incident.getLocation());
                System.out.println("NEW: " + incident.getId() + " at " + incident.getLocation() + " severity=" + incident.getSeverity());
            }

            if (rng.nextDouble() < 0.25) {
                applyRandomTrafficUpdate(rng);
            }

            while (!incidentQueue.isEmpty()) {
                Incident next = incidentQueue.peek();
                DispatchDecision decision = decideDispatch(next);
                if (decision == null) break;
                incidentQueue.poll();
                startDispatch(next, decision);
            }

            if (!activeDispatchesByIncidentId.isEmpty() && rng.nextDouble() < 0.40) {
                completeRandomDispatch(rng);
            }

            if (incidentQueue.isEmpty()) {
                prePositionOneUnit();
            }

            System.out.println("TICK " + tick + " | queued=" + incidentQueue.size() + " active=" + activeDispatchesByIncidentId.size());
            System.out.println("----");
        }
    }

    private void setupDemoNetwork() {
        for (String n : List.of("A", "B", "C", "D", "E", "F")) {
            graph.addNode(n);
        }

        graph.addEdge("A", "B", 4);
        graph.addEdge("B", "C", 3);
        graph.addEdge("C", "D", 6);
        graph.addEdge("A", "E", 7);
        graph.addEdge("E", "D", 2);
        graph.addEdge("B", "E", 5);
        graph.addEdge("D", "F", 4);
        graph.addEdge("C", "F", 8);
    }

    private void setupDemoCentersAndUnits() {
        addCenter(new DispatchCenter("DC1", "A"));
        addCenter(new DispatchCenter("DC2", "D"));
        addCenter(new DispatchCenter("DC3", "F"));

        addUnit("DC1", new ResponseUnit("U1", "ambulance"));
        addUnit("DC1", new ResponseUnit("U2", "police"));

        addUnit("DC2", new ResponseUnit("U3", "fire"));
        addUnit("DC2", new ResponseUnit("U4", "ambulance"));

        addUnit("DC3", new ResponseUnit("U5", "police"));
        addUnit("DC3", new ResponseUnit("U6", "fire"));
    }

    private void addCenter(DispatchCenter center) {
        centers.add(center);
        availableUnitsByCenterId.putIfAbsent(center.getId(), new ArrayDeque<>());
    }

    private void addUnit(String centerId, ResponseUnit unit) {
        availableUnitsByCenterId.putIfAbsent(centerId, new ArrayDeque<>());
        availableUnitsByCenterId.get(centerId).addLast(unit);
    }

    private Incident createRandomIncident(Random rng) {
        String location = pickRandomNode(rng);
        String severity = pickSeverity(rng);
        String id = "INC" + incidentCounter++;
        return new Incident(id, location, severity);
    }

    private String pickRandomNode(Random rng) {
        List<String> nodes = new ArrayList<>(graph.getNodes());
        return nodes.get(rng.nextInt(nodes.size()));
    }

    private String pickSeverity(Random rng) {
        double x = rng.nextDouble();
        if (x < 0.20) return "high";
        if (x < 0.60) return "medium";
        return "low";
    }

    private DispatchDecision decideDispatch(Incident incident) {
        String requiredType = requiredUnitType(incident.getSeverity());
        Dijkstra dijkstra = new Dijkstra(graph);

        DispatchDecision best = null;
        double bestScore = Double.POSITIVE_INFINITY;

        for (DispatchCenter c : centers) {
            ResponseUnit candidateUnit = findFirstMatchingUnit(c.getId(), requiredType);
            if (candidateUnit == null) continue;

            double distance = dijkstra.shortestPath(c.getLocation(), incident.getLocation());
            if (Double.isInfinite(distance)) continue;

            double score = scoreDispatch(incident.getSeverity(), distance);
            if (score < bestScore) {
                bestScore = score;
                best = new DispatchDecision(c, candidateUnit, distance, score);
            }
        }

        return best;
    }

    private ResponseUnit findFirstMatchingUnit(String centerId, String requiredType) {
        Deque<ResponseUnit> q = availableUnitsByCenterId.getOrDefault(centerId, new ArrayDeque<>());
        if (q.isEmpty()) return null;

        if ("any".equals(requiredType)) return q.peekFirst();

        for (ResponseUnit u : q) {
            if (u.getType().equalsIgnoreCase(requiredType)) return u;
        }
        return null;
    }

    private void startDispatch(Incident incident, DispatchDecision decision) {
        Deque<ResponseUnit> q = availableUnitsByCenterId.get(decision.center.getId());
        ResponseUnit chosen = removeUnitFromQueue(q, decision.unit.getId());
        if (chosen == null) return;

        activeDispatchesByIncidentId.put(
                incident.getId(),
                new ActiveDispatch(incident, decision.center, chosen, decision.distance, decision.score)
        );

        System.out.println(
                "DISPATCH: " + chosen.getId() + " (" + chosen.getType() + ") from " +
                decision.center.getId() + "@" + decision.center.getLocation() +
                " to " + incident.getId() + "@" + incident.getLocation() +
                " dist=" + decision.distance
        );
    }

    private ResponseUnit removeUnitFromQueue(Deque<ResponseUnit> q, String unitId) {
        if (q == null || q.isEmpty()) return null;

        Iterator<ResponseUnit> it = q.iterator();
        while (it.hasNext()) {
            ResponseUnit u = it.next();
            if (u.getId().equals(unitId)) {
                it.remove();
                return u;
            }
        }
        return null;
    }

    private void completeRandomDispatch(Random rng) {
        List<String> keys = new ArrayList<>(activeDispatchesByIncidentId.keySet());
        String incidentId = keys.get(rng.nextInt(keys.size()));
        ActiveDispatch done = activeDispatchesByIncidentId.remove(incidentId);

        availableUnitsByCenterId.get(done.center.getId()).addLast(done.unit);

        System.out.println("COMPLETE: " + done.incident.getId() + " by " + done.unit.getId()
                + " returned to " + done.center.getId());
    }

    private void applyRandomTrafficUpdate(Random rng) {
        List<String> nodes = new ArrayList<>(graph.getNodes());
        if (nodes.size() < 2) return;

        String a = nodes.get(rng.nextInt(nodes.size()));
        String b = nodes.get(rng.nextInt(nodes.size()));
        while (b.equals(a)) {
            b = nodes.get(rng.nextInt(nodes.size()));
        }

        double newWeight = 2 + rng.nextInt(10);
        graph.updateEdgeWeight(a, b, newWeight);

        System.out.println("UPDATE: traffic change edge " + a + "<->" + b + " newWeight=" + newWeight);
    }

    private void prePositionOneUnit() {
        String hotspot = predictor.predict();
        if (hotspot == null) return;

        DispatchCenter targetCenter = findCenterAtLocation(hotspot);
        if (targetCenter == null) return;

        for (DispatchCenter c : centers) {
            if (c.getId().equals(targetCenter.getId())) continue;

            Deque<ResponseUnit> fromQ = availableUnitsByCenterId.getOrDefault(c.getId(), new ArrayDeque<>());
            if (fromQ.isEmpty()) continue;

            ResponseUnit moved = fromQ.pollFirst();
            if (moved == null) continue;

            availableUnitsByCenterId.get(targetCenter.getId()).addLast(moved);

            System.out.println("PREPOSITION: moved " + moved.getId() + " to " + targetCenter.getId()
                    + "@" + targetCenter.getLocation() + " hotspot=" + hotspot);
            return;
        }
    }

    private DispatchCenter findCenterAtLocation(String location) {
        for (DispatchCenter c : centers) {
            if (c.getLocation().equals(location)) return c;
        }
        return null;
    }

    private String requiredUnitType(String severity) {
        int s = severityRank(severity);
        if (s >= 3) return "fire";
        if (s == 2) return "ambulance";
        return "any";
    }

    private static int severityRank(String severity) {
        if (severity == null) return 0;
        String s = severity.toLowerCase(Locale.ROOT);
        if (s.equals("high")) return 3;
        if (s.equals("medium")) return 2;
        if (s.equals("low")) return 1;
        return 0;
    }

    private static double scoreDispatch(String severity, double distance) {
        int s = severityRank(severity);
        double severityBias = (s == 3) ? 0.70 : (s == 2) ? 0.85 : 1.00;
        return distance * severityBias;
    }

    private static class DispatchDecision {
        private final DispatchCenter center;
        private final ResponseUnit unit;
        private final double distance;
        private final double score;

        private DispatchDecision(DispatchCenter center, ResponseUnit unit, double distance, double score) {
            this.center = center;
            this.unit = unit;
            this.distance = distance;
            this.score = score;
        }
    }

    private static class ActiveDispatch {
        private final Incident incident;
        private final DispatchCenter center;
        private final ResponseUnit unit;
        private final double distance;
        private final double score;

        private ActiveDispatch(Incident incident, DispatchCenter center, ResponseUnit unit, double distance, double score) {
            this.incident = incident;
            this.center = center;
            this.unit = unit;
            this.distance = distance;
            this.score = score;
        }
    }
}