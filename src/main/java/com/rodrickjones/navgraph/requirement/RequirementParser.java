package com.rodrickjones.navgraph.requirement;

import java.io.DataInputStream;
import java.io.IOException;

// TODO remove
public interface RequirementParser {
    Requirement parseRequirement(DataInputStream in) throws IOException;
}
