package com.rodrickjones.navgraph.edges;

import com.rodrickjones.navgraph.requirements.RequirementReader;

import java.io.DataInputStream;
import java.io.IOException;

public interface EdgeParser {
    public Edge parseEdge(DataInputStream in, RequirementReader requirementReader) throws IOException;
}
