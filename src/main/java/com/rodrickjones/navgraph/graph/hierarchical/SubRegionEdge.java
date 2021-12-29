package com.rodrickjones.navgraph.graph.hierarchical;

import com.rodrickjones.navgraph.requirement.RequirementContext;
import lombok.NonNull;

public interface SubRegionEdge {
    boolean canTraverse(@NonNull RequirementContext context);

    SubRegion origin();

    SubRegion destination();

    double cost();
}
