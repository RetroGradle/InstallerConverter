package uk.gemwire.installerconverter.v1_5.processor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.util.maven.Artifact;

public class ProcessorStep {

    private final Artifact jar;
    private final Artifact[] classpath;
    private final String[] args;
    private final Map<String, String> outputs;
    private final List<String> sides;

    public ProcessorStep(Artifact jar, Artifact[] classpath, String[] args, Map<String, String> outputs, List<String> sides) {
        this.jar = jar;
        this.classpath = classpath;
        this.args = args;
        this.outputs = outputs;
        this.sides = sides;
    }

    public Artifact jar() {
        return jar;
    }

    public Artifact[] classpath() {
        return classpath;
    }

    public String[] args() {
        return args;
    }

    public Map<String, String> outputs() {
        return outputs;
    }

    public List<String> sides() {
        return sides;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessorStep that = (ProcessorStep) o;
        return Objects.equals(jar, that.jar) && Arrays.equals(classpath, that.classpath) && Arrays.equals(args, that.args)
            && Objects.equals(outputs, that.outputs) && Objects.equals(sides, that.sides);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(jar, outputs, sides);
        result = 31 * result + Arrays.hashCode(classpath);
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }

    @Override
    public String toString() {
        return "ProcessorStep{" +
            "jar=" + jar +
            ", classpath=" + Arrays.toString(classpath) +
            ", args=" + Arrays.toString(args) +
            ", outputs=" + outputs +
            ", sides=" + sides +
            '}';
    }

}
