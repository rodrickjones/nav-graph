package com.rodrickjones.navgraph.util;

import com.rodrickjones.navgraph.Graph;
import com.rodrickjones.navgraph.edges.Edge;
import com.rodrickjones.navgraph.vertices.Vertex;
import com.rodrickjones.navgraph.edges.BasicEdge;
import com.rodrickjones.navgraph.pathfinding.Path;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GraphRenderer {

    public static BufferedImage renderToImage(Graph graph, int plane, int vertexSize, Path... paths) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = 0;
        int maxY = 0;
        for (Vertex vertex : graph.getVertices()) {
            int x = vertex.getX();
            int y = vertex.getY();
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
        for (Vertex vertex : graph.getVertices()) {
            if (vertex.getZ() != plane) {
                continue;
            }
            Collection<Edge> edges = graph.getEdges(vertex);
            int x = vertex.getX() - minX;
            int y = vertex.getY() - minY;

            graphics.setPaint(vertexFill);
            graphics.fillRect(x * vertexSize, y * vertexSize, vertexSize, vertexSize);
//            graphics.setPaint(vertexBorder);
//            graphics.drawRect(x * vertexSize, y * vertexSize, vertexSize, vertexSize);
            graphics.setPaint(wall);
            Set<String> diff = edges == null ? Collections.emptySet() : edges.stream().filter(e -> e.getType() == BasicEdge.TYPE)
                    .map(Edge::getDestination)
                    .map(d -> (d.getX() - vertex.getX()) + ", " + (d.getY() - vertex.getY()))
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
        List<Edge> nonBasicEdges = new ArrayList<>(10000);
        graphics.setPaint(Color.GREEN);
        for (Vertex vertex : graph.getVertices()) {
            if (vertex.getZ() != plane) {
                continue;
            }
            Collection<Edge> edges = graph.getEdges(vertex);
            if (edges == null) {
                continue;
            }
            int x = vertex.getX() - minX;
            int y = vertex.getY() - minY;
            for (Edge edge : edges) {
                if (edge.getType() != BasicEdge.TYPE) {
                    nonBasicEdges.add(edge);
                    continue;
                }
                Vertex destination = edge.getDestination();
                graphics.drawLine(x * vertexSize + offset, y * vertexSize + offset,
                        (destination.getX() - minX) * vertexSize + offset, (destination.getY() - minY) * vertexSize + offset);
            }
        }
        Stroke oldStroke = graphics.getStroke();
        graphics.setPaint(Color.BLUE);
        graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{vertexSize}, 0));
        for (Edge edge : nonBasicEdges) {
            Vertex origin = edge.getOrigin();
            Vertex destination = edge.getDestination();
            graphics.drawLine((origin.getX() - minX) * vertexSize + offset, (origin.getY() - minY) * vertexSize + offset,
                    (destination.getX() - minX) * vertexSize + offset, (destination.getY() - minY) * vertexSize + offset);
        }
        graphics.setStroke(oldStroke);

        for (Path p : paths) {
            for (Edge edge : p.getEdges()) {
                Vertex origin = edge.getOrigin();
                Vertex destination = edge.getDestination();
                if (edge.getType() == 0) {
                    graphics.setPaint(Color.YELLOW);
                } else {
                    graphics.setPaint(Color.RED);
                }
                graphics.drawLine((origin.getX() - minX) * vertexSize + offset, (origin.getY() - minY) * vertexSize + offset,
                        (destination.getX() - minX) * vertexSize + offset, (destination.getY() - minY) * vertexSize + offset);
            }
        }

        graphics.dispose();
        return image;
    }

    public static void renderToFile(Graph graph, int plane, int vertexSize, File imageFile, Path... paths) throws IOException {
        if (imageFile.exists()) {
            imageFile.delete();
        }
        ImageIO.write(renderToImage(graph, plane, vertexSize, paths), "png", imageFile);
    }
}
