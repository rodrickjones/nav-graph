package com.rodrickjones.navgraph.pathfinding;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.graph.Graph;
import com.rodrickjones.navgraph.graph.SimpleGraph;
import com.rodrickjones.navgraph.graph.hierarchical.SimpleHierarchicalGraph;
import com.rodrickjones.navgraph.requirement.RequirementContext;
import com.rodrickjones.navgraph.util.Frontier;
import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Hierarchical extends PathfindingAlgorithm<SimpleHierarchicalGraph> {
    public Hierarchical(SimpleHierarchicalGraph graph) {
        super(graph);
    }

    @Override
    public Path findPath(Vertex origin, Collection<Vertex> destinations, RequirementContext context) {
        long start = System.currentTimeMillis();
        //find path of sections
        Graph originSubGraph = graph.subGraph(origin);
        Collection<Graph> destinationSubGraphs = destinations.stream().map(graph::subGraph)
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (originSubGraph == null || destinationSubGraphs.isEmpty()) {
            log.warn("Unable to find origin and destination sub regions: origin={}, originSubGraph={}, destination={}, destinationSubGraph={}", origin, originSubGraph, destinations, destinationSubGraphs);
            log.trace("No path, unable to find both sub regions: {}ms", System.currentTimeMillis() - start);
            return null;
        }
        Queue<SubGraphNode> frontier = new Frontier<>(Comparator.comparingDouble(SubGraphNode::cost));
        frontier.add(new SubGraphNode(originSubGraph, null, 0));
        Map<Graph, SubGraphNode> explored = new HashMap<>();
        SimpleGraph leanGraph = null;
        while (!frontier.isEmpty()) {
            SubGraphNode current = frontier.poll();
            if (destinationSubGraphs.contains(current.getSubGraph())) {
                leanGraph = new SimpleGraph();
                addToGraph(leanGraph, current.getSubGraph());
                while ((current = current.getParent()) != null) {
                    addToGraph(leanGraph, current.getSubGraph());
                }
                break;
            }
            explored.put(current.getSubGraph(), current);
            Iterator<Edge<Graph>> subGraphEdgeIterator = graph.edges(current.getSubGraph()).iterator();
            while (subGraphEdgeIterator.hasNext()) {
                Edge<Graph> subGraphEdge = subGraphEdgeIterator.next();
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
                } else if (cost < node.cost()) {
                    node.setParent(current, subGraphEdge);
                }
            }
        }
        if (leanGraph != null) {
            log.trace("Lean graph found in {}ms: {}", System.currentTimeMillis() - start, leanGraph);
            PathfindingAlgorithm<Graph> pathfindingAlgorithm = new AStar(leanGraph);
            Path path = pathfindingAlgorithm.findPath(origin, destinations, context);
            if (path != null) {
                log.trace("Path created: {}ms", System.currentTimeMillis() - start);
            } else {
                //We should never see this, unless the SubGraph linking has issues
                log.error("No path, unable to find path: {}ms", System.currentTimeMillis() - start);
            }
            return path;
        } else {
            log.warn("Unable to find lean graph for subGraph: origin={}, destinations={}", originSubGraph, destinationSubGraphs);
            log.trace("No path, unable to find lean graph: {}ms", System.currentTimeMillis() - start);
            return null;
        }
    }

    // TODO revisit
    private void addToGraph(SimpleGraph leanGraph, Graph subGraph) {
        subGraph.vertices().forEachOrdered(vertex -> {
            leanGraph.addVertex(vertex);
            leanGraph.addEdges(graph.edges(vertex));
        });
    }

    static class SubGraphNode {
        final Graph subGraph;
        SubGraphNode parent;
        float cost;

        SubGraphNode(Graph subGraph, SubGraphNode parent, float cost) {
            this.subGraph = subGraph;
            this.parent = parent;
            this.cost = cost;
        }

        public Graph getSubGraph() {
            return subGraph;
        }

        public SubGraphNode getParent() {
            return parent;
        }

        public void setParent(SubGraphNode parent, Edge<Graph> edge) {
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
