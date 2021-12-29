package com.rodrickjones.navgraph.pathfinding;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.edge.EdgeLiteral;
import com.rodrickjones.navgraph.edge.EdgeReader;
import com.rodrickjones.navgraph.requirement.RequirementReader;
import com.rodrickjones.navgraph.vertex.Vertex;
import com.rodrickjones.navgraph.vertex.VertexLiteral;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// TODO make package-private or extract interface
public class Path {
    private final List<Vertex> vertices;
    private final List<Edge> edges;
    private final double cost;

    public Path(List<Vertex> vertices, List<Edge> edges) {
        this.vertices = vertices;
        this.edges = edges;
        cost = edges.stream().mapToDouble(Edge::cost).sum();
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public double getCost() {
        return cost;
    }

    public static Path readFromDataStream(DataInputStream in,
                                          EdgeReader edgeReader,
                                          RequirementReader requirementReader) throws IOException {
        int vertexCount = in.readInt();
        List<Vertex> vertices = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            vertices.add(VertexLiteral.readFromDataStream(in));
        }

        int edgeCount = in.readInt();
        List<Edge> edges = new ArrayList<>(edgeCount);
        for (int i = 0; i < edgeCount; i++) {
            Edge edge = edgeReader.readEdge(in, requirementReader);
            edges.add(edge);
        }

        return new Path(vertices, edges);
    }

    public void writeToDataStream(DataOutputStream out) throws IOException {
        out.writeInt(vertices.size());
        for (Vertex v : vertices) {
            // FIXME
            ((VertexLiteral) v).writeToDataStream(out);
        }
        out.writeInt(edges.size());
        for (Edge e : edges) {
            // FIXME
            ((EdgeLiteral) e).writeToDataStream(out);
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
