package com.rodrickjones.navgraph.requirements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AndRequirement extends Requirement {
    public final static int TYPE = 1;
    private final List<Requirement> requirements;
    public AndRequirement(List<Requirement> requirements) {
        this.requirements = requirements;
    }

    AndRequirement(DataInputStream inputStream, RequirementReader requirementReader) throws IOException {
        int count = inputStream.readInt();
        requirements = new ArrayList<>(count);
        //get requirement readers and use those
        for (int i = 0; i < count; i++) {
            requirements.add(requirementReader.readRequirement(inputStream));
        }
    }

    @Override
    public void writeToDataStream(DataOutputStream dos) throws IOException {
        super.writeToDataStream(dos);
        dos.writeInt(requirements.size());
        for (Requirement requirement : requirements) {
            requirement.writeToDataStream(dos);
        }
    }

    @Override
    public boolean isMet(RequirementContext context) {
        return requirements.stream().allMatch(r -> r.isMet(context));
    }

    @Override
    public int getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "AndRequirement{" +
                "reqs=" + requirements +
                '}';
    }
}
