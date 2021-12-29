package com.rodrickjones.navgraph.edge;

import com.rodrickjones.navgraph.requirement.Requirement;
import com.rodrickjones.navgraph.requirement.RequirementReader;
import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;

import java.io.DataInputStream;
import java.io.IOException;

public final class BasicEdge extends EdgeLiteral {
    public final static int TYPE = 0;

    public BasicEdge(@NonNull Vertex origin, @NonNull Vertex destination, float cost, @NonNull Requirement requirement) {
        super(origin, destination, cost, requirement);
    }

    public BasicEdge(@NonNull Vertex origin, @NonNull Vertex destination, float cost) {
        super(origin, destination, cost);
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
                "origin=" + origin() +
                ", destination=" + destination() +
                ", cost=" + cost() +
                '}';
    }
}
