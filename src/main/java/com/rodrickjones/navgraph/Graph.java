package com.rodrickjones.navgraph;

import com.rodrickjones.navgraph.edges.Edge;
import com.rodrickjones.navgraph.vertices.Vertex;

import java.util.Collection;
import java.util.LinkedHashSet;


public interface Graph {
    public static Graph combine(Graph a, Graph b) {
        Collection<Vertex> vertices = new LinkedHashSet<>();
        vertices.addAll(a.getVertices());
        vertices.addAll(b.getVertices());

        Collection<Edge> edges = new LinkedHashSet<>();
        edges.addAll(a.getEdges());
        edges.addAll(b.getEdges());

        return new SimpleGraph(vertices, edges);
    }

    Collection<Vertex> getVertices();

    Vertex getVertex(int x, int y, int z);

    void addVertex(Vertex vertex);

    void addVertices(Collection<Vertex> vertices);

    int getVertexCount();

    Collection<Edge> getEdges();

    Collection<Edge> getEdges(Vertex vertex);

    void addEdge(Edge edge);

    void addEdges(Collection<Edge> edges);

    int getEdgeCount();
}
