package com.rodrickjones.navgraph.vertex;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

@EqualsAndHashCode
@ToString
public class VertexLiteral implements Vertex {
    private final int x;
    private final int y;
    private final int z;

    public VertexLiteral(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int x() {
        return x;
    }

    @Override
    public int y() {
        return y;
    }

    @Override
    public int z() {
        return z;
    }

    public void writeToDataStream(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(x);
        dataOutputStream.writeInt(y);
        dataOutputStream.writeInt(z);
    }

    public static VertexLiteral readFromDataStream(DataInputStream dataInputStream) throws IOException {
        return new VertexLiteral(dataInputStream.readInt(), dataInputStream.readInt(), dataInputStream.readInt());
    }

    public static VertexLiteral derive(@NonNull Vertex base, int dx, int dy, int dz) {
        return new VertexLiteral(base.x() + dx, base.y() + dy, base.z() + dz);
    }
}
