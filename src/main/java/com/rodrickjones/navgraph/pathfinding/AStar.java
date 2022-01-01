package com.rodrickjones.navgraph.pathfinding;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.graph.Graph;
import com.rodrickjones.navgraph.requirement.Requirement;
import com.rodrickjones.navgraph.requirement.RequirementContext;
import com.rodrickjones.navgraph.util.Frontier;
import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;

@Slf4j
public class AStar<G extends Graph<?>> extends PathfindingAlgorithm<G, Vertex> {

    private static final BiFunction<Graph<Vertex>, Vertex, Stream<Edge<Vertex>>> SIMPLE_EDGES_FUNCTION = Graph::edges;

    private final BiFunction<G, Vertex, Stream<Edge<Vertex>>> edgesFunction;

    public AStar(@NonNull G graph, @NonNull BiFunction<G, Vertex, Stream<Edge<Vertex>>> edgesFunction) {
        super(graph);
        this.edgesFunction = edgesFunction;
    }

    @SuppressWarnings("unchecked")
    public AStar(Graph<Vertex> graph) {
        this((G) graph, (BiFunction<G, Vertex, Stream<Edge<Vertex>>>) SIMPLE_EDGES_FUNCTION);
    }

    @Override
    public Path<Vertex> findPath(Vertex origin, Collection<Vertex> destinations, RequirementContext context) {
        long start = System.currentTimeMillis();
        Queue<Node<Vertex>> frontier = new Frontier<>(Comparator.comparingDouble(n -> n.cost() + n.heuristic()));
        frontier.offer(new Node<>(origin, null, null, 0, 0));
        Map<Vertex, Node<Vertex>> explored = new HashMap<>();
        while (!frontier.isEmpty()) {
            Node<Vertex> current = frontier.poll();
            if (destinations.contains(current.vertex())) {
                Path<Vertex> res = backtrackAndBuildPath(current);
                log.debug("Path built in {}ms: {}", System.currentTimeMillis() - start, res);
                log.trace("Explored + frontier: {}", explored.size() + frontier.size());
                return res;
            }
            explored.put(current.vertex(), current);
            Stream<Edge<Vertex>> edges = edgesFunction.apply(graph, current.vertex()); // graph.edges(current.vertex());
            Iterator<Edge<Vertex>> edgeIterator = edges.iterator();
            while (edgeIterator.hasNext()) {
                Edge<Vertex> edge = edgeIterator.next();
                Requirement requirement = edge.requirement();
                if (!requirement.satisfy(context)) {
                    continue;
                }
                Node<Vertex> node = explored.get(edge.destination());
                float cost = current.cost() + edge.cost();
                if (node == null) {
                    node = new Node<>(edge.destination(), current, edge, cost, heuristic(destinations, edge.destination()));
                    if (!frontier.contains(node)) {
                        frontier.offer(node);
                    }
                }
                else if (cost < node.cost()) {
                    node.update(current, edge);
                }
            }
        }
        log.debug("Unable to build path, {}ms: {} -> {}", System.currentTimeMillis() - start, origin, destinations);
        return null;
    }

    public double heuristic(Collection<Vertex> destinations, Vertex currentVertex) {
        double min = Double.MAX_VALUE;
        for (Vertex destination : destinations) {
            double h = Math.sqrt(
                    Math.pow(destination.x() - currentVertex.x(), 2) +
                            Math.pow(destination.y() - currentVertex.y(), 2) +
                            Math.pow(destination.z() - currentVertex.z(), 2)
            );
            if (h < min) {
                min = h;
            }
        }
        return min;
    }
}
