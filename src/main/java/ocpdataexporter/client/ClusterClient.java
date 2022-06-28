package ocpdataexporter.client;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

public class ClusterClient {

    private KubernetesClient k8sClient;
    private OpenShiftClient osClient;

    public ClusterClient() {
    }

    public void connect(String clusterUrl, String username, String password) {
        System.out.println("Connecting to " + clusterUrl);
        Config config1 = new ConfigBuilder()
                .withTrustCerts(true)
                .withMasterUrl(clusterUrl)
                .withUsername(username)
                .withPassword(password)
                .build();
    }

    public void connect(String clusterUrl, String authToken) {
        System.out.println("Connecting to " + clusterUrl);
        connectToCluster(new ConfigBuilder()
                .withTrustCerts(true)
                .withMasterUrl(clusterUrl)
                .withOauthToken(authToken)
                .build());
    }

    private void connectToCluster(Config config) {
        k8sClient = new DefaultKubernetesClient(config);
        osClient = null;
        if (k8sClient.isAdaptable(OpenShiftClient.class)) {
            osClient = k8sClient.adapt(OpenShiftClient.class);
        } else {
            System.err.println("Adapting to OpenShiftClient not supported. Check if adapter is present, and that env provides /oapi root path.");
            System.exit(2);
        }
    }

    public KubernetesClient k8s() {
        return k8sClient;
    }

    public OpenShiftClient os() {
        return osClient;
    }
}
