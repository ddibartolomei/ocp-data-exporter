package ocpdataexporter.exporter;

public interface BuildConfigDataExporter {
    void exportBuildConfigData(String namespace, String name, String sourceType, String[] sourceData, String strategyType, String outputType, String outputData);
}
