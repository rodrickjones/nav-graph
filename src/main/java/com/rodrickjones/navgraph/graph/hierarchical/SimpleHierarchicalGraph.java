package com.rodrickjones.navgraph.graph.hierarchical;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.graph.SimpleGraph;
import com.rodrickjones.navgraph.util.Frontier;
import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
public class SimpleHierarchicalGraph extends SimpleGraph implements HierarchicalGraph {
    private static final DecimalFormat fourDig = new DecimalFormat("0000");
    private static final DecimalFormat twoDif = new DecimalFormat("00");

    private final Map<Long, SimpleRegion> regions;
    private final Map<SubRegion, Collection<SubRegionEdge>> sectionEdges = new HashMap<>();

    public SimpleHierarchicalGraph() {
        super();
        regions = initRegions(1500);
    }

    public SimpleHierarchicalGraph(int initialVertexCapacity, int initialEdgeCapacity) {
        super(initialVertexCapacity, initialEdgeCapacity);
        regions = initRegions(initialVertexCapacity);
    }

    public SimpleHierarchicalGraph(Collection<Vertex> vertices, Collection<Edge> edges) {
        super(vertices, edges);
        regions = initRegions(vertices.size());
    }

    private static Map<Long, SimpleRegion> initRegions(int vertexCount) {
        return new HashMap<>((int) Math.ceil((double) vertexCount / SimpleRegion.HEIGHT / SimpleRegion.WIDTH));
    }

    private static String formatTime(long milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        return twoDif.format(hours) + ":" + twoDif.format(minutes % 60) + ":" + twoDif.format(seconds % 60) + "." + fourDig.format(milliseconds % 1000);
    }

    private static long regionId(int x, int y) {
        int baseX = x - (x % SimpleRegion.WIDTH);
        int baseY = y - (y % SimpleRegion.HEIGHT);
        return ((long) (baseX >> 6) << 8) | (baseY >> 6);
    }

    private static long regionId(Vertex vertex) {
        return regionId(vertex.x(), vertex.y());
    }

    public void compile() {
        Set<Vertex> visited = new HashSet<>(vertexCount());
        log.info("Compiling regions and sections");
        long start = System.currentTimeMillis();
        Iterator<Vertex> vertexIterator = vertices().iterator();
        while (vertexIterator.hasNext()) {
            Vertex vertex = vertexIterator.next();
            if (!visited.contains(vertex)) {
                SimpleRegion region = regions.computeIfAbsent(regionId(vertex), SimpleRegion::new);
                SimpleSubRegion subRegion = new SimpleSubRegion(region.id() + "_" + region.subRegions.size());
                region.addSubRegion(subRegion);
                Queue<Vertex> frontier = new Frontier<>(Comparator.comparingInt(Vertex::y));
                frontier.add(vertex);
                while (!frontier.isEmpty()) {
                    Vertex current = frontier.poll();
                    subRegion.addVertex(vertex);
                    Iterator<Edge> edgeIterator = edges(current).iterator();
                    while (edgeIterator.hasNext()) {
                        Edge edge = edgeIterator.next();
                        if (edge.requirement() != null) {
                            //Skip all requirements when pathing through the high level overview
                            continue;
                        }
                        Vertex dest = edge.destination();
                        if (!visited.contains(dest) && !frontier.contains(dest)
                                && dest.x() >= region.baseX() && dest.y() >= region.baseY()
                                && dest.x() < region.baseX() + SimpleRegion.WIDTH && dest.y() < region.baseY() + SimpleRegion.HEIGHT
                                && !subRegion.contains(dest)) {
                            frontier.add(dest);
                            subRegion.addVertex(dest);
                        }
                    }
                }
                subRegion.vertices().forEach(visited::add);
            }
        }
        log.info("Time taken: " + formatTime(System.currentTimeMillis() - start));
        log.info("Linking sections");
        start = System.currentTimeMillis();
        Iterator<SubRegion> subRegionIterator = regions.values().stream().flatMap(Region::subRegions).iterator();
        while (subRegionIterator.hasNext()) {
            SubRegion subRegion = subRegionIterator.next();
            Collection<SubRegionEdge> subRegionEdges = this.sectionEdges.computeIfAbsent(subRegion, k -> new ArrayList<>());
            Iterator<Edge> edgeIterator = subRegion.vertices().flatMap(this::edges).iterator();
            while (edgeIterator.hasNext()) {
                Edge edge = edgeIterator.next();
                if (!subRegion.contains(edge.destination())) {
                    subRegionEdges.add(new SimpleSubRegionEdge(subRegion, subRegion(edge.destination()),
                            Math.sqrt(subRegion.vertexCount()), edge.requirement()));
                }
            }
            if (subRegionEdges instanceof ArrayList<?>) {
                ((ArrayList<SubRegionEdge>) subRegionEdges).trimToSize();
            }
        }
        log.info("Time taken: " + formatTime(System.currentTimeMillis() - start));
    }

    @Override
    public @NonNull Stream<Region> regions() {
        return regions.values().stream().map(Function.identity());
    }

    @Override
    public @Nullable SubRegion subRegion(@NonNull Vertex vertex) {
        return regions.get(regionId(vertex)).subRegion(vertex);
    }

    @Override
    public Stream<SubRegionEdge> edges(@NonNull SubRegion subRegion) {
        Collection<SubRegionEdge> value = sectionEdges.get(subRegion);
        if (value == null) {
            return Stream.empty();
        }
        return value.stream();
    }

    @Override
    public String toString() {
        return "HeirarchicalGraph{" +
                "vertices=" + vertexCount() +
                ", edges=" + edgeCount() +
                ", regions=" + regions.size() +
                ", subRegions=" + regions.values().stream().mapToInt(Region::subRegionCount).sum() +
                ", sectionEdges=" + sectionEdges.values().stream().mapToInt(Collection::size).sum() +
                '}';
    }
}
