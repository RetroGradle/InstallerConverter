package uk.gemwire.installerconverter.v1_5;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.util.maven.Artifact;

public record Processor(Artifact jar, Artifact[] classpath, String[] args, Map<String, String> outputs, List<String> sides) {

    //TODO: MINIMAL INTERFACE
    public static Processor of(Artifact jar, Artifact[] classpath, String[] args, Map<String, String> outputs, List<String> sides) {
        return new Processor(jar, classpath, args, outputs, sides);
    }

    public ObjectNode toNode(JsonNodeFactory factory) {
        ObjectNode node = factory.objectNode();

        node.put("jar", jar.asStringWithClassifier());
        if (classpath.length != 0) node.putPOJO("classpath", classpath);
        if (args.length != 0) node.putPOJO("args", args);
        if (outputs.size() != 0) node.putPOJO("outputs", outputs);
        if (!sides.isEmpty() && sides.size() != 2) node.putPOJO("sides", sides);

        return node;
    }

}
