package com.rodrickjones.navgraph.pathfinding;

import com.rodrickjones.navgraph.graph.Graph;
import com.rodrickjones.navgraph.vertex.Vertex;

import java.util.Collection;

public abstract class Dijkstras<G extends Graph<?>> extends AStar<G> {

    public Dijkstras(G graph) {
        super(graph);
    }

    @Override
    public double heuristic(Collection<Vertex> destination, Vertex currentVertex) {
        return 0;
    }
}
