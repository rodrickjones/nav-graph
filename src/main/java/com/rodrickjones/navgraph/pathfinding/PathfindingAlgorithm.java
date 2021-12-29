package com.rodrickjones.navgraph.pathfinding;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.graph.Graph;
import com.rodrickjones.navgraph.requirement.RequirementContext;
import com.rodrickjones.navgraph.vertex.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public abstract class PathfindingAlgorithm<T extends Graph> {
    protected final T graph;

    public PathfindingAlgorithm(T graph) {
        this.graph = graph;
    }

    public abstract Path findPath(Vertex origin, Collection<Vertex> destinations, RequirementContext context);

    protected Path backtrackAndBuildPath(Node current) {
        ArrayList<Vertex> vertices = new ArrayList<>();
        ArrayList<Edge> edges = new ArrayList<>();

        while (current.parent() != null) {
            vertices.add(current.vertex());
            Edge edge = current.edge();
            edges.add(edge);
            current = current.parent();
        }
        vertices.add(current.vertex());

        vertices.trimToSize();
        edges.trimToSize();

        Collections.reverse(vertices);
        Collections.reverse(edges);
        return new Path(vertices, edges);
    }

}
