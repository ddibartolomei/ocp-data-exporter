package ocpdataexporter.retriever;

import ocpdataexporter.client.ClusterClient;
import ocpdataexporter.exporter.HPADataExporter;
import io.fabric8.kubernetes.api.model.autoscaling.v1.HorizontalPodAutoscaler;

public class HPARetriever extends DataRetriever {

    private HPADataExporter hpaDataExporter;

    public HPARetriever(ClusterClient client, HPADataExporter hpaDataExporter) {
        super(client);
        this.hpaDataExporter = hpaDataExporter;
    }

    public void retrieveData(HorizontalPodAutoscaler hpa) {
        String name = hpa.getMetadata().getName();

        String namespaceName = hpa.getMetadata().getNamespace();
        System.out.println("Retrieving hpa " + namespaceName + "/" + name);

        String targetKind = safeNullable(hpa.getSpec().getScaleTargetRef().getKind());
        String targetName = safeNullable(hpa.getSpec().getScaleTargetRef().getName());
        int minReplica = hpa.getSpec().getMinReplicas();
        int maxReplica = hpa.getSpec().getMaxReplicas();
        int targetCPUUtilizationPercentage = hpa.getSpec().getTargetCPUUtilizationPercentage();

        hpaDataExporter.exportHPAData(namespaceName, name, targetKind, targetName, minReplica, maxReplica, targetCPUUtilizationPercentage);
    }
}
