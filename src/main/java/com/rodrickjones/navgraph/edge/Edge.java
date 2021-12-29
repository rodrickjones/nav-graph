package com.rodrickjones.navgraph.edge;

import com.rodrickjones.navgraph.requirement.Requirement;
import com.rodrickjones.navgraph.vertex.Vertex;

public interface Edge {
    Vertex origin();

    Vertex destination();

    float cost();

    Requirement requirement();

    // TODO replace polymorphism with labels
    int getType();
}
