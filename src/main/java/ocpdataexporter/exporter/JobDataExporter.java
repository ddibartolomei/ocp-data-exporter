package ocpdataexporter.exporter;

public interface JobDataExporter {
    void exportJobData(String namespace, String bKind, String bName, String bBackoffLimit, String bActiveDeadlineSeconds, String bParallelism, String bTTLSecondsAfterFinished, String bCompletions, String bSchedule, String[] bSecurityContextValues, String bRestartPolicy, int bContainerCount);
}
