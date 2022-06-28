package ocpdataexporter.exporter;

public interface ImageStreamDataExporter {
    void exportImageStreamData(String namespace, String name, String[] tags);
}
