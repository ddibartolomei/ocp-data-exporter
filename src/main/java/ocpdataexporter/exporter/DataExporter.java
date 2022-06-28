package ocpdataexporter.exporter;

import java.util.Map;

public abstract class DataExporter implements NamespaceDataExporter, DeploymentDataExporter, DeploymentConfigDataExporter, StatefulSetDataExporter, DaemonSetDataExporter, ContainerDataExporter, ServiceDataExporter, PVCDataExporter, JobDataExporter, HPADataExporter, BuildConfigDataExporter, ImageStreamDataExporter {

    protected final String[] namespacesHeaderFields = {"Namespace", "Workload Count", "Data" };
    protected final String[] workloadsHeaderFields = {"Namespace", "Kind", "Name", "Strategy Type", "Replicas", "Has Affinity", "Use Helm", "Helm Data", "Selectors", "Services name/type/[ports]", "Configmaps", "Secrets", "PVCs", "Other volumes", "Security Context values", "Restart Policy", "Container Count", "Container Name", "Ports name/protocol/number", "CPU Requests (mcore)", "Mem Requests", "CPU Limits (mcore)", "Mem Limits", "Liveness Probe", "Readiness Probe", "Image pull policy", "Image"}; // , "Container Security Context"};
    protected final String[] servicesHeaderFields = {"Namespace", "Name", "Type", "Selectors", "Ports", "External IPs", "Routes name/targetPort/weight/host/TLS-termination"};
    protected final String[] pvcHeaderFields = {"Namespace", "Name", "Status", "Capacity", "Access Mode", "Storage Class Name", "Volume Mode", "Volume Name"};
    protected final String[] batchWorkloadsHeaderFields = {"Namespace", "Kind", "Name", "Backoff Limit", "Active Deadline secs", "Parallelism", "TTL secs After Finished", "Completions", "Schedule", "Security Context values", "Restart Policy", "Container Count", "Container Name", "Ports name/protocol/number", "CPU Requests", "Mem Requests", "CPU Limits", "Mem Limits", "Liveness Probe", "Readiness Probe", "Image pull policy", "Image"};
    protected final String[] hpaHeaderFields = {"Namespace", "Name", "Target Kind", "Target Name", "Min replica", "Max Replica", "Target CPU Utilization %"};

    protected final String[] buildConfigHeaderFields = {"Namespace", "Name", "Source Type", "Source Data","Strategy Type", "Output Type", "Output Data"};

    protected final String[] imageStreamHeaderFields = {"Namespace", "Name", "Tags (id/kind[image])"};

    protected DataExporter() {
    }

    public abstract void exportHeaderData();

    public abstract void finalizeExport() throws DataExportException;

    protected String stringArraySerializer(String[] array, String separator) {
        String result = "";
        if (array!=null && array.length>0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : array) {
                stringBuilder.append(s + separator);
            }
            result = stringBuilder.delete(stringBuilder.length() - separator.length(), stringBuilder.length()).toString();
        }
        return result;
    }

    protected String stringStringMapSerializer(Map<String,String> map, String separator) {
        String result = "";
        if (map!=null && map.size()>0) {
            StringBuilder stringBuilder = new StringBuilder();
            map.entrySet().stream().forEach(entry -> {
                stringBuilder.append(entry.getKey() + ": " + entry.getValue() + separator);
            });
            result = stringBuilder.delete(stringBuilder.length() - separator.length(), stringBuilder.length()).toString();
        }
        return result;
    }
}
