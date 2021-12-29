package com.rodrickjones.navgraph.requirement;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// TODO remove
public class RequirementReader {
    private static final RequirementReader DEFAULT = new RequirementReader() {
        @Override
        public void registerParser(int type, RequirementParser requirementParser) {
            throw new UnsupportedOperationException("Cannot register custom readers to the Default RequirementReaders");
        }
    };
    private final Map<Integer, RequirementParser> requirementParserMap = new HashMap<>();

    public RequirementReader() {
        requirementParserMap.put(OrRequirement.TYPE, in -> new OrRequirement(in, this));
        requirementParserMap.put(AndRequirement.TYPE, in -> new AndRequirement(in, this));
    }

    public static RequirementReader getDefault() {
        return DEFAULT;
    }

    public void registerParser(int type, RequirementParser requirementParser) {
        if (type < 0) {
            throw new IllegalArgumentException("Type must be positive");
        }
        else if (requirementParserMap.containsKey(type)) {
            throw new IllegalStateException("A reader for type " + type + " is already registered");
        }
        requirementParserMap.put(type, requirementParser);
    }

    public RequirementParser getParser(int type) {
        return requirementParserMap.get(type);
    }

    public Requirement readRequirement(DataInputStream in) throws IOException {
        int type = in.readInt();
        RequirementParser reader = getParser(type);
        if (reader == null) {
            throw new IllegalStateException("Unsupported requirement type: " + type);
        }
        return reader.parseRequirement(in);
    }

}
