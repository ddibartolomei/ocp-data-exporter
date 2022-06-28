package ocpdataexporter.exporter;

public interface PVCDataExporter {

    void exportPVCData(String namespace, String name, String status, String capacity, String accessMode, String storageClassName, String volumeMode, String volumeName);
}
