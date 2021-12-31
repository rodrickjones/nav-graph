package com.rodrickjones.navgraph.edge;

import com.rodrickjones.navgraph.requirement.Requirement;
import com.rodrickjones.navgraph.requirement.Requirements;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

public class EdgeLiteral<V> implements Edge<V> {

    public static final int TYPE = 0;

    protected final V origin;
    protected final V destination;
    protected final float cost;
    protected final Requirement requirement;

    public EdgeLiteral(@NonNull V origin, @NonNull V destination, float cost, @NonNull Requirement requirement) {
        this.origin = origin;
        this.destination = destination;
        this.cost = cost;
        this.requirement = requirement;
    }

    public EdgeLiteral(@NonNull V origin, @NonNull V destination, float cost) {
        this.origin = origin;
        this.destination = destination;
        this.cost = cost;
        this.requirement = Requirements.none();
    }

    @Override
    public @NotNull V origin() {
        return origin;
    }

    @Override
    public @NotNull V destination() {
        return destination;
    }

    @Override
    public float cost() {
        return cost;
    }

    @Override
    public @NotNull Requirement requirement() {
        return requirement;
    }

    @Override
    public int type() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "EdgeLiteral{" +
                "origin=" + origin +
                ", destination=" + destination +
                ", cost=" + cost +
                ", requirement=" + requirement +
                '}';
    }
}
