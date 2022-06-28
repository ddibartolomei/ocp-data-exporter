package ocpdataexporter.retriever;

import ocpdataexporter.client.ClusterClient;
import ocpdataexporter.exporter.StatefulSetDataExporter;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;

import java.util.*;
import java.util.stream.Collectors;

public class StatefulSetRetriever extends WorkloadRetriever {

    private StatefulSetDataExporter statefulSetDataExporter;

    private ContainerRetriever containerRetriever;

    public StatefulSetRetriever(ClusterClient client, StatefulSetDataExporter statefulSetDataExporter, ContainerRetriever containerRetriever) {
        super(client);
        this.statefulSetDataExporter = statefulSetDataExporter;
        this.containerRetriever = containerRetriever;
    }

    public void retrieveData(StatefulSet statefulSet) {
        String wName = statefulSet.getMetadata().getName();

        String wNamespace = statefulSet.getMetadata().getNamespace();
        System.out.println("Retrieving statefulset " + wNamespace + "/" + wName);

        String wKind = statefulSet.getKind();

        String wStrategy = statefulSet.getSpec().getUpdateStrategy().getType();

        // Get services bound to this workload
        Map<String, String> wSelectorMap = (statefulSet.getSpec().getSelector()!=null && statefulSet.getSpec().getSelector().getMatchLabels()!=null) ? statefulSet.getSpec().getSelector().getMatchLabels() : new HashMap<>();
        String[] wMatchedServicesArray = getMatchingServicesDescriptors(wNamespace, wSelectorMap);
        // For statefulsets, if no match has been found based on .spec.selector.matchLabels, try again with .spec.template.metadata.labels
        if (wMatchedServicesArray.length == 0) {
            wSelectorMap = statefulSet.getSpec().getTemplate().getMetadata().getLabels();
            wMatchedServicesArray = getMatchingServicesDescriptors(statefulSet.getMetadata().getNamespace(), wSelectorMap);
        }

        int wReplicas = statefulSet.getSpec().getReplicas();

        String wHasAffinity = safeNullableObjectAsBooleanString(statefulSet.getSpec().getTemplate().getSpec().getAffinity(), "YES", "");

        // Helm
        String wUseHelm = "";
        List<String> wHelmData = new ArrayList<>();
        if (notNull(statefulSet.getMetadata().getLabels())) {
            String helmChartName = statefulSet.getMetadata().getLabels()!=null ? statefulSet.getMetadata().getLabels().get("helm.sh/chart") : null;
            if (helmChartName!=null) {
                wUseHelm = "YES";
                wHelmData.add("helm.sh/chart: "+ helmChartName);
                if (notNull(statefulSet.getMetadata().getAnnotations())) {
                    wHelmData.add("meta.helm.sh/release-name: " + safeNullable(statefulSet.getMetadata().getAnnotations().get("meta.helm.sh/release-name"), ""));
                    wHelmData.add("meta.helm.sh/release-namespace: " + safeNullable(statefulSet.getMetadata().getAnnotations().get("meta.helm.sh/release-namespace"), ""));
                }
            }
        }

        String restartPolicy = safeNullable(statefulSet.getSpec().getTemplate().getSpec().getRestartPolicy());

        String[] securityContextArray = getSecurityContext(statefulSet.getSpec().getTemplate().getSpec().getSecurityContext());

        List<Container> wContainers = statefulSet.getSpec().getTemplate().getSpec().getContainers();

        // Retrieve volumes from .spec.volumes and collect .env.valueFrom/.envFrom refs to configmaps/secrets and add them to the whole list of referred configmaps/secrets
        ConfigMapsSecretsPVCsCollection configMapsSecretsPVCsCollection = collectConfigMapsSecretsAndPVCs(statefulSet.getSpec().getTemplate().getSpec().getVolumes(), wContainers);

        // Retrieve also volume templates .spec.volumeClaimTemplates for statefulsets
        List<String> pvcTemplatesList = statefulSet.getSpec().getVolumeClaimTemplates().stream().map(pvc -> pvc.getMetadata().getName()).collect(Collectors.toList());
        configMapsSecretsPVCsCollection.wPVCsList.addAll(pvcTemplatesList);

        statefulSetDataExporter.exportStatefulSetData(
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
