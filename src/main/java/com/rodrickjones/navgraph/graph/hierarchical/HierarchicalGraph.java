package com.rodrickjones.navgraph.graph.hierarchical;

import com.rodrickjones.navgraph.graph.Graph;
import com.rodrickjones.navgraph.graph.MutableGraph;
import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HierarchicalGraph<G extends Graph<V>, V> extends Graph<G> {
    @Nullable G sub(@NonNull V vertex);

    @NotNull <F extends Graph<Vertex> & MutableGraph<Vertex>> F flatten();
}
