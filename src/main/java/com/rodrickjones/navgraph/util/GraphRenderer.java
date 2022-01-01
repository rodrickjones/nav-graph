package com.rodrickjones.navgraph.util;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.edge.EdgeLiteral;
import com.rodrickjones.navgraph.graph.Graph;
import com.rodrickjones.navgraph.pathfinding.Path;
import com.rodrickjones.navgraph.vertex.Vertex;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GraphRenderer<V> {

    public static <V extends Vertex> BufferedImage renderToImage(Graph<V> graph, int plane, int vertexSize, Path<V>... paths) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = 0;
        int maxY = 0;
        Iterator<V> vertexIterator = graph.vertices().iterator();
        while (vertexIterator.hasNext()) {
            V vertex = vertexIterator.next();
            int x = vertex.x();
            int y = vertex.y();
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }
        }

        BufferedImage image = BigBufferedImage.create((maxX - minX + 1) * vertexSize, (maxY - minY + 1) * vertexSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        AffineTransform tran = AffineTransform.getTranslateInstance(0, image.getHeight());
        AffineTransform flip = AffineTransform.getScaleInstance(1d, -1d);
        tran.concatenate(flip);
        graphics.transform(tran);

        graphics.setPaint(Color.WHITE);
        graphics.fillRect(0 ,0, image.getWidth(), image.getHeight());
        Color vertexFill = Color.LIGHT_GRAY;
//        Color vertexBorder = Color.GRAY;
        Color wall = Color.BLACK;
        vertexIterator = graph.vertices().iterator();
        while (vertexIterator.hasNext()) {
            V vertex = vertexIterator.next();
            if (vertex.z() != plane) {
                continue;
            }
            Stream<Edge<V>> edges = graph.edges(vertex);
            int x = vertex.x() - minX;
            int y = vertex.y() - minY;

            graphics.setPaint(vertexFill);
            graphics.fillRect(x * vertexSize, y * vertexSize, vertexSize, vertexSize);
//            graphics.setPaint(vertexBorder);
//            graphics.drawRect(x * vertexSize, y * vertexSize, vertexSize, vertexSize);
            graphics.setPaint(wall);
            Set<String> diff = edges.filter(e -> e.type() == EdgeLiteral.TYPE)
                    .map(Edge::destination)
                    .map(d -> (d.x() - vertex.x()) + ", " + (d.y() - vertex.y()))
                    .collect(Collectors.toSet());
            //no neighbour north
            if (!diff.contains("0, 1")) {
                graphics.drawLine(x * vertexSize, (y + 1) * vertexSize,
                        (x + 1) * vertexSize, (y + 1) * vertexSize);
            }
            //no neighbour east
            if (!diff.contains("1, 0")) {
                graphics.drawLine((x + 1) * vertexSize, y * vertexSize,
                        (x + 1) * vertexSize, (y + 1) * vertexSize);
            }
            //no neighbour south
            if (!diff.contains("0, -1")) {
                graphics.drawLine(x * vertexSize, y * vertexSize,
                        (x + 1) * vertexSize, y * vertexSize);
            }
            //no neighbour west
            if (!diff.contains("-1, 0")) {
                graphics.drawLine(x * vertexSize, y * vertexSize,
                        x * vertexSize, (y + 1) * vertexSize);
            }
        }

        int offset = (int) ((vertexSize + 0.5d) / 2);
        List<Edge<V>> nonBasicEdges = new ArrayList<>(10000);
        graphics.setPaint(Color.GREEN);
        vertexIterator = graph.vertices().iterator();
        while (vertexIterator.hasNext()) {
            V vertex = vertexIterator.next();
            if (vertex.z() != plane) {
                continue;
            }
            int x = vertex.x() - minX;
            int y = vertex.y() - minY;
            Stream<Edge<V>> edges = graph.edges(vertex);
            Iterator<Edge<V>> edgeIterator = edges.iterator();
            while (edgeIterator.hasNext()) {
                Edge<V> edge = edgeIterator.next();
                if (edge.type() != EdgeLiteral.TYPE) {
                    nonBasicEdges.add(edge);
                    continue;
                }
                Vertex destination = edge.destination();
                graphics.drawLine(x * vertexSize + offset, y * vertexSize + offset,
                        (destination.x() - minX) * vertexSize + offset, (destination.y() - minY) * vertexSize + offset);
            }
        }
        Stroke oldStroke = graphics.getStroke();
        graphics.setPaint(Color.BLUE);
        graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{vertexSize}, 0));
        for (Edge<V> edge : nonBasicEdges) {
            Vertex origin = edge.origin();
            Vertex destination = edge.destination();
            graphics.drawLine((origin.x() - minX) * vertexSize + offset, (origin.y() - minY) * vertexSize + offset,
                    (destination.x() - minX) * vertexSize + offset, (destination.y() - minY) * vertexSize + offset);
        }
        graphics.setStroke(oldStroke);

        for (Path<V> p : paths) {
            for (Edge<V> edge : p.getEdges()) {
                Vertex origin = edge.origin();
                Vertex destination = edge.destination();
                if (edge.type() == 0) {
                    graphics.setPaint(Color.YELLOW);
                } else {
                    graphics.setPaint(Color.RED);
                }
                graphics.drawLine((origin.x() - minX) * vertexSize + offset, (origin.y() - minY) * vertexSize + offset,
                        (destination.x() - minX) * vertexSize + offset, (destination.y() - minY) * vertexSize + offset);
            }
        }

        graphics.dispose();
        return image;
    }

    public static <V extends Vertex> void renderToFile(Graph<V> graph, int plane, int vertexSize, File imageFile, Path<V>... paths) throws IOException {
        if (imageFile.exists()) {
            imageFile.delete();
        }
        ImageIO.write(renderToImage(graph, plane, vertexSize, paths), "png", imageFile);
    }
}
