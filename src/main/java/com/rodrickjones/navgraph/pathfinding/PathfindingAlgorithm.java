package com.rodrickjones.navgraph.pathfinding;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.graph.Graph;
import com.rodrickjones.navgraph.requirement.RequirementContext;
import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public abstract class PathfindingAlgorithm<G extends Graph<?>, V> {
    protected final G graph;

    public PathfindingAlgorithm(@NonNull G graph) {
        this.graph = graph;
    }

    public abstract Path<V> findPath(V origin, Collection<V> destinations, RequirementContext context);

    protected Path<V> backtrackAndBuildPath(Node<V> current) {
        ArrayList<V> vertices = new ArrayList<>();
        ArrayList<Edge<V>> edges = new ArrayList<>();

        while (current.parent() != null) {
            vertices.add(current.vertex());
            Edge<V> edge = current.edge();
            edges.add(edge);
            current = current.parent();
        }
        vertices.add(current.vertex());

        vertices.trimToSize();
        edges.trimToSize();

        Collections.reverse(vertices);
        Collections.reverse(edges);
        return new Path<>(vertices, edges);
    }

}
