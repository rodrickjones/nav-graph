package com.rodrickjones.navgraph.graph.hierarchical;

import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public interface HierarchicalGraph {
    @NonNull Stream<Region> regions();

    @Nullable SubRegion subRegion(@NonNull Vertex vertex);

    Stream<SubRegionEdge> edges(@NonNull SubRegion subRegion);
}
