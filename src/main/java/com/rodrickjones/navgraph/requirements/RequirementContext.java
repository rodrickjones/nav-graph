package com.rodrickjones.navgraph.requirements;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface RequirementContext {
    void writeToDataStream(DataOutputStream out) throws IOException;
}
