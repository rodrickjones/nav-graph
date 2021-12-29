package com.rodrickjones.navgraph.requirement;

import java.io.DataOutputStream;
import java.io.IOException;

public interface RequirementContext {
    // TODO remove
    void writeToDataStream(DataOutputStream out) throws IOException;
}
