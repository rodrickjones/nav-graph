package com.rodrickjones.navgraph.graph;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;

import java.util.stream.Stream;

public interface MutableGraph extends Graph {
    void addVertices(@NonNull Stream<Vertex> vertices);

    void addVertex(@NonNull Vertex vertex);

    void addEdges(@NonNull Stream<Edge<Vertex>> edges);

    void addEdge(@NonNull Edge<Vertex> edge);
}
