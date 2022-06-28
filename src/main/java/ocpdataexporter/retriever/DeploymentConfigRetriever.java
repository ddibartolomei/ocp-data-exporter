package ocpdataexporter.retriever;

import ocpdataexporter.client.ClusterClient;
import ocpdataexporter.exporter.DeploymentConfigDataExporter;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.openshift.api.model.DeploymentConfig;

import java.util.*;

public class DeploymentConfigRetriever extends WorkloadRetriever {

    private DeploymentConfigDataExporter deploymentConfigDataExporter;

    private ContainerRetriever containerRetriever;

    public DeploymentConfigRetriever(ClusterClient client, DeploymentConfigDataExporter deploymentConfigDataExporter, ContainerRetriever containerRetriever) {
        super(client);
        this.deploymentConfigDataExporter = deploymentConfigDataExporter;
        this.containerRetriever = containerRetriever;
    }

    public void retrieveData(DeploymentConfig deploymentConfig) {
        String wName = deploymentConfig.getMetadata().getName();

        String wNamespace = deploymentConfig.getMetadata().getNamespace();
        System.out.println("Retrieving deployment config " + wNamespace + "/" + wName);

        String wKind = deploymentConfig.getKind();

        String wStrategy = deploymentConfig.getSpec().getStrategy().getType();

        // Get services bound to this workload
        Map<String, String> wSelectorMap = deploymentConfig.getSpec().getSelector()!=null ? deploymentConfig.getSpec().getSelector() : new HashMap<>();
        String[] wMatchedServicesArray = getMatchingServicesDescriptors(wNamespace, wSelectorMap);

        int wReplicas = deploymentConfig.getSpec().getReplicas();

        String wHasAffinity = safeNullableObjectAsBooleanString(deploymentConfig.getSpec().getTemplate().getSpec().getAffinity(), "YES", "");

        // Helm
        String wUseHelm = "";
        List<String> wHelmData = new ArrayList<>();
        if (notNull(deploymentConfig.getMetadata().getLabels())) {
            String helmChartName = deploymentConfig.getMetadata().getLabels() != null ? deploymentConfig.getMetadata().getLabels().get("helm.sh/chart") : null;
            if (helmChartName != null) {
                wUseHelm = "YES";
                wHelmData.add("helm.sh/chart: " + helmChartName);
                if (notNull(deploymentConfig.getMetadata().getAnnotations())) {
                    wHelmData.add("meta.helm.sh/release-name: " + safeNullable(deploymentConfig.getMetadata().getAnnotations().get("meta.helm.sh/release-name"), ""));
                    wHelmData.add("meta.helm.sh/release-namespace: " + safeNullable(deploymentConfig.getMetadata().getAnnotations().get("meta.helm.sh/release-namespace"), ""));
                }
            }
        }

        String restartPolicy = safeNullable(deploymentConfig.getSpec().getTemplate().getSpec().getRestartPolicy());

        String[] securityContextArray = getSecurityContext(deploymentConfig.getSpec().getTemplate().getSpec().getSecurityContext());

        List<Container> wContainers = deploymentConfig.getSpec().getTemplate().getSpec().getContainers();

        // Retrieve volumes from .spec.volumes and collect .env.valueFrom/.envFrom refs to configmaps/secrets and add them to the whole list of referred configmaps/secrets
        ConfigMapsSecretsPVCsCollection configMapsSecretsPVCsCollection = collectConfigMapsSecretsAndPVCs(deploymentConfig.getSpec().getTemplate().getSpec().getVolumes(), wContainers);

        deploymentConfigDataExporter.exportDeploymentConfigData(
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
