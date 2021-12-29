package com.rodrickjones.navgraph.requirement;

// TODO @FunctionalInterface
public interface Requirement {

    <R extends RequirementContext> boolean satisfy(R context);

    // TODO remove
    int type();
}
