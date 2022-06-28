package ocpdataexporter.retriever;

import ocpdataexporter.client.ClusterClient;
import ocpdataexporter.exporter.BuildConfigDataExporter;
import io.fabric8.openshift.api.model.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public class BuildConfigRetriever extends DataRetriever {

    private BuildConfigDataExporter buildConfigDataExporter;

    public BuildConfigRetriever(ClusterClient client, BuildConfigDataExporter buildConfigDataExporter) {
        super(client);
        this.buildConfigDataExporter = buildConfigDataExporter;
    }

    public void retrieveData(BuildConfig buildConfig) {
        String name = buildConfig.getMetadata().getName();

        String namespace = buildConfig.getMetadata().getNamespace();
        System.out.println("Retrieving build config " + namespace + "/" + name);

        // Source
        List<String> sourceData = new ArrayList<>();
        String sourceType = safeNullable(buildConfig.getSpec().getSource().getType(), "undefined");
        if (sourceType.equals("Git")) {
            if (notNull(buildConfig.getSpec().getSource().getGit())) {
                sourceData.add("git.uri: " + safeNullable(buildConfig.getSpec().getSource().getGit().getUri(), "undefined"));
            }
            if (notNull(buildConfig.getSpec().getSource().getDockerfile())) {
                sourceData.add("dockerfile: \"" + buildConfig.getSpec().getSource().getDockerfile().substring(1, 50) + "...\"");
            }
        }
        else if (sourceType.equals("Dockerfile")) {
            if (notNull(buildConfig.getSpec().getSource().getDockerfile())) {
                sourceData.add("dockerfile: \"" + buildConfig.getSpec().getSource().getDockerfile().substring(1, 50) + "...\"");
            }
        }

        // Strategy
        String strategyType = "undefined";
        if (notNull(buildConfig.getSpec().getStrategy())) {
            strategyType = safeNullable(buildConfig.getSpec().getStrategy().getType(), "undefined");
        }

        // Output
        String outputType = "Undefined";
        String outputData = "Undefined";
        if (notNull(buildConfig.getSpec().getOutput()) && notNull(buildConfig.getSpec().getOutput().getTo())) {
            outputType = safeNullable(buildConfig.getSpec().getOutput().getTo().getKind(), "Undefined");
            outputData = safeNullable(buildConfig.getSpec().getOutput().getTo().getName(), "Undefined");
        }

        buildConfigDataExporter.exportBuildConfigData(
                namespace,
                name,
                sourceType,
                sourceData.stream().toArray(String[]::new),
                strategyType,
                outputType,
                outputData);

    }
}
