package com.rodrickjones.navgraph.edge;

import com.rodrickjones.navgraph.requirement.Requirement;
import com.rodrickjones.navgraph.requirement.RequirementBase;
import com.rodrickjones.navgraph.requirement.RequirementReader;
import com.rodrickjones.navgraph.requirement.Requirements;
import com.rodrickjones.navgraph.vertex.Vertex;
import com.rodrickjones.navgraph.vertex.VertexLiteral;
import lombok.NonNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class EdgeLiteral implements Edge {
    private final Vertex origin;
    private final Vertex destination;
    private final float cost;
    private final Requirement requirement;

    protected EdgeLiteral(@NonNull Vertex origin, @NonNull Vertex destination, float cost, @NonNull Requirement requirement) {
        this.origin = origin;
        this.destination = destination;
        this.cost = cost;
        this.requirement = requirement;
    }

    protected EdgeLiteral(@NonNull Vertex origin, @NonNull Vertex destination, float cost) {
        this.origin = origin;
        this.destination = destination;
        this.cost = cost;
        this.requirement = Requirements.none();
    }

    protected EdgeLiteral(DataInputStream in, RequirementReader reader) throws IOException {
        this.origin = VertexLiteral.readFromDataStream(in);
        this.destination = VertexLiteral.readFromDataStream(in);
        this.cost = in.readFloat();
        this.requirement = in.readBoolean() ? reader.readRequirement(in) : null;
    }

    @Override
    public Vertex origin() {
        return origin;
    }

    @Override
    public Vertex destination() {
        return destination;
    }

    @Override
    public float cost() {
        return cost;
    }

    @Override
    public Requirement requirement() {
        return requirement;
    }

    public void writeToDataStream(DataOutputStream dos) throws IOException {
        dos.writeInt(getType());
        // FIXME
        ((VertexLiteral) origin).writeToDataStream(dos);
        ((VertexLiteral) destination).writeToDataStream(dos);
        dos.writeFloat(cost());
        Requirement requirement = requirement();
        if (requirement != null) {
            dos.writeBoolean(true);
            // FIXME
            ((RequirementBase) requirement).writeToDataStream(dos);
        } else {
            dos.writeBoolean(false);
        }
    }
}
