package ocpdataexporter.retriever;

import ocpdataexporter.client.ClusterClient;
import ocpdataexporter.exporter.DaemonSetDataExporter;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.apps.DaemonSet;

import java.util.*;

public class DaemonSetRetriever extends WorkloadRetriever {

    private DaemonSetDataExporter daemonSetDataExporter;

    private ContainerRetriever containerRetriever;

    public DaemonSetRetriever(ClusterClient client, DaemonSetDataExporter daemonSetDataExporter, ContainerRetriever containerRetriever) {
        super(client);
        this.daemonSetDataExporter = daemonSetDataExporter;
        this.containerRetriever = containerRetriever;
    }

    public void retrieveData(DaemonSet daemonSet) {
        String wName = daemonSet.getMetadata().getName();

        String wNamespace = daemonSet.getMetadata().getNamespace();
        System.out.println("Retrieving daemonset " + wNamespace + "/" + wName);

        String wKind = daemonSet.getKind();

        String wStrategy = daemonSet.getSpec().getUpdateStrategy().getType();

        // Get services bound to this workload
        Map<String, String> wSelectorMap = (daemonSet.getSpec().getSelector()!=null && daemonSet.getSpec().getSelector().getMatchLabels()!=null) ? daemonSet.getSpec().getSelector().getMatchLabels() : new HashMap<>();
        String[] wMatchedServicesArray = getMatchingServicesDescriptors(wNamespace, wSelectorMap);

        int wReplicas = 1;

        String wHasAffinity = safeNullableObjectAsBooleanString(daemonSet.getSpec().getTemplate().getSpec().getAffinity(), "YES", "");

        // Helm
        String wUseHelm = "";
        List<String> wHelmData = new ArrayList<>();
        if (notNull(daemonSet.getMetadata().getLabels())) {
            String helmChartName = daemonSet.getMetadata().getLabels().get("helm.sh/chart");
            if (helmChartName != null) {
                wUseHelm = "YES";
                wHelmData.add("helm.sh/chart: " + helmChartName);
                if (notNull(daemonSet.getMetadata().getAnnotations())) {
                    wHelmData.add("meta.helm.sh/release-name: " + safeNullable(daemonSet.getMetadata().getAnnotations().get("meta.helm.sh/release-name"), ""));
                    wHelmData.add("meta.helm.sh/release-namespace: " + safeNullable(daemonSet.getMetadata().getAnnotations().get("meta.helm.sh/release-namespace"), ""));
                }
            }
        }

        String[] securityContextArray = getSecurityContext(daemonSet.getSpec().getTemplate().getSpec().getSecurityContext());

        String restartPolicy = safeNullable(daemonSet.getSpec().getTemplate().getSpec().getRestartPolicy());

        List<Container> wContainers = daemonSet.getSpec().getTemplate().getSpec().getContainers();

        // Retrieve volumes from .spec.volumes and collect .env.valueFrom/.envFrom refs to configmaps/secrets and add them to the whole list of referred configmaps/secrets
        ConfigMapsSecretsPVCsCollection configMapsSecretsPVCsCollection = collectConfigMapsSecretsAndPVCs(daemonSet.getSpec().getTemplate().getSpec().getVolumes(), wContainers);

        daemonSetDataExporter.exportDaemonSetData(
                wNamespace,
                wKind,
                wName,
                wStrategy,
                wReplicas,
                wHasAffinity,
                wUseHelm,
                wHelmData.stream().toArray(String[]::new),
                wSelectorMap,
                wMatchedServicesArray,
                configMapsSecretsPVCsCollection.wConfigMapsList.stream().toArray(String[]::new),
                configMapsSecretsPVCsCollection.wSecretsList.stream().toArray(String[]::new),
                configMapsSecretsPVCsCollection.wPVCsList.stream().toArray(String[]::new),
                configMapsSecretsPVCsCollection.wOtherVolumesList.stream().toArray(String[]::new),
                securityContextArray,
                restartPolicy,
                wContainers.size());

        // Retrieve containers specific data
        int index = 1;
        for (Container container : wContainers) {
            containerRetriever.retrieveData(wNamespace, container, index++, wContainers.size());
        }
    }
}
