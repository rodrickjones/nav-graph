package com.rodrickjones.navgraph.graph.hierarchical;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.edge.EdgeLiteral;
import com.rodrickjones.navgraph.graph.Graph;
import com.rodrickjones.navgraph.graph.MutableGraph;
import com.rodrickjones.navgraph.graph.SimpleGraph;
import com.rodrickjones.navgraph.requirement.Requirements;
import com.rodrickjones.navgraph.util.Frontier;
import com.rodrickjones.navgraph.vertex.Vertex;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
public class SimpleHierarchicalGraph implements HierarchicalGraph<Graph<Vertex>, Vertex>, MutableGraph<Vertex> {
    private static final DecimalFormat fourDig = new DecimalFormat("0000");
    private static final DecimalFormat twoDif = new DecimalFormat("00");

    private final SimpleGraph<Vertex> delegate;
    private final Map<Long, SimpleRegion> regions;
    private final Map<Graph<Vertex>, Collection<Edge<Graph<Vertex>>>> sectionEdges = new HashMap<>();

    private volatile boolean compiled;
    private volatile FlattenedView flattened;

    public SimpleHierarchicalGraph() {
        delegate = new SimpleGraph<>();
        regions = initRegions(1500);
    }

    public SimpleHierarchicalGraph(int initialVertexCapacity, int initialEdgeCapacity) {
        delegate = new SimpleGraph<>(initialVertexCapacity, initialEdgeCapacity);
        regions = initRegions(initialVertexCapacity);
    }

