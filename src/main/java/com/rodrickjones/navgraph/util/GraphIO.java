package com.rodrickjones.navgraph.util;

import com.rodrickjones.navgraph.edges.Edge;
import com.rodrickjones.navgraph.Graph;
import com.rodrickjones.navgraph.SimpleGraph;
import com.rodrickjones.navgraph.edges.EdgeReader;
import com.rodrickjones.navgraph.requirements.RequirementReader;
import com.rodrickjones.navgraph.vertices.Vertex;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class GraphIO {
    //    public final static String VERSION = "1.0.0";
    public final static Charset CHARSET = StandardCharsets.US_ASCII;

    public static <T extends Graph> T readFromZip(File zipFile, Class<T> tClass,
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
                            graph.addVertex(Vertex.readFromDataStream(dataIn));
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
            int vertexCount = graph.getVertexCount();
            verticesEntry.setComment(String.valueOf(vertexCount));
            zipOut.putNextEntry(verticesEntry);
            dos.writeInt(vertexCount);
            for (Vertex vertex : graph.getVertices()) {
                vertex.writeToDataStream(dos);
            }

            ZipEntry edgesEntry = new ZipEntry("edges");
            int edgeCount = graph.getEdgeCount();
            edgesEntry.setComment(String.valueOf(edgeCount));
            zipOut.putNextEntry(edgesEntry);
            dos.writeInt(edgeCount);
            for (Edge edge : graph.getEdges()) {
                edge.writeToDataStream(dos);
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
