package com.rodrickjones.navgraph.pathfinding;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.edge.EdgeReader;
import com.rodrickjones.navgraph.edge.VertexEdgeLiteral;
import com.rodrickjones.navgraph.requirement.RequirementReader;
import com.rodrickjones.navgraph.vertex.Vertex;
import com.rodrickjones.navgraph.vertex.VertexLiteral;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// TODO make package-private or extract interface
public class Path<V> {
    private final List<V> vertices;
    private final List<Edge<V>> edges;
    private final float cost;

    public Path(List<V> vertices, List<Edge<V>> edges) {
        this.vertices = vertices;
        this.edges = edges;
        cost = (float) edges.stream().mapToDouble(Edge::cost).sum();
    }

    public List<V> getVertices() {
        return vertices;
    }

    public List<Edge<V>> getEdges() {
        return edges;
    }

    public double getCost() {
        return cost;
    }

    public static <V> Path<V> readFromDataStream(DataInputStream in,
                                          EdgeReader edgeReader,
                                          RequirementReader requirementReader) throws IOException {
        int vertexCount = in.readInt();
        List<V> vertices = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            // FIXME
            vertices.add((V) VertexLiteral.readFromDataStream(in));
        }

        int edgeCount = in.readInt();
        List<Edge<V>> edges = new ArrayList<>(edgeCount);
        for (int i = 0; i < edgeCount; i++) {
            // FIXME
            Edge<V> edge = (Edge<V>) edgeReader.readEdge(in, requirementReader);
            edges.add(edge);
        }

        return new Path<>(vertices, edges);
    }

    public void writeToDataStream(DataOutputStream out) throws IOException {
        out.writeInt(vertices.size());
        for (V v : vertices) {
            // FIXME
            ((VertexLiteral) v).writeToDataStream(out);
        }
        out.writeInt(edges.size());
        for (Edge<V> e : edges) {
            // FIXME
            ((VertexEdgeLiteral) e).writeToDataStream(out);
        }
    }

    @Override
    public String toString() {
        return "Path{" +
                "origin=" + vertices.get(0) +
                ", destination=" + vertices.get(vertices.size() - 1) +
                ", vertices=" + vertices.size() +
                ", edges=" + edges.size() +
                ", cost=" + cost +
                '}';
    }
}