    public SimpleHierarchicalGraph(Collection<Vertex> vertices, Collection<Edge<Vertex>> edges) {
        delegate = new SimpleGraph<>(vertices, edges);
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

    private synchronized boolean compile() {
        if (compiled) {
            return false;
        }
        delegate.compact();
        Set<Vertex> visited = new HashSet<>((int) delegate.vertexCount());
        log.info("Compiling regions and subgraphs");
        long start = System.currentTimeMillis();
        Iterator<Vertex> vertexIterator = delegate.vertices().iterator();
        while (vertexIterator.hasNext()) {
            Vertex vertex = vertexIterator.next();
            if (!visited.contains(vertex)) {
                SimpleRegion region = regions.computeIfAbsent(regionId(vertex), SimpleRegion::new);
                int maxVertices = SimpleRegion.WIDTH * SimpleRegion.HEIGHT;
                SimpleGraph<Vertex> subGraph = new SimpleGraph<>(maxVertices, maxVertices * 4);
                region.addSubGraph(subGraph);
                Queue<Vertex> frontier = new Frontier<>(Comparator.comparingInt(Vertex::y));
                frontier.add(vertex);
                while (!frontier.isEmpty()) {
                    Vertex current = frontier.poll();
                    subGraph.addVertex(vertex);
                    Iterator<Edge<Vertex>> edgeIterator = delegate.edges(current).iterator();
                    while (edgeIterator.hasNext()) {
                        Edge<Vertex> edge = edgeIterator.next();
                        subGraph.addEdge(edge);
                        if (!edge.requirement().equals(Requirements.none())) {
                            //Skip all requirements when pathing through the high level overview
                            continue;
                        }
                        Vertex dest = edge.destination();
                        if (!visited.contains(dest) && !frontier.contains(dest)
                                && dest.x() >= region.baseX() && dest.y() >= region.baseY()
                                && dest.x() < region.baseX() + SimpleRegion.WIDTH && dest.y() < region.baseY() + SimpleRegion.HEIGHT
                                && !subGraph.containsVertex(dest)) {
                            frontier.add(dest);
                            subGraph.addVertex(dest);
                        }
                    }
                }
                subGraph.vertices().forEach(visited::add);
            }
        }
        log.info("Time taken: " + formatTime(System.currentTimeMillis() - start));
        log.info("Linking sections");
        start = System.currentTimeMillis();
        Iterator<Graph<Vertex>> subGraphIterator = regions.values().stream().flatMap(Region::graphs).iterator();
        while (subGraphIterator.hasNext()) {
            Graph<Vertex> subGraph = subGraphIterator.next();
            Collection<Edge<Graph<Vertex>>> subGraphEdges = this.sectionEdges.computeIfAbsent(subGraph, k -> new LinkedHashSet<>());
            Iterator<Edge<Vertex>> edgeIterator = subGraph.vertices().flatMap(delegate::edges).iterator();
            while (edgeIterator.hasNext()) {
                Edge<Vertex> edge = edgeIterator.next();
                if (!subGraph.containsVertex(edge.destination())) {
                    subGraphEdges.add(new EdgeLiteral<>(subGraph, Objects.requireNonNull(_sub(edge.destination())),
                            (float) Math.sqrt(subGraph.vertexCount()), edge.requirement()));
                }
            }
            if (subGraphEdges instanceof ArrayList<?>) {
                ((ArrayList<Edge<Graph<Vertex>>>) subGraphEdges).trimToSize();
            }
        }
        log.info("Time taken: " + formatTime(System.currentTimeMillis() - start));
        return compiled = true;
    }

    @Override
    public @Nullable Graph<Vertex> sub(@NonNull Vertex vertex) {
        compile();
        return _sub(vertex);
    }

    private @Nullable Graph<Vertex> _sub(@NonNull Vertex vertex) {
        SimpleRegion region = regions.get(regionId(vertex));
        if (region == null) {
            return null;
        }
        return region.graph(vertex);
    }

    @Override
    public @NotNull <G extends Graph<Vertex> & MutableGraph<Vertex>> G flatten() {
        if (flattened == null) {
            flattened = new FlattenedView();
        }
        //noinspection unchecked
        return (G) flattened;
    }

    @Override
    public @NotNull Stream<Graph<Vertex>> vertices() {
        compile();
        return regions.values().stream().flatMap(Region::graphs);
    }

    @Override
    public long vertexCount() {
        compile();
        return regions.values().stream().mapToLong(Region::graphCount).sum();
    }

    @Override
    public boolean containsVertex(@NonNull Graph<Vertex> vertex) {
        compile();
        return vertices().anyMatch(vertex::equals);
    }

    @Override
    public @NotNull Stream<Edge<Graph<Vertex>>> edges() {
        compile();
        return sectionEdges.values().stream().flatMap(Collection::stream);
    }

    @Override
    public @NotNull Stream<Edge<Graph<Vertex>>> edges(@NonNull Graph<Vertex> subGraph) {
        compile();
        Collection<Edge<Graph<Vertex>>> value = sectionEdges.get(subGraph);
        if (value == null) {
            return Stream.empty();
        }
        return value.stream();
    }

    @Override
    public long edgeCount() {
        compile();
        return sectionEdges.values().stream().mapToLong(Collection::size).sum();
    }

    @Override
    public boolean containsEdge(@NonNull Edge<Graph<Vertex>> edge) {
        compile();
        return edges().anyMatch(edge::equals);
    }

    // TODO revisit
    @Override
    public String toString() {
        if (!compiled) {
            return "SimpleHeirarchicalGraph{" +
                    "compiled=false" +
                    '}';
        }
        return "SimpleHeirarchicalGraph{" +
                "compiled=true" +
                ", vertices=" + vertexCount() +
                ", edges=" + edgeCount() +
                ", regions=" + regions.size() +
                ", subs=" + regions.values().stream().mapToLong(Region::graphCount).sum() +
                ", sectionEdges=" + sectionEdges.values().stream().mapToInt(Collection::size).sum() +
                '}';
    }

    @Override
    public void addVertices(@NonNull Stream<Vertex> vertices) {
        if (compiled) {
            throw new IllegalStateException();
        }
        delegate.addVertices(vertices);
    }

    @Override
    public void addVertex(@NonNull Vertex vertex) {
        if (compiled) {
            throw new IllegalStateException();
        }
        delegate.addVertex(vertex);
    }

    @Override
    public void addEdges(@NonNull Stream<Edge<Vertex>> edges) {
        if (compiled) {
            throw new IllegalStateException();
        }
        delegate.addEdges(edges);
    }

    @Override
    public void addEdge(@NonNull Edge<Vertex> edge) {
        if (compiled) {
            throw new IllegalStateException();
        }
        delegate.addEdge(edge);
    }

    private class FlattenedView implements Graph<Vertex>, MutableGraph<Vertex> {

        @Override
        public @NotNull Stream<Vertex> vertices() {
            return delegate.vertices();
        }

        @Override
        public long vertexCount() {
            return delegate.vertexCount();
        }

        @Override
        public boolean containsVertex(@NonNull Vertex vertex) {
            return delegate.containsVertex(vertex);
        }

        @Override
        public @NotNull Stream<Edge<Vertex>> edges() {
            return delegate.edges();
        }

        @Override
        public @NotNull Stream<Edge<Vertex>> edges(@NonNull Vertex vertex) {
            return delegate.edges(vertex);
        }

        @Override
        public long edgeCount() {
            return delegate.edgeCount();
        }

        @Override
        public boolean containsEdge(@NonNull Edge<Vertex> edge) {
            return delegate.containsEdge(edge);
        }

        @Override
        public void addVertices(@NonNull Stream<Vertex> vertices) {
            SimpleHierarchicalGraph.this.addVertices(vertices);
        }

        @Override
        public void addVertex(@NonNull Vertex vertex) {
            SimpleHierarchicalGraph.this.addVertex(vertex);
        }

        @Override
        public void addEdges(@NonNull Stream<Edge<Vertex>> edges) {
            SimpleHierarchicalGraph.this.addEdges(edges);
        }

        @Override
        public void addEdge(@NonNull Edge<Vertex> edge) {
            SimpleHierarchicalGraph.this.addEdge(edge);
        }
    }
}
