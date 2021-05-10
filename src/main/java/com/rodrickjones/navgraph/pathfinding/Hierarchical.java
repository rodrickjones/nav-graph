package com.rodrickjones.navgraph.pathfinding;

import com.rodrickjones.navgraph.Graph;
import com.rodrickjones.navgraph.HierarchicalGraph;
import com.rodrickjones.navgraph.SimpleGraph;
import com.rodrickjones.navgraph.requirements.RequirementContext;
import com.rodrickjones.navgraph.util.Frontier;
import com.rodrickjones.navgraph.vertices.Vertex;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Hierarchical extends PathfindingAlgorithm<HierarchicalGraph> {
    public Hierarchical(HierarchicalGraph graph) {
        super(graph);
    }

    @Override
    public Path findPath(Vertex origin, Collection<Vertex> destinations, RequirementContext context) {
        long start = System.currentTimeMillis();
        //find path of sections
        HierarchicalGraph.SubRegion originSubRegion = graph.getSubRegion(origin);
        Collection<HierarchicalGraph.SubRegion> destinationSubRegions = destinations.stream().map(graph::getSubRegion)
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (originSubRegion == null || destinationSubRegions.isEmpty()) {
            log.warn("Unable to find origin and destination sub regions: origin={}, origin SubRegion={}, destination={}, destination SubRegion={}", origin, originSubRegion, destinations, destinationSubRegions);
            log.trace("No path, unable to find both sub regions: {}ms", System.currentTimeMillis() - start);
            return null;
        }
        Queue<SubRegionNode> frontier = new Frontier<>(Comparator.comparingDouble(SubRegionNode::getCost));
        frontier.add(new SubRegionNode(originSubRegion, null, 0));
        Map<HierarchicalGraph.SubRegion, SubRegionNode> explored = new HashMap<>();
        SimpleGraph leanGraph = null;
        while (!frontier.isEmpty()) {
            SubRegionNode current = frontier.poll();
            if (destinationSubRegions.contains(current.getSubRegion())) {
                leanGraph = new SimpleGraph();
                addToGraph(leanGraph, current.getSubRegion());
                while ((current = current.getParent()) != null) {
                    addToGraph(leanGraph, current.getSubRegion());
                }
                break;
            }
            explored.put(current.getSubRegion(), current);
            Collection<HierarchicalGraph.SubRegionEdge> subRegionEdges = graph.getEdges(current.getSubRegion());
            if (subRegionEdges != null) {
                for (HierarchicalGraph.SubRegionEdge edge : subRegionEdges) {
                    if (!edge.canTraverse(context)) {
                        continue;
                    }

                    SubRegionNode node = explored.get(edge.getDestination());
                    double cost = current.getCost() + edge.getCost();
                    if (node == null) {
                        node = new SubRegionNode(edge.getDestination(), current, cost);
                        if (!frontier.contains(node)) {
                            frontier.add(node);
                        }
                    } else if (cost < node.getCost()) {
                        node.setParent(current, edge);
                    }
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
                //We should never see this, unless the SubRegion linking has issues
                log.error("No path, unable to find path: {}ms", System.currentTimeMillis() - start);
            }
            return path;
        } else {
            log.warn("Unable to find lean graph for subregions: origin={}, destinations={}", originSubRegion, destinationSubRegions);
            log.trace("No path, unable to find lean graph: {}ms", System.currentTimeMillis() - start);
            return null;
        }
    }

    private void addToGraph(Graph leanGraph, HierarchicalGraph.SubRegion subRegion) {
        for (Vertex v : subRegion.getVertices()) {
            leanGraph.addVertex(v);
            leanGraph.addEdges(graph.getEdges(v));
        }
    }

    public static class SubRegionNode {
        final HierarchicalGraph.SubRegion subRegion;
        SubRegionNode parent;
        double cost;

        SubRegionNode(HierarchicalGraph.SubRegion subRegion, SubRegionNode parent, double cost) {
            this.subRegion = subRegion;
            this.parent = parent;
            this.cost = cost;
        }

        public HierarchicalGraph.SubRegion getSubRegion() {
            return subRegion;
        }

        public SubRegionNode getParent() {
            return parent;
        }

        public void setParent(SubRegionNode parent, HierarchicalGraph.SubRegionEdge edge) {
            this.parent = parent;
            this.cost = parent.getCost() + edge.getCost();
        }

        public double getCost() {
            return cost;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SubRegionNode node = (SubRegionNode) o;
            return Objects.equals(subRegion, node.subRegion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(subRegion);
        }
    }

}
