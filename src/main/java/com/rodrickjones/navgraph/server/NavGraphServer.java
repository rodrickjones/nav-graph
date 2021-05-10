package com.rodrickjones.navgraph.server;

import com.rodrickjones.navgraph.pathfinding.PathfindingAlgorithm;
import com.rodrickjones.navgraph.requirements.RequirementReader;
import com.rodrickjones.navgraph.util.GraphIO;
import com.rodrickjones.navgraph.vertices.Vertex;
import com.rodrickjones.navgraph.HierarchicalGraph;
import com.rodrickjones.navgraph.edges.EdgeReader;
import com.rodrickjones.navgraph.requirements.BasicRequirementContext;
import com.rodrickjones.navgraph.requirements.RequirementContext;
import com.rodrickjones.navgraph.pathfinding.Hierarchical;
import com.rodrickjones.navgraph.pathfinding.Path;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

@Slf4j
public class NavGraphServer {
    private final HierarchicalGraph graph;
    private final PathfindingAlgorithm<HierarchicalGraph> pathfindingAlgorithm;
    private final HttpServer server;
    private final Function<DataInputStream, RequirementContext> contextFunction;

    public NavGraphServer(File file, String hostname, int port, Function<DataInputStream, RequirementContext> contextFunction) throws IOException {
        this.contextFunction = contextFunction;
        long start = System.currentTimeMillis();
        graph = GraphIO.readFromZip(file, HierarchicalGraph.class, EdgeReader.getDefault(), RequirementReader.getDefault());
        if (graph == null) {
            throw new IllegalStateException("No graph loaded");
        }
        graph.compile();
        pathfindingAlgorithm = new Hierarchical(graph);

        server = HttpServer.create();
        server.bind(new InetSocketAddress(hostname, port), 0);
        HttpContext context = server.createContext("/nav-graph/findPath", exchange -> {
            Vertex origin;
            Collection<Vertex> destinations;
            RequirementContext reqContext;
            try (DataInputStream input = new DataInputStream(exchange.getRequestBody())) {
                origin = Vertex.readFromDataStream(input);
                int destinationCount = input.readInt();
                destinations = new ArrayList<>(destinationCount);
                for (int i = 0; i < destinationCount; i++) {
                    destinations.add(Vertex.readFromDataStream(input));
                }
                reqContext = contextFunction.apply(input);
            }
            Path path = pathfindingAlgorithm.findPath(origin, destinations, reqContext);
            if (path != null) {
                exchange.sendResponseHeaders(200, 0);
                try (DataOutputStream out = new DataOutputStream(exchange.getResponseBody())) {
                    path.writeToDataStream(out);
                }
            } else {
                exchange.sendResponseHeaders(204, -1);
            }
            exchange.close();
        });
        server.start();
        log.info("Server started in " + (System.currentTimeMillis() - start) + "ms");
    }

    public void stop() {
        server.stop(5);
    }

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        NavGraphServer graphServer = new NavGraphServer(new File("graph.zip"), "localhost", 8080,
                dis -> {
                    try {
                        return new BasicRequirementContext(dis);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new IllegalStateException("Unable to read RequirementContext", e);
                    }
                });

        test();
    }

    public static void test() throws IOException, InterruptedException, URISyntaxException {

        URL url = new URL("http://localhost:8080/nav-graph/findPath");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
            new Vertex(3208, 3218, 2).writeToDataStream(dos);
            dos.writeInt(1);
            new Vertex(2967, 3377, 0).writeToDataStream(dos);
        }


        long start = System.currentTimeMillis();
        int status = connection.getResponseCode();
        if (status == 200) {
            try (DataInputStream in = new DataInputStream(connection.getInputStream())) {
                Path path = Path.readFromDataStream(in, EdgeReader.getDefault(), RequirementReader.getDefault());
                log.info(String.valueOf(path));
            }
        } else {
            log.info("no path");
        }
        log.info("Path retrieved in " + (System.currentTimeMillis() - start) + "ms");
    }
}
