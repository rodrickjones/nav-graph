package com.rodrickjones.navgraph.requirement;

import lombok.experimental.UtilityClass;

import java.util.Arrays;

@UtilityClass
public class Requirements {

    private static final Requirement NONE = new RequirementBase() {
        @Override
        public <R extends RequirementContext> boolean satisfy(R context) {
            return true;
        }

        @Override
        public int type() {
            // TODO revisit
            return Integer.MAX_VALUE;
        }
    };

    public static Requirement none() {
        return NONE;
    }

    public static Requirement and(Requirement... requirements) {
        return new AndRequirement(Arrays.asList(requirements));
    }

    public static Requirement or(Requirement... requirements) {
        return new OrRequirement(Arrays.asList(requirements));
    }
}
