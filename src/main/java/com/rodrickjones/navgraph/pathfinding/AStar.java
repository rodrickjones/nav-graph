package com.rodrickjones.navgraph.pathfinding;

import com.rodrickjones.navgraph.Graph;
import com.rodrickjones.navgraph.edges.Edge;
import com.rodrickjones.navgraph.requirements.Requirement;
import com.rodrickjones.navgraph.requirements.RequirementContext;
import com.rodrickjones.navgraph.util.Frontier;
import com.rodrickjones.navgraph.vertices.Vertex;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class AStar extends PathfindingAlgorithm<Graph> {
    public AStar(Graph graph) {
        super(graph);
    }

    @Override
    public Path findPath(Vertex origin, Collection<Vertex> destinations, RequirementContext context) {
        long start = System.currentTimeMillis();
        Queue<Node> frontier = new Frontier<>(Comparator.comparingDouble(n -> n.getCost() + n.getHeuristic()));
        frontier.offer(new Node(origin, null, null, 0, 0));
        Map<Vertex, Node> explored = new HashMap<>();
        while (!frontier.isEmpty()) {
            Node current = frontier.poll();
            if (destinations.contains(current.getVertex())) {
                Path res = backtrackAndBuildPath(current);
                log.debug("Path built in {}ms: {}", System.currentTimeMillis() - start, res);
                log.trace("Explored + frontier: {}", explored.size() + frontier.size());
                return res;
            }
            explored.put(current.getVertex(), current);
            Iterable<Edge> edges = graph.getEdges(current.getVertex());
            if (edges != null) {
                for (Edge edge : edges) {
                    Requirement requirement = edge.getRequirement();
                    if (requirement != null && !requirement.isMet(context)) {
                        continue;
                    }
                    Node node = explored.get(edge.getDestination());
                    double cost = current.getCost() + edge.getCost();
                    if (node == null) {
                        node = new Node(edge.getDestination(), current, edge, cost, heuristic(destinations, edge.getDestination()));
                        if (!frontier.contains(node)) {
                            frontier.offer(node);
                        }
                    } else if (cost < node.getCost()) {
                        node.setParent(current, edge);
                    }
                }
            }
        }
        log.debug("Unable to build path, {}ms: {} -> {}", System.currentTimeMillis() - start, origin, destinations);
        return null;
    }

    public double heuristic(Collection<Vertex> destinations, Vertex currentVertex) {
        double min =  Double.MAX_VALUE;
        for (Vertex destination : destinations) {
            double h = Math.sqrt(Math.pow(destination.getX() - currentVertex.getX(), 2) + Math.pow(destination.getY() - currentVertex.getY(), 2));
            if (h < min) {
                min = h;
            }
        }
        return min;
    }
}
