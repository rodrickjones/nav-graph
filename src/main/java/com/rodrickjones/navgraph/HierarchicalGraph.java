package com.rodrickjones.navgraph;

import com.rodrickjones.navgraph.edges.Edge;
import com.rodrickjones.navgraph.requirements.RequirementContext;
import com.rodrickjones.navgraph.util.Frontier;
import com.rodrickjones.navgraph.vertices.Vertex;
import com.rodrickjones.navgraph.requirements.Requirement;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class HierarchicalGraph extends SimpleGraph {
    private final Map<Integer, Region> regions = new HashMap<>(1500);
    private final Map<SubRegion, Collection<SubRegionEdge>> sectionEdges = new HashMap<>();

    public HierarchicalGraph() {
        super();
    }

    public HierarchicalGraph(int initialVertexCapacity, int initialEdgeCapacity) {
        super(initialVertexCapacity, initialEdgeCapacity);
    }

    public HierarchicalGraph(Collection<Vertex> vertices, Collection<Edge> edges) {
        super(vertices, edges);
    }

    public void compile() {
        Set<Vertex> visited = new HashSet<>(getVertices().size());
        log.info("Compiling regions and sections");
        long start = System.currentTimeMillis();
        for (Vertex vertex : getVertices()) {
            if (!visited.contains(vertex)) {
                Region region = regions.computeIfAbsent(getRegionId(vertex), Region::new);
                SubRegion subRegion = new SubRegion(region.id + "_" + region.subRegions.size());
                region.addSubRegion(subRegion);
                Queue<Vertex> frontier = new Frontier<>(Comparator.comparingInt(Vertex::getY));
                frontier.add(vertex);
                while (!frontier.isEmpty()) {
                    Vertex current = frontier.poll();
                    subRegion.add(vertex);
                    Collection<Edge> edges = getEdges(current);
                    if (edges != null) {
                        for (Edge edge : edges) {
                            if (edge.getRequirement() != null) {
                                //Skip all requirements when pathing through the high level overview
                                continue;
                            }
                            Vertex dest = edge.getDestination();
                            if (!visited.contains(dest) && !frontier.contains(dest)
                                    && dest.getX() >= region.getBaseX() && dest.getY() >= region.getBaseY()
                                    && dest.getX() < region.getBaseX() + Region.WIDTH && dest.getY() < region.getBaseY() + Region.HEIGHT
                                    && !subRegion.contains(dest)) {
                                frontier.add(dest);
                                subRegion.add(dest);
                            }
                        }
                    }
                }
                visited.addAll(subRegion.getVertices());
            }
        }
        log.info("Time taken: " + formatTime(System.currentTimeMillis() - start));
        log.info("Linking sections");
        start = System.currentTimeMillis();
        List<SubRegion> subRegions = regions.values().stream().flatMap(r -> r.getSubRegions().stream()).collect(Collectors.toList());
        for (SubRegion subRegion : subRegions) {
            Collection<SubRegionEdge> subRegionEdges = this.sectionEdges.computeIfAbsent(subRegion, k -> new ArrayList<>());
            for (Vertex vertex : subRegion.getVertices()) {
                Collection<Edge> edges = getEdges(vertex);
                if (edges != null) {
                    for (Edge edge : edges) {
                        if (!subRegion.contains(edge.getDestination())) {
                            subRegionEdges.add(new SubRegionEdge(subRegion, getSubRegion(edge.getDestination()),
                                    Math.sqrt(subRegion.getVertices().size()), edge.getRequirement()));
                        }
                    }
                }
            }
            if(subRegionEdges instanceof ArrayList) {
                ((ArrayList<SubRegionEdge>) subRegionEdges).trimToSize();
            }
        }
        log.info("Time taken: " + formatTime(System.currentTimeMillis() - start));
    }

    static DecimalFormat fourDig = new DecimalFormat("0000");
    static DecimalFormat twoDif = new DecimalFormat("00");
    private static String formatTime(long milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        return twoDif.format(hours) + ":" + twoDif.format(minutes % 60) + ":" + twoDif.format(seconds % 60) + "." + fourDig.format(milliseconds % 1000);
    }

    public SubRegion getSubRegion(Vertex vertex) {
        return regions.get(getRegionId(vertex)).getSubRegion(vertex);
    }

    private SubRegion getSubRegion(int x, int y, int z) {
        return regions.get(getRegionId(x, y)).getSubRegion(x, y, z);
    }

    public Collection<SubRegionEdge> getEdges(SubRegion subRegion) {
        return sectionEdges.get(subRegion);
    }

    static int getRegionId(int x, int y) {
        int baseX = x - (x % Region.WIDTH);
        int baseY = y - (y % Region.HEIGHT);
        return ((baseX >> 6) << 8) | (baseY >> 6);
    }

    static int getRegionId(Vertex vertex) {
        return getRegionId(vertex.getX(), vertex.getY());
    }

    public static class SubRegion {
        private final String id;
        Map<Integer, Vertex> vertices = new HashMap<>();

        public SubRegion(String id) {
            this.id = id;
        }

        void add(Vertex vertex) {
            vertices.put(vertex.hashCode(), vertex);
        }

        Vertex getVertex(int hashCode) {
            return vertices.get(hashCode);
        }

        boolean contains(Vertex vertex) {
            return vertices.containsKey(vertex.hashCode());
        }

        public Collection<Vertex> getVertices() {
            return vertices.values();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SubRegion subRegion = (SubRegion) o;
            return id.equals(subRegion.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    public static class SubRegionEdge {
        final SubRegion origin;
        final SubRegion destination;
        final double cost;
        final Requirement requirement;

        SubRegionEdge(SubRegion origin, SubRegion destination, double cost, Requirement requirement) {
            this.origin = origin;
            this.destination = destination;
            this.cost = cost;
            this.requirement = requirement;
        }

        public boolean canTraverse(RequirementContext context) {
            return requirement == null || requirement.isMet(context);
        }

        public SubRegion getDestination() {
            return destination;
        }

        public double getCost() {
            return cost;
        }
    }

    static class Region {
        private final int id;
        private final int baseX;
        private final int baseY;
        final static int WIDTH = 64;
        final static int HEIGHT = 64;

        List<SubRegion> subRegions = new ArrayList<>();

        public Region(int id) {
            this.id = id;
            this.baseX = (id >> 8 & 0xFF) << 6;
            this.baseY = (id & 0xFF) << 6;
        }

        public int getBaseX() {
            return baseX;
        }

        public int getBaseY() {
            return baseY;
        }

        SubRegion getSubRegion(Vertex vertex) {
            return subRegions.stream().filter(s -> s.contains(vertex)).findAny().orElse(null);
        }

        SubRegion getSubRegion(int x, int y, int z) {
            return subRegions.stream().filter(s -> s.vertices.containsKey(Vertex.hashCode(x, y, z))).findAny().orElse(null);
        }

        public List<SubRegion> getSubRegions() {
            return subRegions;
        }

        void addSubRegion(SubRegion subRegion) {
            subRegions.add(subRegion);
        }

        boolean contains(Vertex vertex) {
            return subRegions.stream().anyMatch(s -> s.contains(vertex));
        }
    }

    @Override
    public String toString() {
        return "HeirarchicalGraph{" +
                "vertices=" + getVertexCount() +
                ", edges=" + getEdgeCount() +
                ", regions=" + regions.size() +
                ", subRegions=" + regions.values().stream().mapToInt(r -> r.getSubRegions().size()).sum() +
                ", sectionEdges=" + sectionEdges.values().stream().mapToInt(Collection::size).sum() +
                '}';
    }
}
