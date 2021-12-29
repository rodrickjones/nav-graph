package com.rodrickjones.navgraph.requirement;

import java.io.DataOutputStream;
import java.io.IOException;

// TODO remove
public abstract class RequirementBase implements Requirement {

    public void writeToDataStream(DataOutputStream dos) throws IOException {
        dos.writeInt(type());
    }
}
