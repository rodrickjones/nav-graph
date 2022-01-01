package com.rodrickjones.navgraph.graph;

import com.rodrickjones.navgraph.edge.Edge;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;


public interface Graph<V> {

    @NotNull Stream<V> vertices();

    long vertexCount();

    boolean containsVertex(@NonNull V vertex);

    @NotNull Stream<Edge<V>> edges();

    @NotNull Stream<Edge<V>> edges(@NonNull V vertex);

    long edgeCount();

    boolean containsEdge(@NonNull Edge<V> edge);
}
