package com.rodrickjones.navgraph;

import com.rodrickjones.navgraph.edges.Edge;
import com.rodrickjones.navgraph.vertices.Vertex;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SimpleGraph implements Graph {
    private final Map<Integer, Vertex> vertices;
    private final Map<Vertex, Collection<Edge>> edges;

    public SimpleGraph() {
        this(3000000, 12000000);
    }

    public SimpleGraph(int initialVertexCapacity, int initialEdgeCapacity) {
        vertices = new HashMap<>(initialVertexCapacity);
        edges = new HashMap<>(initialEdgeCapacity);
    }

    public SimpleGraph(Collection<Vertex> vertices, Collection<Edge> edges) {
        this(vertices.size(), vertices.size());
        addVertices(vertices);
        addEdges(edges);
    }

    public void cleanUp() {
        List<Vertex> verticesToRemove = vertices.values().stream().filter(v -> !edges.containsKey(v)).collect(Collectors.toList());
        for (Vertex vertex : verticesToRemove) {
            vertices.remove(vertex.hashCode());
            edges.remove(vertex);
        }
        int removedEdges = 0;
        for (Collection<Edge> edgeCollection : edges.values()) {
            List<Edge> toRemove = edgeCollection.stream().filter(edge -> !vertices.containsKey(edge.getDestination().hashCode())).collect(Collectors.toList());
            removedEdges += toRemove.size();
            edgeCollection.removeAll(toRemove);
            if (edgeCollection instanceof ArrayList) {
                ((ArrayList<Edge>) edgeCollection).trimToSize();
            }
        }
        edges.values().forEach(e1 -> e1.removeIf(e -> !vertices.containsKey(e.getDestination().hashCode())));
        log.info(verticesToRemove.size() + " orphaned vertices removed");
        log.info(removedEdges + " invalid edges removed");
    }

    @Override
    public int getVertexCount() {
        return vertices.size();
    }

    @Override
    public Collection<Vertex> getVertices() {
        return vertices.values();
    }

    @Override
    public Vertex getVertex(int x, int y, int z) {
        return vertices.get(Vertex.hashCode(x, y, z));
    }

    @Override
    public void addVertices(Collection<Vertex> vertices) {
        //TODO Optimize this by making it one addAll call instead
        for (Vertex vertex : vertices) {
            addVertex(vertex);
        }
    }

    @Override
    public void addVertex(Vertex vertex) {
        if (vertex == null) {
            throw new IllegalArgumentException("Cannot add a null Vertex to a " + getClass().getSimpleName());
        }
        if (vertices.put(vertex.hashCode(), vertex) != null) {
            throw new IllegalStateException(vertex + " already exists in Graph");
        }
    }

    @Override
    public Collection<Edge> getEdges() {
        return edges.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        //Using Collectors.toSet() increases zip size by almost 50%
    }

    @Override
    public Collection<Edge> getEdges(Vertex vertex) {
        return edges.get(vertex);
    }

    @Override
    public void addEdges(Collection<Edge> edges) {
        //TODO Optimize this by making it one addAll call instead
        for (Edge edge : edges) {
            addEdge(edge);
        }
    }

    @Override
    public void addEdge(Edge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("Cannot add a null Edge to a " + getClass().getSimpleName());
        }
        Vertex vertex = edge.getOrigin();
        Collection<Edge> edges = this.edges.computeIfAbsent(vertex, k -> new LinkedHashSet<>(4));
        Optional<Edge> conflict;
        if (edges.contains(edge)) {
            throw new IllegalStateException(edge + " already exists for " + vertex);
        } else if ((conflict = edges.stream().filter(e -> e.getDestination().equals(edge.getDestination())).findAny()).isPresent()) {
            throw new IllegalStateException("Conflicting edge exists for " + vertex + ", existing=" + conflict.get() + ", new=" + edge);
        }
        edges.add(edge);
    }

    @Override
    public int getEdgeCount() {
        return edges.values().stream().mapToInt(Collection::size).sum();
    }

    @Override
    public String toString() {
        return "SimpleGraph{" +
                "vertices=" + getVertexCount() +
                ", edges=" + getEdgeCount() +
                '}';
    }
}
