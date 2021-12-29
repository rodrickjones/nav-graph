package com.rodrickjones.navgraph.graph.hierarchical;

import com.rodrickjones.navgraph.requirement.Requirement;
import com.rodrickjones.navgraph.requirement.RequirementContext;
import lombok.NonNull;

public class SimpleSubRegionEdge implements SubRegionEdge {
    final SubRegion origin;
    final SubRegion destination;
    final double cost;
    final Requirement requirement;

    SimpleSubRegionEdge(SubRegion origin, SubRegion destination, double cost, Requirement requirement) {
        this.origin = origin;
        this.destination = destination;
        this.cost = cost;
        this.requirement = requirement;
    }

    @Override
    public boolean canTraverse(@NonNull RequirementContext context) {
        return requirement == null || requirement.satisfy(context);
    }

    @Override
    public SubRegion origin() {
        return origin;
    }

    @Override
    public SubRegion destination() {
        return destination;
    }

    @Override
    public double cost() {
        return cost;
    }
}
