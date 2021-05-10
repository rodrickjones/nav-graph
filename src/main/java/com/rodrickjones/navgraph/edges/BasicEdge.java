package com.rodrickjones.navgraph.edges;

import com.rodrickjones.navgraph.requirements.RequirementReader;
import com.rodrickjones.navgraph.vertices.Vertex;
import com.rodrickjones.navgraph.requirements.Requirement;

import java.io.DataInputStream;
import java.io.IOException;

public final class BasicEdge extends Edge {
    public final static int TYPE = 0;
    public BasicEdge(Vertex origin, Vertex destination, float cost, Requirement requirement) {
        super(origin, destination, cost, requirement);
    }

    BasicEdge(DataInputStream in, RequirementReader requirementReader) throws IOException {
        super(in, requirementReader);
    }

    @Override
    public int getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "BasicEdge{" +
                "origin=" + getOrigin() +
                ", destination=" + getDestination() +
                ", cost=" + getCost() +
                '}';
    }
}
