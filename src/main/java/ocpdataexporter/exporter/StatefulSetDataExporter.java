package ocpdataexporter.exporter;

import java.util.Map;

public interface StatefulSetDataExporter {

    void exportStatefulSetData(String namespace, String sKind, String sName, String sStrategy, int sReplicas, String sHasAffinity, String sUseHelm, String[] sHelmData, Map<String, String> sSelectors, String[] sMatchedServices, String[] sConfigmaps, String[] sSecrets, String[] sPvcs, String[] sOtherVolumes, String[] sSecurityContextValues, String sRestartPolicy, int sContainerCount);
}
