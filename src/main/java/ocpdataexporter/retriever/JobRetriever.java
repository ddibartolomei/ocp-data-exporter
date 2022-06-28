package ocpdataexporter.retriever;

import ocpdataexporter.client.ClusterClient;
import ocpdataexporter.exporter.JobDataExporter;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.batch.v1.Job;

import java.util.List;

public class JobRetriever extends WorkloadRetriever {

    private JobDataExporter jobDataExporter;

    private BatchContainerRetriever containerRetriever;

    public JobRetriever(ClusterClient client, JobDataExporter jobDataExporter, BatchContainerRetriever containerRetriever) {
        super(client);
        this.jobDataExporter = jobDataExporter;
        this.containerRetriever = containerRetriever;
    }

    public void retrieveData(Job job) {
        String wName = job.getMetadata().getName();

        String wNamespace = job.getMetadata().getNamespace();
        System.out.println("Retrieving job " + wNamespace + "/" + wName);

        String wKind = job.getKind();

        String wBackoffLimit = safeNullableObjectAsIntegerString(job.getSpec().getBackoffLimit());
        String wActiveDeadlineSeconds = safeNullableObjectAsLongString(job.getSpec().getActiveDeadlineSeconds());
        String wParallelism = safeNullableObjectAsIntegerString(job.getSpec().getParallelism());
        String wTTLSecondsAfterFinished = safeNullableObjectAsIntegerString(job.getSpec().getTtlSecondsAfterFinished());
        String wCompletions = safeNullableObjectAsIntegerString(job.getSpec().getCompletions());
        String wSchedule = "";

        String restartPolicy = safeNullable(job.getSpec().getTemplate().getSpec().getRestartPolicy());

        String[] wSecurityContextArray = getSecurityContext(job.getSpec().getTemplate().getSpec().getSecurityContext());

        List<Container> wContainers = job.getSpec().getTemplate().getSpec().getContainers();

        jobDataExporter.exportJobData(
                wNamespace,
                wKind,
                wName,
                wBackoffLimit,
                wActiveDeadlineSeconds,
                wParallelism,
                wTTLSecondsAfterFinished,
                wCompletions,
                wSchedule,
                wSecurityContextArray,
                restartPolicy,
                wContainers.size());

        // Retrieve containers specific data
        int index = 1;
        for (Container container : wContainers) {
            containerRetriever.retrieveData(wNamespace, container, index++, wContainers.size());
        }
    }
}
