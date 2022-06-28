package ocpdataexporter.exporter;

import java.util.Map;

public interface DaemonSetDataExporter {
    void exportDaemonSetData(String namespace, String dKind, String dName, String dStrategy, int dReplicas, String dHasAffinity, String dUseHelm, String[] dHelmData, Map<String, String> dSelectors, String[] dMatchedServices, String[] dConfigmaps, String[] dSecrets, String[] dPvcs, String[] dOtherVolumes, String[] dSecurityContextValues, String dbRestartPolicy, int dContainerCount);
}
