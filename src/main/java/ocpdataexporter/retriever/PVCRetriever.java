package ocpdataexporter.retriever;

import ocpdataexporter.client.ClusterClient;
import ocpdataexporter.exporter.PVCDataExporter;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Quantity;

import java.util.*;

public class PVCRetriever extends DataRetriever {

    private PVCDataExporter pvcDataExporter;

    public PVCRetriever(ClusterClient client, PVCDataExporter pvcDataExporter) {
        super(client);
        this.pvcDataExporter = pvcDataExporter;
    }

    public void retrieveData(PersistentVolumeClaim pvc) {
        String name = pvc.getMetadata().getName();

        String namespaceName = pvc.getMetadata().getNamespace();
        System.out.println("Retrieving pvc " + namespaceName + "/" + name);

        String accessMode = stringListToString(safeNullable(pvc.getSpec().getAccessModes()), ",");
        String volumeMode = safeNullable(pvc.getSpec().getVolumeMode());
        String volumeName = safeNullable(pvc.getSpec().getVolumeName());
        String storageClassName = safeNullable(pvc.getSpec().getStorageClassName());
        String status = safeNullable(pvc.getStatus().getPhase());

        Optional<Map.Entry<String, Quantity>> capacityEntry = safeNullable(pvc.getStatus().getCapacity()).entrySet().stream().findFirst();
        String capacity = "";
        if (capacityEntry.isPresent()) {
            capacity = capacityEntry.get().getValue().toString();
        }
        pvcDataExporter.exportPVCData(namespaceName, name, status, capacity, accessMode, storageClassName, volumeMode, volumeName);
    }
}
