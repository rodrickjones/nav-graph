package com.rodrickjones.navgraph.edge;

import com.rodrickjones.navgraph.requirement.RequirementReader;
import com.rodrickjones.navgraph.vertex.Vertex;

import java.io.DataInputStream;
import java.io.IOException;

@FunctionalInterface
public interface EdgeParser {
    Edge<Vertex> parseEdge(DataInputStream in, RequirementReader requirementReader) throws IOException;
}
