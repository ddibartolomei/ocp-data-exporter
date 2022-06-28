package ocpdataexporter.retriever;

import ocpdataexporter.client.ClusterClient;
import ocpdataexporter.exporter.NamespaceDataExporter;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.DaemonSet;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.autoscaling.v1.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1beta1.CronJob;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.ImageStream;

import java.util.ArrayList;
import java.util.List;

public class NamespaceRetriever extends DataRetriever {

    private NamespaceDataExporter namespaceDataExporter;

    private DeploymentRetriever deploymentRetriever;

    private DeploymentConfigRetriever deploymentConfigRetriever;

    private StatefulSetRetriever statefulSetRetriever;

    private DaemonSetRetriever daemonSetRetriever;

    private ServiceRetriever serviceRetriever;

    private PVCRetriever pvcRetriever;

    private JobRetriever jobRetriever;

    private CronJobRetriever cronJobRetriever;

    private HPARetriever hpaRetriever;

    private BuildConfigRetriever buildConfigRetriever;

    private ImageStreamRetriever imageStreamRetriever;

    public NamespaceRetriever(ClusterClient client,
                              NamespaceDataExporter namespaceDataExporter,
                              DeploymentRetriever deploymentRetriever,
                              DeploymentConfigRetriever deploymentConfigRetriever,
                              StatefulSetRetriever statefulSetRetriever,
                              DaemonSetRetriever daemonSetRetriever,
                              ServiceRetriever serviceRetriever,
                              PVCRetriever pvcRetriever,
                              JobRetriever jobRetriever,
                              CronJobRetriever cronJobRetriever,
                              HPARetriever hpaRetriever,
                              BuildConfigRetriever buildConfigRetriever,
                              ImageStreamRetriever imageStreamRetriever) {
        super(client);
        this.namespaceDataExporter = namespaceDataExporter;
        this.deploymentRetriever = deploymentRetriever;
        this.deploymentConfigRetriever = deploymentConfigRetriever;
        this.statefulSetRetriever = statefulSetRetriever;
        this.daemonSetRetriever = daemonSetRetriever;
        this.serviceRetriever = serviceRetriever;
        this.pvcRetriever = pvcRetriever;
        this.jobRetriever = jobRetriever;
        this.cronJobRetriever = cronJobRetriever;
        this.hpaRetriever = hpaRetriever;
        this.buildConfigRetriever = buildConfigRetriever;
        this.imageStreamRetriever = imageStreamRetriever;
    }

