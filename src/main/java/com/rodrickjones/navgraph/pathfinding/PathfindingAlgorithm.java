package com.rodrickjones.navgraph.pathfinding;

import com.rodrickjones.navgraph.Graph;
import com.rodrickjones.navgraph.edges.Edge;
import com.rodrickjones.navgraph.vertices.Vertex;
import com.rodrickjones.navgraph.requirements.RequirementContext;

import java.util.*;

public abstract class PathfindingAlgorithm<T extends Graph> {
    protected final T graph;

    public PathfindingAlgorithm(T graph) {
        this.graph = graph;
    }

    public abstract Path findPath(Vertex origin, Collection<Vertex> destinations, RequirementContext context);

    protected Path backtrackAndBuildPath(Node current) {
        ArrayList<Vertex> vertices = new ArrayList<>();
        ArrayList<Edge> edges = new ArrayList<>();

        while (current.getParent() != null) {
            vertices.add(current.getVertex());
            Edge edge = current.getEdge();
            edges.add(edge);
            current = current.getParent();
        }
        vertices.add(current.getVertex());

        vertices.trimToSize();
        edges.trimToSize();

        Collections.reverse(vertices);
        Collections.reverse(edges);
        return new Path(vertices, edges);
    }

    static class Node {
        private final Vertex vertex;
        private final double heuristic;
        private Edge edge;
        private Node parent;
        private double cost;

        Node(Vertex vertex, Node parent, Edge edge, double cost, double heuristic) {
            this.vertex = vertex;
            this.parent = parent;
            this.edge = edge;
            this.cost = cost;
            this.heuristic = heuristic;
        }

        public Vertex getVertex() {
            return vertex;
        }

        public Edge getEdge() {
            return edge;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent, Edge edge) {
            this.parent = parent;
            this.edge = edge;
            this.cost = parent.getCost() + edge.getCost();
        }

        public double getCost() {
            return cost;
        }

        public double getHeuristic() {
            return heuristic;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(vertex, node.vertex);
        }

        @Override
        public int hashCode() {
            return Objects.hash(vertex);
        }
    }
}
