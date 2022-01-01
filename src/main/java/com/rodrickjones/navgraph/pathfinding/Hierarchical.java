package com.rodrickjones.navgraph.pathfinding;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.graph.Graph;
import com.rodrickjones.navgraph.graph.SimpleGraph;
import com.rodrickjones.navgraph.graph.hierarchical.HierarchicalGraph;
import com.rodrickjones.navgraph.requirement.RequirementContext;
import com.rodrickjones.navgraph.util.Frontier;
import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class Hierarchical extends PathfindingAlgorithm<HierarchicalGraph<Graph<Vertex>, Vertex>, Vertex> {
    public Hierarchical(HierarchicalGraph<Graph<Vertex>, Vertex> graph) {
        super(graph);
    }

    @Override
    public Path<Vertex> findPath(Vertex origin, Collection<Vertex> destinations, RequirementContext context) {
        long start = System.currentTimeMillis();
        //find path of sections
        Graph<Vertex> originSubGraph = graph.sub(origin);
        Collection<Graph<Vertex>> destinationSubGraphs = destinations.stream().map(graph::sub)
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (originSubGraph == null || destinationSubGraphs.isEmpty()) {
            log.warn("Unable to find origin and destination sub regions: origin={}, originSubGraph={}, destination={}, destinationSubGraph={}", origin, originSubGraph, destinations, destinationSubGraphs);
            log.trace("No path, unable to find both sub regions: {}ms", System.currentTimeMillis() - start);
            return null;
        }
        Queue<SubGraphNode> frontier = new Frontier<>(Comparator.comparingDouble(SubGraphNode::cost));
        frontier.add(new SubGraphNode(originSubGraph, null, 0));
        Map<Graph<Vertex>, SubGraphNode> explored = new HashMap<>();
        SimpleGraph<Graph<Vertex>> leanGraph = null;
        while (!frontier.isEmpty()) {
            SubGraphNode current = frontier.poll();
            if (destinationSubGraphs.contains(current.subGraph())) {
                leanGraph = new SimpleGraph<>();
                do {
                    leanGraph.addVertex(current.subGraph());
                    leanGraph.addEdges(graph.edges(current.subGraph()));
                } while ((current = current.parent()) != null);
                break;
            }
            explored.put(current.subGraph(), current);
            Iterator<Edge<Graph<Vertex>>> subGraphEdgeIterator = graph.edges(current.subGraph()).iterator();
            while (subGraphEdgeIterator.hasNext()) {
                Edge<Graph<Vertex>> subGraphEdge = subGraphEdgeIterator.next();
                if (!subGraphEdge.requirement().satisfy(context)) {
                    continue;
                }

                SubGraphNode node = explored.get(subGraphEdge.destination());
                float cost = current.cost() + subGraphEdge.cost();
                if (node == null) {
                    node = new SubGraphNode(subGraphEdge.destination(), current, cost);
                    if (!frontier.contains(node)) {
                        frontier.add(node);
                    }
                }
                else if (cost < node.cost()) {
                    node.parent(current, subGraphEdge);
                }
            }
        }
        if (leanGraph != null) {
            log.trace("Lean graph found in {}ms: {}", System.currentTimeMillis() - start, leanGraph);
            PathfindingAlgorithm<Graph<Graph<Vertex>>, Vertex> pathfindingAlgorithm = new AStar<Graph<Graph<Vertex>>>(leanGraph) {
                @Override
                protected @NotNull Stream<Edge<Vertex>> edges(@NonNull Vertex vertex) {
                    return graph.vertices()
                            .filter(subGraph -> subGraph.containsVertex(vertex))
                            .findFirst()
                            .map(subGraph -> subGraph.edges(vertex))
                            .orElseGet(Stream::empty);
                }
            };
            Path<Vertex> path = pathfindingAlgorithm.findPath(origin, destinations, context);
            if (path != null) {
                log.trace("Path created: {}ms", System.currentTimeMillis() - start);
            }
            else {
                //We should never see this, unless the SubGraph linking has issues
                log.error("No path, unable to find path: {}ms", System.currentTimeMillis() - start);
            }
            return path;
        }
        else {
            log.warn("Unable to find lean graph for subGraph: origin={}, destinations={}", originSubGraph, destinationSubGraphs);
            log.trace("No path, unable to find lean graph: {}ms", System.currentTimeMillis() - start);
            return null;
        }
    }

    static class SubGraphNode {
        final Graph<Vertex> subGraph;
        SubGraphNode parent;
        float cost;

        SubGraphNode(Graph<Vertex> subGraph, SubGraphNode parent, float cost) {
            this.subGraph = subGraph;
            this.parent = parent;
            this.cost = cost;
        }

        public Graph<Vertex> subGraph() {
            return subGraph;
        }

        public SubGraphNode parent() {
            return parent;
        }

        public void parent(SubGraphNode parent, Edge<Graph<Vertex>> edge) {
            this.parent = parent;
            this.cost = parent.cost() + edge.cost();
        }

        public float cost() {
            return cost;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SubGraphNode node = (SubGraphNode) o;
            return Objects.equals(subGraph, node.subGraph);
        }

        @Override
        public int hashCode() {
            return Objects.hash(subGraph);
        }
    }
}
