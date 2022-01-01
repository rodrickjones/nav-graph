package com.rodrickjones.navgraph.graph.hierarchical;

import com.rodrickjones.navgraph.graph.Graph;
import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

class SimpleRegion implements Region {
    final static int WIDTH = 64;
    final static int HEIGHT = 64;
    private final long id;
    private final int baseX;
    private final int baseY;
    List<Graph<Vertex>> graphs = new ArrayList<>();

    public SimpleRegion(long id) {
        this.id = id;
        this.baseX = (int) (id >> 8 & 0xFF) << 6;
        this.baseY = (int) (id & 0xFF) << 6;
    }

    public long id() {
        return id;
    }

    @Override
    public int baseX() {
        return baseX;
    }

    @Override
    public int baseY() {
        return baseY;
    }

    @Override
    public @NotNull Stream<Graph<Vertex>> graphs() {
        return graphs.stream();
    }

    @Override
    public @Nullable Graph<Vertex> graph(@NonNull Vertex vertex) {
        return graphs.stream().filter(s -> s.containsVertex(vertex)).findAny().orElse(null);
    }

    @Override
    public long graphCount() {
        return graphs.size();
    }

    void addSubGraph(Graph<Vertex> subGraph) {
        graphs.add(subGraph);
    }

    @Override
    public boolean contains(@NonNull Vertex vertex) {
        return graphs.stream().anyMatch(s -> s.containsVertex(vertex));
    }
}
