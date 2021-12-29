package com.rodrickjones.navgraph.requirement;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class BasicRequirementContext implements RequirementContext {
    private Map<String, Object> map;

    public BasicRequirementContext(InputStream in) throws IOException {
        try (DataInputStream dis = new DataInputStream(in)) {
            int size = dis.readInt();
            map = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                String key = dis.readUTF();
                String type = dis.readUTF();
                Object value;
                switch (type) {
                    case "Integer":
                        value = dis.readInt();
                        break;
                    case "String":
                        value = dis.readUTF();
                        break;
                    default:
                        throw new UnsupportedOperationException("Unsupported context value type for deserialization: " + type);
                }
                map.put(key, value);
            }
        }
    }

    @Override
    public void writeToDataStream(DataOutputStream dos) throws IOException {
        dos.writeInt(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            dos.writeUTF(entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Integer) {
                dos.writeUTF("Integer");
                dos.writeInt((Integer) value);
            } else if (value instanceof String) {
                dos.writeUTF("String");
                dos.writeUTF((String) value);
            } else {
                throw new UnsupportedOperationException("Unsupported context value type for serialization: " + value.getClass().getSimpleName());
            }
        }
    }

    public <T> T getContextValue(String key, Class<T> tClass) {
        return tClass.cast(map.get(key));
    }
}
