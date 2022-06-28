package ocpdataexporter.retriever;

import ocpdataexporter.client.ClusterClient;
import ocpdataexporter.exporter.ContainerDataExporter;
import ocpdataexporter.exporter.WorkloadType;

public class BatchContainerRetriever extends ContainerRetriever {

    public BatchContainerRetriever(ClusterClient client, ContainerDataExporter containerDataExporter) {
        super(client, containerDataExporter, WorkloadType.BATCH_WORKLOAD);
    }
}
