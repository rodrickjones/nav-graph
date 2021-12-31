package com.rodrickjones.navgraph.edge;

import com.rodrickjones.navgraph.requirement.Requirement;
import org.jetbrains.annotations.NotNull;

public interface Edge<V> {
    @NotNull V origin();

    @NotNull V destination();

    float cost();

    @NotNull Requirement requirement();

    // TODO replace polymorphism with labels
    int type();
}
