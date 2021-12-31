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
    List<Graph> subGraphs = new ArrayList<>();

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
    public @Nullable Graph subGraph(@NonNull Vertex vertex) {
        return subGraphs.stream().filter(s -> s.containsVertex(vertex)).findAny().orElse(null);
    }

    @Override
    public int subGraphCount() {
        return subGraphs.size();
    }

    @Override
    public @NotNull Stream<Graph> subGraphs() {
        return subGraphs.stream().map(Function.identity());
    }

    void addSubGraph(Graph subGraph) {
        subGraphs.add(subGraph);
    }

    @Override
    public boolean contains(@NonNull Vertex vertex) {
        return subGraphs.stream().anyMatch(s -> s.containsVertex(vertex));
    }
}
