package com.rodrickjones.navgraph.requirement;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class OrRequirement extends RequirementBase {
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
            // FIXME
            ((RequirementBase) requirement).writeToDataStream(dos);
        }
    }

    @Override
    public boolean satisfy(RequirementContext context) {
        return requirements.stream().anyMatch(r -> r.satisfy(context));
    }

    @Override
    public int type() {
        return 10;
    }

    @Override
    public String toString() {
        return "OrRequirement{" +
                "requirements=" + requirements +
                '}';
    }
}
