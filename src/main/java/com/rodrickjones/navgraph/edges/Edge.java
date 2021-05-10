package com.rodrickjones.navgraph.edges;

import com.rodrickjones.navgraph.requirements.Requirement;
import com.rodrickjones.navgraph.requirements.RequirementReader;
import com.rodrickjones.navgraph.vertices.Vertex;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public abstract class Edge {
    private final Vertex origin;
    private final Vertex destination;
    private final float cost;
    private final Requirement requirement;

    public Edge(Vertex origin, Vertex destination, float cost, Requirement requirement) {
        if (origin == null) {
            throw new IllegalArgumentException("origin cannot be null");
        }
        if (destination == null) {
            throw new IllegalArgumentException("destination cannot be null");
        }
        this.origin = origin;
        this.destination = destination;
        this.cost = cost;
        this.requirement = requirement;
    }

    public Edge(DataInputStream in, RequirementReader reader) throws IOException {
        this.origin = Vertex.readFromDataStream(in);
        this.destination = Vertex.readFromDataStream(in);
        this.cost = in.readFloat();
        this.requirement = in.readBoolean() ? reader.readRequirement(in) : null;
    }

    public Vertex getOrigin() {
        return origin;
    }

    public Vertex getDestination() {
        return destination;
    }

    public float getCost() {
        return cost;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public abstract int getType();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Edge edge = (Edge) o;
        return Objects.equals(origin, edge.origin) && Objects.equals(destination, edge.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, destination);
    }

    @Override
    public abstract String toString();

    public void writeToDataStream(DataOutputStream dos) throws IOException {
        dos.writeInt(getType());
        getOrigin().writeToDataStream(dos);
        getDestination().writeToDataStream(dos);
        dos.writeFloat(getCost());
        Requirement requirement = getRequirement();
        if (requirement != null) {
            dos.writeBoolean(true);
            requirement.writeToDataStream(dos);
        } else {
            dos.writeBoolean(false);
        }
    }
}
