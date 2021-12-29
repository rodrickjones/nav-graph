package com.rodrickjones.navgraph.edge;

import com.rodrickjones.navgraph.requirement.RequirementReader;

import java.io.DataInputStream;
import java.io.IOException;

public interface EdgeParser {
    Edge parseEdge(DataInputStream in, RequirementReader requirementReader) throws IOException;
}
