package ocpdataexporter.exporter;

import java.util.Map;

public interface ServiceDataExporter {

    void exportServiceData(String namespace, String sName, String sType, Map<String,String> sSelectors, String[] sPorts, String[] externalIPsArray, String[] sRoutes);
}
