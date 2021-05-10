package com.rodrickjones.navgraph.requirements;

import java.io.DataInputStream;
import java.io.IOException;

public interface RequirementParser {
    public Requirement parseRequirement(DataInputStream in) throws IOException;
}
