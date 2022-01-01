package com.rodrickjones.navgraph.graph;

import com.rodrickjones.navgraph.edge.Edge;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SimpleGraph<V> implements Graph<V>, MutableGraph<V> {
    private final Set<V> vertices;
    private final Map<V, Collection<Edge<V>>> edges;

    public SimpleGraph() {
        this(3000000, 12000000);
    }

    public SimpleGraph(int initialVertexCapacity, int initialEdgeCapacity) {
        vertices = new LinkedHashSet<>(initialVertexCapacity);
        edges = new HashMap<>(initialEdgeCapacity);
    }

    public SimpleGraph(@NonNull Collection<V> vertices, @NonNull Collection<Edge<V>> edges) {
        this(vertices.size(), vertices.size());
        addVertices(vertices.stream());
        addEdges(edges.stream());
    }

    // TODO revisit concurrency
    public synchronized void compact() {
        List<V> verticesToRemove = vertices.stream().filter(v -> !edges.containsKey(v)).collect(Collectors.toList());
        for (V vertex : verticesToRemove) {
            vertices.remove(vertex);
            edges.remove(vertex);
        }
        int removedEdges = 0;
        for (Collection<Edge<V>> edgeCollection : edges.values()) {
            List<Edge<V>> toRemove = edgeCollection.stream().filter(edge -> !vertices.contains(edge.destination())).collect(Collectors.toList());
            removedEdges += toRemove.size();
            edgeCollection.removeAll(toRemove);
            if (edgeCollection instanceof ArrayList<?>) {
                ((ArrayList<Edge<V>>) edgeCollection).trimToSize();
            }
        }
        edges.values().forEach(e1 -> e1.removeIf(e -> !vertices.contains(e.destination())));
        log.info(verticesToRemove.size() + " orphaned vertices removed");
        log.info(removedEdges + " invalid edges removed");
    }

    @Override
    public long vertexCount() {
        return vertices.size();
    }

    @Override
    public boolean containsVertex(@NonNull V vertex) {
        return vertices.contains(vertex);
    }

    @Override
    public @NotNull Stream<V> vertices() {
        return vertices.stream();
    }

    @Override
    public void addVertices(@NonNull Stream<V> vertices) {
        vertices.forEachOrdered(this::addVertex);
    }

    @Override
    public void addVertex(@NonNull V vertex) {
        vertices.add(vertex);
    }

    @Override
    public @NotNull Stream<Edge<V>> edges() {
        return edges.values().stream().flatMap(Collection::stream);
    }

    @Override
    public @NotNull Stream<Edge<V>> edges(@NonNull V vertex) {
        Collection<Edge<V>> value = edges.get(vertex);
        if (value == null) {
            return Stream.empty();
        }
        return value.stream();
    }

    @Override
    public void addEdges(@NonNull Stream<Edge<V>> edges) {
        edges.forEachOrdered(this::addEdge);
    }

    @Override
    public void addEdge(@NonNull Edge<V> edge) {
        V origin = edge.origin();
        Collection<Edge<V>> edges = this.edges.computeIfAbsent(origin, k -> new LinkedHashSet<>(4));
        Optional<Edge<V>> conflict;
        if (edges.contains(edge)) {
            throw new IllegalStateException(edge + " already exists for " + origin);
        } else if ((conflict = edges.stream().filter(e -> e.destination().equals(edge.destination())).findAny()).isPresent()) {
            throw new IllegalStateException("Conflicting edge exists for " + origin + ", existing=" + conflict.get() + ", new=" + edge);
        }
        edges.add(edge);
    }

    @Override
    public long edgeCount() {
        return edges.values().stream().mapToLong(Collection::size).sum();
    }

    @Override
    public boolean containsEdge(@NonNull Edge<V> edge) {
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
