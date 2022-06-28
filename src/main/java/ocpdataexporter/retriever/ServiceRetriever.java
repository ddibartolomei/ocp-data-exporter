package ocpdataexporter.retriever;

import ocpdataexporter.client.ClusterClient;
import ocpdataexporter.exporter.ServiceDataExporter;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteTargetReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceRetriever extends DataRetriever {

    private ServiceDataExporter serviceDataExporter;

    private ContainerRetriever containerRetriever;

    public ServiceRetriever(ClusterClient client, ServiceDataExporter serviceDataExporter) {
        super(client);
        this.serviceDataExporter = serviceDataExporter;
    }

    public void retrieveData(Service service) {
        String sName = service.getMetadata().getName();

        String sNamespace = service.getMetadata().getNamespace();
        System.out.println("Retrieving service " + sNamespace + "/" + sName);

        String sType = service.getSpec().getType()!=null ? service.getSpec().getType() : "ClusterIP";

        Map<String, String> sSelectorMap = service.getSpec().getSelector()!=null ? service.getSpec().getSelector() : new HashMap<>();

        String[] servicePortsArray = service.getSpec().getPorts().stream()
                .map(p -> {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(p.getName()!=null ? p.getName() + "/" : "");
                    stringBuilder.append(p.getProtocol()!=null ? p.getProtocol() + "/" : "");
                    stringBuilder.append(p.getPort()!=null ? p.getPort() : "");
                    stringBuilder.append(p.getTargetPort()!=null ? ">" + getIntOrStringValue(p.getTargetPort()) : "");
                    stringBuilder.append(p.getNodePort()!=null ? "/NodePort:" + + p.getNodePort() : "");
                    return stringBuilder.toString();
                })
                .toArray(String[]::new);

        String[] externalIPsArray = service.getSpec().getExternalIPs().stream().toArray(String[]::new);

        // TODO export weight as a percentage?
        List<Route> nsRoutes = getClient().os().routes().inNamespace(sNamespace).list().getItems();
        List<String> serviceRoutes = new ArrayList<>();
        for (Route r:nsRoutes) {
            String targetPort = r.getSpec().getPort()!=null ? getIntOrStringValue(r.getSpec().getPort().getTargetPort()) : "";

            // Filter routes bound to this service (.spec.to or .spec.alternateBackends)
            if ("Service".equals(r.getSpec().getTo().getKind()) && sName.equals(r.getSpec().getTo().getName())) {
                serviceRoutes.add(buildRouteData(
                        r.getMetadata().getName(),
                        targetPort,
                        r.getSpec().getTo().getWeight(),
                        r.getSpec().getHost(),
                        r.getSpec().getTls()!=null ? r.getSpec().getTls().getTermination() : null
                ));
            }
            else {
                List<RouteTargetReference> alternateServices = r.getSpec().getAlternateBackends();
                for (RouteTargetReference altService: alternateServices) {
                    if ("Service".equals(altService.getKind()) && sName.equals(altService.getName())) {
                        serviceRoutes.add(buildRouteData(
                                r.getMetadata().getName(),
                                targetPort,
                                altService.getWeight(),
                                r.getSpec().getHost(),
                                r.getSpec().getTls()!=null ? r.getSpec().getTls().getTermination() : null
                        ));
                    }
                }
            }
        }
        String[] serviceRouteArray = serviceRoutes.stream().toArray(String[]::new);

        serviceDataExporter.exportServiceData(sNamespace, sName, sType, sSelectorMap, servicePortsArray, externalIPsArray, serviceRouteArray);
    }

    private String buildRouteData(String routeName, String serviceTargetPort, int weight, String host, String tlsTermination) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(routeName + "/");
        stringBuilder.append(safeNullable(serviceTargetPort) + "/");
        stringBuilder.append(weight + "/");
        stringBuilder.append(safeNullable(host) + "/");
        stringBuilder.append(safeNullable(tlsTermination, "noTLS"));
        return stringBuilder.toString();
    }
}
