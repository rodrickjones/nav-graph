package com.rodrickjones.navgraph.graph;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SimpleGraph implements MutableGraph {
    private final Set<Vertex> vertices;
    private final Map<Vertex, Collection<Edge>> edges;

    public SimpleGraph() {
        this(3000000, 12000000);
    }

    public SimpleGraph(int initialVertexCapacity, int initialEdgeCapacity) {
        vertices = new LinkedHashSet<>(initialVertexCapacity);
        edges = new HashMap<>(initialEdgeCapacity);
    }

    public SimpleGraph(Collection<Vertex> vertices, Collection<Edge> edges) {
        this(vertices.size(), vertices.size());
        addVertices(vertices.stream());
        addEdges(edges.stream());
    }

    public void cleanUp() {
        List<Vertex> verticesToRemove = vertices.stream().filter(v -> !edges.containsKey(v)).collect(Collectors.toList());
        for (Vertex vertex : verticesToRemove) {
            vertices.remove(vertex);
            edges.remove(vertex);
        }
        int removedEdges = 0;
        for (Collection<Edge> edgeCollection : edges.values()) {
            List<Edge> toRemove = edgeCollection.stream().filter(edge -> !vertices.contains(edge.destination())).collect(Collectors.toList());
            removedEdges += toRemove.size();
            edgeCollection.removeAll(toRemove);
            if (edgeCollection instanceof ArrayList<?>) {
                ((ArrayList<Edge>) edgeCollection).trimToSize();
            }
        }
        edges.values().forEach(e1 -> e1.removeIf(e -> !vertices.contains(e.destination())));
        log.info(verticesToRemove.size() + " orphaned vertices removed");
        log.info(removedEdges + " invalid edges removed");
    }

    @Override
    public int vertexCount() {
        return vertices.size();
    }

    @Override
    public boolean containsVertex(@NonNull Vertex vertex) {
        return vertices.contains(vertex);
    }

    @Override
    public Stream<Vertex> vertices() {
        return vertices.stream();
    }

    @Override
    public void addVertices(@NonNull Stream<Vertex> vertices) {
        vertices.forEachOrdered(this::addVertex);
    }

    @Override
    public void addVertex(@NonNull Vertex vertex) {
        if (!vertices.add(vertex)) {
            throw new IllegalArgumentException(vertex + " already exists in Graph");
        }
    }

    @Override
    public Stream<Edge> edges() {
        return edges.values().stream().flatMap(Collection::stream);
    }

    @Override
    public Stream<Edge> edges(@NonNull Vertex vertex) {
        Collection<Edge> value = edges.get(vertex);
        if (value == null) {
            return Stream.empty();
        }
        return value.stream();
    }

    @Override
    public void addEdges(@NonNull Stream<Edge> edges) {
        edges.forEachOrdered(this::addEdge);
    }

    @Override
    public void addEdge(@NonNull Edge edge) {
        Vertex vertex = edge.origin();
        Collection<Edge> edges = this.edges.computeIfAbsent(vertex, k -> new LinkedHashSet<>(4));
        Optional<Edge> conflict;
        if (edges.contains(edge)) {
            throw new IllegalStateException(edge + " already exists for " + vertex);
        } else if ((conflict = edges.stream().filter(e -> e.destination().equals(edge.destination())).findAny()).isPresent()) {
            throw new IllegalStateException("Conflicting edge exists for " + vertex + ", existing=" + conflict.get() + ", new=" + edge);
        }
        edges.add(edge);
    }

    @Override
    public int edgeCount() {
        return edges.values().stream().mapToInt(Collection::size).sum();
    }

    @Override
    public boolean containsEdge(@NonNull Edge edge) {
        return edges.values().stream().anyMatch(value -> value.contains(edge));
    }

    @Override
    public String toString() {
        return "SimpleGraph{" +
                "vertices=" + vertexCount() +
                ", edges=" + edgeCount() +
                '}';
    }
}
