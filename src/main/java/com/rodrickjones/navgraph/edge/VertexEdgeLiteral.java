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

@Deprecated
public class VertexEdgeLiteral extends EdgeLiteral<Vertex> {

    public VertexEdgeLiteral(@NonNull Vertex origin, @NonNull Vertex destination, float cost, @NonNull Requirement requirement) {
        super(origin, destination, cost, requirement);
    }

    public VertexEdgeLiteral(@NonNull Vertex origin, @NonNull Vertex destination, float cost) {
        super(origin, destination, cost);
    }

    public VertexEdgeLiteral(DataInputStream in, RequirementReader reader) throws IOException {
        super(VertexLiteral.readFromDataStream(in), VertexLiteral.readFromDataStream(in), in.readFloat(), reader.readRequirement(in));
    }

    public void writeToDataStream(DataOutputStream dos) throws IOException {
        dos.writeInt(type());
        // FIXME
        ((VertexLiteral) origin).writeToDataStream(dos);
        ((VertexLiteral) destination).writeToDataStream(dos);
        dos.writeFloat(cost());
        // FIXME
        ((RequirementBase) requirement()).writeToDataStream(dos);
    }
}
