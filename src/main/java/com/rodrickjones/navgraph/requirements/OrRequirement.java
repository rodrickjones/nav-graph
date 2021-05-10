package com.rodrickjones.navgraph.requirements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OrRequirement extends Requirement {
    public final static int TYPE = 0;
    private final List<Requirement> requirements;
    public OrRequirement(List<Requirement> requirements) {
        this.requirements = requirements;
    }

    OrRequirement(DataInputStream inputStream, RequirementReader requirementReader) throws IOException {
        int count = inputStream.readInt();
        requirements = new ArrayList<>(count);
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
        return requirements.stream().anyMatch(r -> r.isMet(context));
    }

    @Override
    public int getType() {
        return 10;
    }

    @Override
    public String toString() {
        return "OrRequirement{" +
                "requirements=" + requirements +
                '}';
    }
}
