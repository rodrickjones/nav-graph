package com.rodrickjones.navgraph.graph;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;


public interface Graph {

    @NotNull Stream<Vertex> vertices();

    int vertexCount();

    boolean containsVertex(@NonNull Vertex vertex);

    @NotNull Stream<Edge<Vertex>> edges();

    @NotNull Stream<Edge<Vertex>> edges(@NonNull Vertex vertex);

    int edgeCount();

    boolean containsEdge(Edge<Vertex> edge);
}
