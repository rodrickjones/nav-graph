package com.rodrickjones.navgraph.graph;

import com.rodrickjones.navgraph.edge.Edge;
import lombok.NonNull;

import java.util.stream.Stream;

public interface MutableGraph<V> {
    void addVertices(@NonNull Stream<V> vertices);

    void addVertex(@NonNull V vertex);

    void addEdges(@NonNull Stream<Edge<V>> edges);

    void addEdge(@NonNull Edge<V> edge);
}
