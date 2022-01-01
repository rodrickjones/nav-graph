package com.rodrickjones.navgraph.pathfinding;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.graph.Graph;
import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class Dijkstras<G extends Graph<?>> extends AStar<G> {

    public Dijkstras(@NonNull G graph, @NonNull BiFunction<G, Vertex, Stream<Edge<Vertex>>> edgesFunction) {
        super(graph, edgesFunction);
    }

    public Dijkstras(Graph<Vertex> graph) {
        super(graph);
    }

    @Override
    public double heuristic(Collection<Vertex> destination, Vertex currentVertex) {
        return 0;
    }
}
