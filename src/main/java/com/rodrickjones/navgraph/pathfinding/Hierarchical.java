package com.rodrickjones.navgraph.pathfinding;

import com.rodrickjones.navgraph.graph.Graph;
import com.rodrickjones.navgraph.graph.SimpleGraph;
import com.rodrickjones.navgraph.graph.hierarchical.SimpleHierarchicalGraph;
import com.rodrickjones.navgraph.graph.hierarchical.SubRegion;
import com.rodrickjones.navgraph.graph.hierarchical.SubRegionEdge;
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
        SubRegion originSubRegion = graph.subRegion(origin);
        Collection<SubRegion> destinationSubRegions = destinations.stream().map(graph::subRegion)
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (originSubRegion == null || destinationSubRegions.isEmpty()) {
            log.warn("Unable to find origin and destination sub regions: origin={}, origin SubRegion={}, destination={}, destination SubRegion={}", origin, originSubRegion, destinations, destinationSubRegions);
            log.trace("No path, unable to find both sub regions: {}ms", System.currentTimeMillis() - start);
            return null;
        }
        Queue<SubRegionNode> frontier = new Frontier<>(Comparator.comparingDouble(SubRegionNode::getCost));
        frontier.add(new SubRegionNode(originSubRegion, null, 0));
        Map<SubRegion, SubRegionNode> explored = new HashMap<>();
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
            Iterator<SubRegionEdge> subRegionEdgeIterator = graph.edges(current.getSubRegion()).iterator();
            while (subRegionEdgeIterator.hasNext()) {
                SubRegionEdge subRegionEdge = subRegionEdgeIterator.next();
                if (!subRegionEdge.canTraverse(context)) {
                    continue;
                }

                SubRegionNode node = explored.get(subRegionEdge.destination());
                double cost = current.getCost() + subRegionEdge.cost();
                if (node == null) {
                    node = new SubRegionNode(subRegionEdge.destination(), current, cost);
                    if (!frontier.contains(node)) {
                        frontier.add(node);
                    }
                } else if (cost < node.getCost()) {
                    node.setParent(current, subRegionEdge);
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

    private void addToGraph(SimpleGraph leanGraph, SubRegion subRegion) {
        subRegion.vertices().forEachOrdered(vertex -> {
            leanGraph.addVertex(vertex);
            leanGraph.addEdges(graph.edges(vertex));
        });
    }

    public static class SubRegionNode {
        final SubRegion subRegion;
        SubRegionNode parent;
        double cost;

        SubRegionNode(SubRegion subRegion, SubRegionNode parent, double cost) {
            this.subRegion = subRegion;
            this.parent = parent;
            this.cost = cost;
        }

        public SubRegion getSubRegion() {
            return subRegion;
        }

        public SubRegionNode getParent() {
            return parent;
        }

        public void setParent(SubRegionNode parent, SubRegionEdge edge) {
            this.parent = parent;
            this.cost = parent.getCost() + edge.cost();
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