    public void retrieveData(Namespace namespace, String namespaceFields) {
        String namespaceName = namespace.getMetadata().getName();

        System.out.println("Retrieving namespace " + namespaceName);

        // Retrieve workloads
        List<Deployment> deployments = getClient().k8s().apps().deployments().inNamespace(namespaceName).list().getItems();
        List<DeploymentConfig> deploymentConfigs = getClient().os().deploymentConfigs().inNamespace(namespaceName).list().getItems();
        List<StatefulSet> statefulSets = getClient().k8s().apps().statefulSets().inNamespace(namespaceName).list().getItems();
        List<DaemonSet> daemonSets = getClient().k8s().apps().daemonSets().inNamespace(namespaceName).list().getItems();

        int workloadCount = deployments.size() + deploymentConfigs.size() + statefulSets.size() + daemonSets.size();

        System.out.println("Found " + workloadCount + " workloads in namespace " + namespaceName);

        List<String> namespaceData = new ArrayList<>();
        String[] labelsAndAnnotations = namespaceFields.split(",");
        for (String entry: labelsAndAnnotations) {
            // Search among labels
            if (notNull(namespace.getMetadata().getLabels()) && namespace.getMetadata().getLabels().containsKey(entry)) {
                namespaceData.add("label/" + entry + ": " + namespace.getMetadata().getLabels().get(entry));
            }
            // Search among annotations
            if (notNull(namespace.getMetadata().getAnnotations()) && namespace.getMetadata().getAnnotations().containsKey(entry)) {
                namespaceData.add("annotation/" + entry + ": " + namespace.getMetadata().getAnnotations().get(entry));
            }
        }

        namespaceDataExporter.exportNamespace(
                namespaceName,
                workloadCount,
                namespaceData.stream().toArray(String[]::new));

        namespaceDataExporter.exportNamespaceWorkloadData(namespaceName);

        for (Deployment deployment : deployments) {
            deploymentRetriever.retrieveData(deployment);
        }

        for (DeploymentConfig deploymentConfig : deploymentConfigs) {
            deploymentConfigRetriever.retrieveData(deploymentConfig);
        }

        for (StatefulSet statefulSet : statefulSets) {
            statefulSetRetriever.retrieveData(statefulSet);
        }

        for (DaemonSet daemonSet : daemonSets) {
            daemonSetRetriever.retrieveData(daemonSet);
        }

        // Retrieve batch workloads
        List<Job> jobs = getClient().k8s().batch().v1().jobs().inNamespace(namespaceName).list().getItems();
        List<CronJob> cronJobs = getClient().k8s().batch().cronjobs().inNamespace(namespaceName).list().getItems();
        System.out.println("Found " + (jobs.size() + cronJobs.size()) + " batch workloads in namespace " + namespaceName);
        namespaceDataExporter.exportNamespaceBatchWorkloadData(namespaceName);
        for (Job job : jobs) {
            jobRetriever.retrieveData(job);
        }
        for (CronJob cronJob : cronJobs) {
            cronJobRetriever.retrieveData(cronJob);
        }

        // Retrieve Services
        List<Service> services = getClient().k8s().services().inNamespace(namespaceName).list().getItems();
        System.out.println("Found " + services.size() + " services in namespace " + namespaceName);
        namespaceDataExporter.exportNamespaceServiceData(namespaceName);
        for (Service service : services) {
            serviceRetriever.retrieveData(service);
        }

        // Retrieve PVC
        List<PersistentVolumeClaim> pvcs = getClient().k8s().persistentVolumeClaims().inNamespace(namespaceName).list().getItems();
        System.out.println("Found " + pvcs.size() + " pvc in namespace " + namespaceName);
        namespaceDataExporter.exportNamespacePVCData(namespaceName);
        for (PersistentVolumeClaim pvc : pvcs) {
            pvcRetriever.retrieveData(pvc);
        }

        // Retrieve Horizontal POD Autoscaler
        List<HorizontalPodAutoscaler> hpas = getClient().k8s().autoscaling().v1().horizontalPodAutoscalers().inNamespace(namespaceName).list().getItems();
        System.out.println("Found " + hpas.size() + " horizontal POD autoscalers in namespace " + namespaceName);
        namespaceDataExporter.exportNamespaceHPAData(namespaceName);
        for (HorizontalPodAutoscaler hpa : hpas) {
            hpaRetriever.retrieveData(hpa);
        }

        // Retrieve BuildConfig
        List<BuildConfig> buildConfigs = getClient().os().buildConfigs().inNamespace(namespaceName).list().getItems();
        System.out.println("Found " + buildConfigs.size() + " build configs in namespace " + namespaceName);
        namespaceDataExporter.exportNamespaceBuildConfigData(namespaceName);
        for (BuildConfig bc : buildConfigs) {
            buildConfigRetriever.retrieveData(bc);
        }

        // Retrieve ImageStream
        List<ImageStream> imageStreams = getClient().os().imageStreams().inNamespace(namespaceName).list().getItems();
        System.out.println("Found " + imageStreams.size() + " image streams in namespace " + namespaceName);
        namespaceDataExporter.exportNamespaceImageStreamData(namespaceName);
        for (ImageStream is : imageStreams) {
            imageStreamRetriever.retrieveData(is);
        }

    }
}
