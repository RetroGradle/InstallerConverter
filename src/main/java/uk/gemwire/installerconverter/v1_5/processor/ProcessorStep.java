package uk.gemwire.installerconverter.v1_5.processor;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.util.maven.Artifact;

public record ProcessorStep(Artifact jar, Artifact[] classpath, String[] args, Map<String, String> outputs, List<String> sides) {

    public static ProcessorStep client(Processor processor, String... args) {
        return client(processor, Map.of(), args);
    }

    public static ProcessorStep client(Processor processor, Map<String, String> outputs, String... args) {
        return new ProcessorStep(processor.jar(), processor.classpath(), args, outputs, List.of("client"));
    }

    public static ProcessorStep server(Processor processor, String... args) {
        return server(processor, Map.of(), args);
    }

    public static ProcessorStep server(Processor processor, Map<String, String> outputs, String... args) {
        return new ProcessorStep(processor.jar(), processor.classpath(), args, outputs, List.of("server"));
    }

    public static ProcessorStep both(Processor processor, String... args) {
        return both(processor, Map.of(), args);
    }

    public static ProcessorStep both(Processor processor, Map<String, String> outputs, String... args) {
        return new ProcessorStep(processor.jar(), processor.classpath(), args, outputs, List.of());
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
