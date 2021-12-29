package com.rodrickjones.navgraph.pathfinding;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.vertex.Vertex;

import java.util.Objects;

class Node {
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

    public Vertex vertex() {
        return vertex;
    }

    public Edge edge() {
        return edge;
    }

    public Node parent() {
        return parent;
    }

    public void update(Node parent, Edge edge) {
        this.parent = parent;
        this.edge = edge;
        this.cost = parent.cost() + edge.cost();
    }

    public double cost() {
        return cost;
    }

    public double heuristic() {
        return heuristic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Node node = (Node) o;
        return Objects.equals(vertex, node.vertex);
    }

    // FIXME
    @Override
    public int hashCode() {
        return Objects.hash(vertex);
    }
}
