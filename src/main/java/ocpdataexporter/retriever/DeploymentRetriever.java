package ocpdataexporter.retriever;

import ocpdataexporter.client.ClusterClient;
import ocpdataexporter.exporter.DeploymentDataExporter;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;

import java.util.*;

public class DeploymentRetriever extends WorkloadRetriever {

    private DeploymentDataExporter deploymentDataExporter;

    private ContainerRetriever containerRetriever;

    public DeploymentRetriever(ClusterClient client, DeploymentDataExporter deploymentDataExporter, ContainerRetriever containerRetriever) {
        super(client);
        this.deploymentDataExporter = deploymentDataExporter;
        this.containerRetriever = containerRetriever;
    }

    public void retrieveData(Deployment deployment) {
        String wName = deployment.getMetadata().getName();

        String wNamespace = deployment.getMetadata().getNamespace();
        System.out.println("Retrieving deployment " + wNamespace + "/" + wName);

        String wKind = deployment.getKind();

        String wStrategy = deployment.getSpec().getStrategy().getType();

        // Get services bound to this workload
        Map<String, String> wSelectorMap = (deployment.getSpec().getSelector()!=null && deployment.getSpec().getSelector().getMatchLabels()!=null) ? deployment.getSpec().getSelector().getMatchLabels() : new HashMap<>();
        String[] wMatchedServicesArray = getMatchingServicesDescriptors(wNamespace, wSelectorMap);

        int wReplicas = deployment.getSpec().getReplicas();

        String wHasAffinity = safeNullableObjectAsBooleanString(deployment.getSpec().getTemplate().getSpec().getAffinity(), "YES", "");

        // Helm
        String wUseHelm = "";
        List<String> wHelmData = new ArrayList<>();
        if (notNull(deployment.getMetadata().getLabels())) {
            String helmChartName = deployment.getMetadata().getLabels() != null ? deployment.getMetadata().getLabels().get("helm.sh/chart") : null;
            if (helmChartName != null) {
                wUseHelm = "YES";
                wHelmData.add("helm.sh/chart: " + helmChartName);
                if (notNull(deployment.getMetadata().getAnnotations())) {
                    wHelmData.add("meta.helm.sh/release-name: " + safeNullable(deployment.getMetadata().getAnnotations().get("meta.helm.sh/release-name"), ""));
                    wHelmData.add("meta.helm.sh/release-namespace: " + safeNullable(deployment.getMetadata().getAnnotations().get("meta.helm.sh/release-namespace"), ""));
                }
            }
        }

        String restartPolicy = safeNullable(deployment.getSpec().getTemplate().getSpec().getRestartPolicy());

        String[] securityContextArray = getSecurityContext(deployment.getSpec().getTemplate().getSpec().getSecurityContext());

        List<Container> wContainers = deployment.getSpec().getTemplate().getSpec().getContainers();

        // Retrieve volumes from .spec.volumes and collect .env.valueFrom/.envFrom refs to configmaps/secrets and add them to the whole list of referred configmaps/secrets
        ConfigMapsSecretsPVCsCollection configMapsSecretsPVCsCollection = collectConfigMapsSecretsAndPVCs(deployment.getSpec().getTemplate().getSpec().getVolumes(), wContainers);

        deploymentDataExporter.exportDeploymentData(
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
