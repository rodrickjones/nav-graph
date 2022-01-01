package com.rodrickjones.navgraph.graph.hierarchical;

import com.rodrickjones.navgraph.graph.Graph;
import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public interface Region {

    int baseX();

    int baseY();

    @NotNull Stream<Graph<Vertex>> graphs();

    @Nullable Graph<Vertex> graph(@NonNull Vertex vertex);

    long graphCount();

    boolean contains(@NonNull Vertex vertex);
}
