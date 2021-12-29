package com.rodrickjones.navgraph.graph.hierarchical;

import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class SimpleSubRegion implements SubRegion {
    private final String id;
    private final Set<Vertex> vertices = new LinkedHashSet<>();

    public SimpleSubRegion(String id) {
        this.id = id;
    }

    void addVertex(Vertex vertex) {
        vertices.add(vertex);
    }

    @Override
    public int vertexCount() {
        return vertices.size();
    }

    @Override
    public boolean contains(@NonNull Vertex vertex) {
        return vertices.contains(vertex);
    }

    @Override
    public Stream<Vertex> vertices() {
        return vertices.stream();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleSubRegion subRegion = (SimpleSubRegion) o;
        return id.equals(subRegion.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
