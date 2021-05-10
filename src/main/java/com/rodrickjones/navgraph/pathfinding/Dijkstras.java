package com.rodrickjones.navgraph.pathfinding;

import com.rodrickjones.navgraph.Graph;
import com.rodrickjones.navgraph.vertices.Vertex;

import java.util.Collection;

public class Dijkstras extends AStar {
    public Dijkstras(Graph graph) {
        super(graph);
    }

    @Override
    public double heuristic(Collection<Vertex> destination, Vertex currentVertex) {
        return 0;
    }
}
