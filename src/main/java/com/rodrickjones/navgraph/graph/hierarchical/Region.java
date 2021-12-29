package com.rodrickjones.navgraph.graph.hierarchical;

import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public interface Region {

    int baseX();

    int baseY();

    Stream<SubRegion> subRegions();

    @Nullable SubRegion subRegion(@NonNull Vertex vertex);

    int subRegionCount();

    boolean contains(@NonNull Vertex vertex);
}
