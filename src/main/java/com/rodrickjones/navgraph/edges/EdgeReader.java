package com.rodrickjones.navgraph.edges;

import com.rodrickjones.navgraph.requirements.RequirementReader;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EdgeReader {
    private static final EdgeReader DEFAULT = new EdgeReader() {
        @Override
        public void registerParser(int type, EdgeParser edgeParser) {
            throw new UnsupportedOperationException("Cannot register custom readers to the Default RequirementReaders");
        }
    };

    private final Map<Integer, EdgeParser> edgeParserMap = new HashMap<>();

    public EdgeReader() {
        edgeParserMap.put(BasicEdge.TYPE, BasicEdge::new);
    }

    public void registerParser(int type, EdgeParser edgeParser) {
        if (type < 0) {
            throw new IllegalArgumentException("Type must be positive");
        } else if (edgeParserMap.containsKey(type)) {
            throw new IllegalStateException("An EdgeReader for type " + type + " is already registered");
        }
        edgeParserMap.put(type, edgeParser);
    }

    public EdgeParser getParser(int type) {
        return edgeParserMap.get(type);
    }

    public Edge readEdge(DataInputStream in, RequirementReader requirementReader) throws IOException {
        int type = in.readInt();
        EdgeParser reader = getParser(type);
        if (reader == null) {
            throw new IllegalStateException("Unsupported Edge type: " + type);
        }
        return reader.parseEdge(in, requirementReader);
    }

    public static EdgeReader getDefault() {
        return DEFAULT;
    }
}
