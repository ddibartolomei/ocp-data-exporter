package ocpdataexporter.exporter;

import java.util.Map;

public interface DeploymentConfigDataExporter {
    void exportDeploymentConfigData(String namespace, String dKind, String dName, String dStrategy, int dReplicas, String dHasAffinity, String dUseHelm, String[] dHelmData, Map<String, String> dSelectors, String[] dMatchedServices, String[] dConfigmaps, String[] dSecrets, String[] dPvcs, String[] dOtherVolumes, String[] dSecurityContextValues, String dRestartPolicy, int dContainerCount);
}
