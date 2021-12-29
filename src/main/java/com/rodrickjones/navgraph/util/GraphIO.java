package com.rodrickjones.navgraph.util;

import com.rodrickjones.navgraph.edge.Edge;
import com.rodrickjones.navgraph.edge.EdgeLiteral;
import com.rodrickjones.navgraph.edge.EdgeReader;
import com.rodrickjones.navgraph.graph.SimpleGraph;
import com.rodrickjones.navgraph.requirement.RequirementReader;
import com.rodrickjones.navgraph.vertex.Vertex;
import com.rodrickjones.navgraph.vertex.VertexLiteral;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class GraphIO {
    //    public final static String VERSION = "1.0.0";
    public final static Charset CHARSET = StandardCharsets.US_ASCII;

    public static <T extends SimpleGraph> T readFromZip(File zipFile, Class<T> tClass,
                                                  EdgeReader edgeReader,
                                                  RequirementReader requirementReader) {
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile), CHARSET);
             DataInputStream dataIn = new DataInputStream(zipIn)) {
            T graph = tClass.getConstructor().newInstance();
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                switch (entry.getName()) {
//                    case "version":
//                        String version = dataIn.readUTF();
//                        if (!VERSION.equals(version)) {
//                            throw new IllegalStateException("Version mismatch: Zip=" + version + ", GraphIO=" + VERSION);
//                        }
//                        break;
                    case "vertices":
                        int vertexCount = dataIn.readInt();
                        for (int i = 0; i < vertexCount; i++) {
                            graph.addVertex(VertexLiteral.readFromDataStream(dataIn));
                        }
                        break;
                    case "edges":
                        int edgeCount = dataIn.readInt();
                        for (int i = 0; i < edgeCount; i++) {
                            graph.addEdge(edgeReader.readEdge(dataIn, requirementReader));
                        }
                        break;
                }
            }
            return graph;
        } catch (IOException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void writeToZip(SimpleGraph graph, File zipFile, int compressionLevel) {
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile), CHARSET);
             DataOutputStream dos = new DataOutputStream(zipOut)) {
            zipOut.setLevel(compressionLevel);

//            ZipEntry versionEntry = new ZipEntry("version");
//            versionEntry.setComment(VERSION);
//            zipOut.putNextEntry(versionEntry);
//            dataOut.writeUTF(VERSION);

            ZipEntry verticesEntry = new ZipEntry("vertices");
            int vertexCount = graph.vertexCount();
            verticesEntry.setComment(String.valueOf(vertexCount));
            zipOut.putNextEntry(verticesEntry);
            dos.writeInt(vertexCount);
            Iterator<Vertex> vertexIterator = graph.vertices().iterator();
            while (vertexIterator.hasNext()) {
                Vertex vertex = vertexIterator.next();
                // FIXME
                ((VertexLiteral) vertex).writeToDataStream(dos);
            }

            ZipEntry edgesEntry = new ZipEntry("edges");
            int edgeCount = graph.edgeCount();
            edgesEntry.setComment(String.valueOf(edgeCount));
            zipOut.putNextEntry(edgesEntry);
            dos.writeInt(edgeCount);
            Iterator<Edge> edgeIterator = graph.edges().iterator();
            while (edgeIterator.hasNext()) {
                Edge edge = edgeIterator.next();
                // FIXME
                ((EdgeLiteral) edge).writeToDataStream(dos);
            }

            zipOut.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToZip(SimpleGraph graph, File zipFile) {
        writeToZip(graph, zipFile, Deflater.BEST_COMPRESSION);
    }

}
