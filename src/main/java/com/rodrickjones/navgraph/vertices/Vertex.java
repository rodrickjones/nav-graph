package com.rodrickjones.navgraph.vertices;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Vertex {
    private final int x;
    private final int y;
    private final int z;

    public Vertex(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vertex(int hashCode) {
        this(hashCode & 16383, hashCode >> 14 & 16383, hashCode >> 28 & 3);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Vertex derive(int dx, int dy, int dz) {
        return new Vertex(x + dx, y + dy, z + dz);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return x == vertex.x && y == vertex.y && z == vertex.z;
    }

    @Override
    public int hashCode() {
        return hashCode(x, y, z);
    }

    @Override
    public String toString() {
        return "Vertex{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public void writeToDataStream(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(hashCode());
    }

    public static Vertex readFromDataStream(DataInputStream dataInputStream) throws IOException {
        return new Vertex(dataInputStream.readInt());
    }

    public static int hashCode(int x, int y, int z) {
        return z << 28 | y << 14 | x;
    }
}
