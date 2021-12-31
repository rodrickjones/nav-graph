package com.rodrickjones.navgraph.graph.hierarchical;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.graph.Graph;
import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public interface HierarchicalGraph {
    @NonNull Stream<Region> regions();

    @Deprecated
    @Nullable Graph subGraph(@NonNull Vertex vertex);

    @Deprecated
    @NotNull Stream<Edge<Graph>> edges(@NonNull Graph subGraph);
}
