package ocpdataexporter.retriever;

import ocpdataexporter.client.ClusterClient;
import io.fabric8.kubernetes.api.model.*;

import java.util.*;
import java.util.stream.Collectors;

public abstract class WorkloadRetriever extends DataRetriever {

    protected class ConfigMapsSecretsPVCsCollection {
        List<String> wConfigMapsList = new ArrayList<>();
        List<String> wSecretsList = new ArrayList<>();
        List<String> wPVCsList = new ArrayList<>();
        List<String> wOtherVolumesList = new ArrayList<>();

        public ConfigMapsSecretsPVCsCollection() {
        }
    }

    protected WorkloadRetriever(ClusterClient client) {
        super(client);
    }

    protected String[] getMatchingServicesDescriptors(String namespace, Map<String, String> wSelectorMap) {
        List<Service> services = getClient().k8s().services().inNamespace(namespace).list().getItems().stream()
                .filter(s -> s.getSpec().getSelector()!=null && wSelectorMap.entrySet().containsAll(s.getSpec().getSelector().entrySet()))
                .collect(Collectors.toList());
        String[] wMatchedServicesArray = new String[services.size()];
        for (int i = 0; i < services.size(); i++) {
            wMatchedServicesArray[i] = getServiceDescriptor(services.get(i));
        }

        return wMatchedServicesArray;
    }

    protected String getServiceDescriptor(Service service) {
        String separator = ";";
        String serviceDescriptor = service.getMetadata().getName();
        serviceDescriptor += "/" + (service.getSpec().getType()!=null ? service.getSpec().getType() : "ClusterIP");

        String servicePosts = "";
        List<ServicePort> servicePortList = service.getSpec().getPorts();
        if (servicePortList.size()>0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ServicePort sp: servicePortList) {
                stringBuilder.append(sp.getName()!=null ? sp.getName() + "/" : "");
                stringBuilder.append(sp.getProtocol()!=null ? sp.getProtocol() + "/" : "");
                stringBuilder.append(sp.getPort()!=null ? sp.getPort() : "");
                stringBuilder.append(sp.getTargetPort()!=null ? ">" + getIntOrStringValue(sp.getTargetPort()) : "");
                stringBuilder.append(sp.getNodePort()!=null ? "/NodePort:" + + sp.getNodePort() : "");
                stringBuilder.append(separator);
            }
            servicePosts = stringBuilder.length()>0 ? stringBuilder.delete(stringBuilder.length() - separator.length(), stringBuilder.length()).toString() : "";
        }
        serviceDescriptor += "[" + servicePosts + "]";

        return serviceDescriptor;
    }

    protected ConfigMapsSecretsPVCsCollection collectConfigMapsSecretsAndPVCs(List<Volume> volumes, List<Container> containers) {
        ConfigMapsSecretsPVCsCollection collection = new ConfigMapsSecretsPVCsCollection();

        // Retrieve volumes from .spec.volumes
        if (volumes!=null) {
            volumes.stream().forEach(volume -> {
                if (volume.getPersistentVolumeClaim() != null) {
                    collection.wPVCsList.add(volume.getPersistentVolumeClaim().getClaimName() + " [ref: " + volume.getName() + "]");
                } else if (volume.getEmptyDir() != null) {
                    collection.wOtherVolumesList.add("emptyDir [ref: " + volume.getName() + "]");
                } else if (volume.getConfigMap() != null) {
                    collection.wConfigMapsList.add(volume.getConfigMap().getName() + " [ref: " + volume.getName() + "]");
                } else if (volume.getSecret() != null) {
                    collection.wSecretsList.add(volume.getSecret().getSecretName() + " [ref: " + volume.getName() + "]");
                } else {
                    collection.wOtherVolumesList.add("?UNMANAGED_TYPE? [ref: " + volume.getName() + "]");
                }
            });
        }

        // Collect .env.valueFrom/.envFrom refs to configmaps/secrets and add them to the whole list of referred configmaps/secrets
        Set<String> wConfigMapsFromEnvSet = new HashSet<>();
        Set<String> wSecretsFromEnvSet = new HashSet<>();
        containers.stream().forEach(container -> {
            // get .env.valueFrom refs to configmaps/secrets
            container.getEnv().stream().filter(env -> env.getValueFrom()!=null).forEach(env -> {
                if (env.getValueFrom().getConfigMapKeyRef()!=null) {
                    wConfigMapsFromEnvSet.add(env.getValueFrom().getConfigMapKeyRef().getName() + " [ref: env vars]");
                }
                else if (env.getValueFrom().getSecretKeyRef()!=null) {
                    wSecretsFromEnvSet.add(env.getValueFrom().getSecretKeyRef().getName() + " [ref: env vars]");
                }
            });
            // get .envFrom refs to configmaps/secrets
            container.getEnvFrom().stream().forEach(env -> {
                if (env.getConfigMapRef()!=null) {
                    wConfigMapsFromEnvSet.add(env.getConfigMapRef().getName() + " [ref: env vars]");
                }
                else if (env.getSecretRef()!=null) {
                    wSecretsFromEnvSet.add(env.getSecretRef().getName() + " [ref: env vars]");
                }
            });
        });
        collection.wConfigMapsList.addAll(wConfigMapsFromEnvSet);
        collection.wSecretsList.addAll(wSecretsFromEnvSet);

        return collection;
    }

    protected String[] getSecurityContext(PodSecurityContext podSecurityContext) {
        if (podSecurityContext!=null) {
            String securityContext = podSecurityContext.toString();
            securityContext = securityContext.substring("PodSecurityContext(".length(), securityContext.length()-1);
            String[] securityContextValues = securityContext.split(", ");
            return Arrays.stream(securityContextValues)
                        .filter(sc -> !sc.endsWith("=null"))
                        .filter(sc -> !sc.endsWith("=[]"))
                        .filter(sc -> !sc.endsWith("={}"))
                        .toArray(String[]::new);
        }
        else {
            return null;
        }
    }

    protected String[] getContainerSecurityContext(SecurityContext containerSecurityContext) {
        if (containerSecurityContext!=null) {
            String securityContext = containerSecurityContext.toString();
            securityContext = securityContext.substring("SecurityContext(".length(), securityContext.length()-1);
            String[] securityContextValues = securityContext.split(", ");
            return Arrays.stream(securityContextValues)
                    .filter(sc -> !sc.endsWith("=null"))
                    .filter(sc -> !sc.endsWith("=[]"))
                    .filter(sc -> !sc.endsWith("={}"))
                    .toArray(String[]::new);
        }
        else {
            return null;
        }
    }
}
