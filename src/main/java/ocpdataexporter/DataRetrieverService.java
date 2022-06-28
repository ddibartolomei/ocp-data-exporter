package ocpdataexporter;

import ocpdataexporter.client.ClusterClient;
import ocpdataexporter.retriever.BatchContainerRetriever;
import ocpdataexporter.exporter.DataExportException;
import ocpdataexporter.exporter.DataExporter;
import io.fabric8.kubernetes.api.model.Namespace;
import ocpdataexporter.retriever.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DataRetrieverService {

    @Value("${ocpdataexporter.namespace.filter:}")
    private String namespaceFilter;

    @Value("${ocpdataexporter.namespace.fields:}")
    private String namespaceFields;

    public DataRetrieverService() {
    }

    public void retrieveData(DataExporter dataExporter, ClusterClient client) {
        retrieveData(dataExporter, null, client);
    }
    public void retrieveData(DataExporter dataExporter, String selectedNamespace, ClusterClient client) {

        System.out.println("Retrieving data...");

        dataExporter.exportHeaderData();

        ContainerRetriever containerRetriever = new ContainerRetriever(client, dataExporter);
        BatchContainerRetriever batchContainerRetriever = new BatchContainerRetriever(client, dataExporter);
        NamespaceRetriever namespaceRetriever = new NamespaceRetriever(
                client,
                dataExporter,
                new DeploymentRetriever(client, dataExporter, containerRetriever),
                new DeploymentConfigRetriever(client, dataExporter, containerRetriever),
                new StatefulSetRetriever(client, dataExporter, containerRetriever),
                new DaemonSetRetriever(client, dataExporter, containerRetriever),
                new ServiceRetriever(client, dataExporter),
                new PVCRetriever(client, dataExporter),
                new JobRetriever(client, dataExporter, batchContainerRetriever),
                new CronJobRetriever(client, dataExporter, batchContainerRetriever),
                new HPARetriever(client, dataExporter),
                new BuildConfigRetriever(client, dataExporter),
                new ImageStreamRetriever(client, dataExporter));

        Pattern namespaceFilterPattern = Pattern.compile(namespaceFilter);

        // Retrieve namespaces, filtering if required
        List<Namespace> namespaces = new ArrayList<>();
        if (selectedNamespace == null) {
            System.out.println("Retrieving namespace list");
            List<Namespace> allNamespaces = client.k8s().namespaces().list().getItems();
            namespaces.addAll(allNamespaces.stream()
                    .filter(n -> !namespaceFilterPattern.matcher(n.getMetadata().getName()).matches())
                    .collect(Collectors.toList()));
            System.out.println("Found " + namespaces.size() + "/" + allNamespaces.size() + " valid namespaces (skipped " + (allNamespaces.size() - namespaces.size()) + "/" + allNamespaces.size() + ")");
        }
        else {
            System.out.println("Retrieving namespace list");
            Namespace namespaceObject = client.k8s().namespaces().withName(selectedNamespace).get();
            if (namespaceObject != null) {
                namespaces.add(namespaceObject);
                System.out.println("Found namespace " + selectedNamespace);
            }
            else {
                System.err.println("ERROR: namespace " + selectedNamespace + " not found");
                System.exit(3);
            }
        }

        int namespaceCounter = 1;
        for (Namespace namespace : namespaces) {
            namespaceRetriever.retrieveData(namespace, namespaceFields);
            System.out.println("Retrieved namespace " + namespaceCounter++ + "/" + namespaces.size());
            System.out.println();
        }

        try {
            if (!namespaces.isEmpty()) {
                System.out.println("Finalizing export...");
                dataExporter.finalizeExport();
                System.out.println("Export completed");
            }
        }
        catch (DataExportException e) {
            System.err.println("ERROR: " + e.getMessage() + "(" + e.getCause() + ")");
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
