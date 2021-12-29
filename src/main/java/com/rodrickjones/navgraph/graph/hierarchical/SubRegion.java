package com.rodrickjones.navgraph.graph.hierarchical;

import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;

import java.util.stream.Stream;

public interface SubRegion {
    Stream<Vertex> vertices();

    int vertexCount();

    boolean contains(@NonNull Vertex vertex);
}
