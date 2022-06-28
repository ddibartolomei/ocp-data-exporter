package ocpdataexporter.exporter;

public interface HPADataExporter {

    void exportHPAData(String namespace, String name, String targetKind, String targetName, int minReplica, int maxReplica, int targetCPUUtilizationPercentage);
}
