package ocpdataexporter.retriever;

import ocpdataexporter.client.ClusterClient;
import ocpdataexporter.exporter.ImageStreamDataExporter;
import io.fabric8.openshift.api.model.ImageStream;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImageStreamRetriever extends DataRetriever {

    private ImageStreamDataExporter imageStreamDataExporter;

    public ImageStreamRetriever(ClusterClient client, ImageStreamDataExporter imageStreamDataExporter) {
        super(client);
        this.imageStreamDataExporter = imageStreamDataExporter;
    }

    public void retrieveData(ImageStream imageStream) {
        String wName = imageStream.getMetadata().getName();

        String wNamespace = imageStream.getMetadata().getNamespace();
        System.out.println("Retrieving image stream " + wNamespace + "/" + wName);

        // Tags
        List<String> tags = new ArrayList<>();
        if (notNull(imageStream.getSpec().getTags())) {
            tags = imageStream.getSpec().getTags().stream().map(ist -> {
                if (notNull(ist.getFrom())) {
                    return safeNullable(ist.getName()) + "/" + safeNullable(ist.getFrom().getKind()) + "[" + safeNullable(ist.getFrom().getName()) + "]";
                }
                else {
                    return safeNullable(ist.getName()) + "/undefined";
                }
            }).collect(Collectors.toList());
        }
        imageStreamDataExporter.exportImageStreamData(
                wNamespace,
                wName,
                tags.stream().toArray(String[]::new));

    }
}
