package ocpdataexporter.retriever;

import ocpdataexporter.client.ClusterClient;
import ocpdataexporter.exporter.ContainerDataExporter;
import ocpdataexporter.exporter.WorkloadType;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ResourceRequirements;

import java.util.List;

public class ContainerRetriever extends WorkloadRetriever {

    private WorkloadType workloadType;

    private ContainerDataExporter containerDataExporter;

    public ContainerRetriever(ClusterClient client, ContainerDataExporter containerDataExporter, WorkloadType workloadType) {
        super(client);
        this.containerDataExporter = containerDataExporter;
        this.workloadType = workloadType;
    }

    public ContainerRetriever(ClusterClient client, ContainerDataExporter containerDataExporter) {
        this(client, containerDataExporter, WorkloadType.WORKLOAD);
    }

    public void retrieveData(String namespace, Container container, int containerIndex, int totalContainerCount) {
        String cName = container.getName();
        System.out.println("Retrieving container " + cName + " (" + containerIndex + "/" + totalContainerCount + ")");

        String cImage = container.getImage();

        List<ContainerPort> cPorts = container.getPorts();
        String[] cPortsArray = new String[cPorts.size()];
        for (int i = 0; i < cPorts.size(); i++) {
            cPortsArray[i] = getContainerPortDescriptor(cPorts.get(i));
        }

        ResourceRequirements resourceRequirements = container.getResources();
        String cCPURequests = normalizeCPUvalue(getResourceRequests(container, "cpu"));
        String cMemRequests = getResourceRequests(container, "memory");
        String cCPULimits = normalizeCPUvalue(getResourceLimits(container, "cpu"));
        String cMemLimits = getResourceLimits(container, "memory");

        // TODO check probes are different? And get more info
        String cHasLivenessProbe = safeNullableObjectAsBooleanString(container.getLivenessProbe(), "YES", "");
        String cHasReadinessProbe = safeNullableObjectAsBooleanString(container.getReadinessProbe(), "YES", "");

        String cImagePullPolicy = safeNullable(container.getImagePullPolicy());

//        String[] securityContextArray = getContainerSecurityContext(container.getSecurityContext());
//        containerDataExporter.exportContainerData(namespace, cName, cImage, cPortsArray, cCPURequests, cMemRequests, cCPULimits, cMemLimits, cHasLivenessProbe, cHasReadinessProbe, securityContextArray);

        containerDataExporter.exportContainerData(
                workloadType,
                namespace,
                totalContainerCount,
                cName,
                cImagePullPolicy,
                cImage,
                cPortsArray,
                cCPURequests,
                cMemRequests,
                cCPULimits,
                cMemLimits,
                cHasLivenessProbe,
                cHasReadinessProbe);
    }

    private String getResourceRequests(Container container, String resourceTypeName) {
        String valueString = "";
        if (container.getResources() != null &&
                container.getResources().getRequests() != null &&
                container.getResources().getRequests().get(resourceTypeName) != null) {
            valueString = container.getResources().getRequests().get(resourceTypeName).getAmount() + container.getResources().getRequests().get(resourceTypeName).getFormat();
        }
        return valueString;
    }

    private String getResourceLimits(Container container, String resourceTypeName) {
        String valueString = "";
        if (container.getResources() != null &&
                container.getResources().getLimits() != null &&
                container.getResources().getLimits().get(resourceTypeName) != null) {
            valueString = container.getResources().getLimits().get(resourceTypeName).getAmount() + container.getResources().getLimits().get(resourceTypeName).getFormat();
        }
        return valueString;
    }

    protected String getContainerPortDescriptor(ContainerPort containerPort) {
        String containerPortDescriptor = containerPort.getName()!=null ? containerPort.getName() + "/" : "";
        containerPortDescriptor += containerPort.getProtocol()!=null ? containerPort.getProtocol() + "/" : "";
        containerPortDescriptor += containerPort.getContainerPort()!=null ? containerPort.getContainerPort() : "";

        return containerPortDescriptor;
    }

    protected String normalizeCPUvalue(String value) {
        if (value!=null && !"".equals(value)) {
            if (!value.endsWith("m")) {
                return "" + (Integer.parseInt(value) * 1000);
            } else {
                return value.substring(0, value.length() - 1);
            }
        }
        else {
            return value;
        }
    }

}
