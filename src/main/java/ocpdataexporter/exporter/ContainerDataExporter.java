package ocpdataexporter.exporter;

public interface ContainerDataExporter {

    void exportContainerData(WorkloadType workloadType, String namespace, int totalContainerCount, String cName, String cImagePullPolicy, String cImage, String[] cPorts, String cpuReq, String memReq, String cpuLim, String memLim, String livenessProbe, String readinessProbe); //, String[] cSecurityContextValues);
}