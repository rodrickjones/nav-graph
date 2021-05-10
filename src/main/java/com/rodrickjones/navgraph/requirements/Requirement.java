package com.rodrickjones.navgraph.requirements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public abstract class Requirement {
    public Requirement() {
    }

    public static Requirement and(Requirement... requirements) {
        return new AndRequirement(Arrays.asList(requirements));
    }

    public static Requirement or(Requirement... requirements) {
        return new OrRequirement(Arrays.asList(requirements));
    }

    public abstract <R extends RequirementContext> boolean isMet(R context);

    public abstract int getType();

    public void writeToDataStream(DataOutputStream dos) throws IOException {
        dos.writeInt(getType());
    }

    @Override
    public abstract String toString();
}
