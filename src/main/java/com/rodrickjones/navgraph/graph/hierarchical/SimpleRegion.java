package com.rodrickjones.navgraph.graph.hierarchical;

import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;
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
    List<SimpleSubRegion> subRegions = new ArrayList<>();

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
    public @Nullable SimpleSubRegion subRegion(@NonNull Vertex vertex) {
        return subRegions.stream().filter(s -> s.contains(vertex)).findAny().orElse(null);
    }

    @Override
    public int subRegionCount() {
        return subRegions.size();
    }

    @Override
    public Stream<SubRegion> subRegions() {
        return subRegions.stream().map(Function.identity());
    }

    void addSubRegion(SimpleSubRegion subRegion) {
        subRegions.add(subRegion);
    }

    @Override
    public boolean contains(@NonNull Vertex vertex) {
        return subRegions.stream().anyMatch(s -> s.contains(vertex));
    }
}
