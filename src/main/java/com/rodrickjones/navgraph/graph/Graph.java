package com.rodrickjones.navgraph.graph;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;

import java.util.stream.Stream;


public interface Graph {

    Stream<Vertex> vertices();

    int vertexCount();

    boolean containsVertex(@NonNull Vertex vertex);

    Stream<Edge> edges();

    Stream<Edge> edges(@NonNull Vertex vertex);

    int edgeCount();

    boolean containsEdge(@NonNull Edge edge);
}
