package ocpdataexporter.exporter;

public interface NamespaceDataExporter {

    void exportNamespace(String namespace, int workloadCount, String[] namespaceData);

    void exportNamespaceWorkloadData(String namespace);

    void exportNamespaceBatchWorkloadData(String namespace);

    void exportNamespaceServiceData(String namespace);

    void exportNamespacePVCData(String namespace);

    void exportNamespaceHPAData(String namespace);

    void exportNamespaceBuildConfigData(String namespace);

    void exportNamespaceImageStreamData(String namespace);
}
