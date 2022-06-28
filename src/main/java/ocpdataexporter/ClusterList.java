package ocpdataexporter;

import java.util.List;

public class ClusterList {

    private List<ClusterData> clusters;

    public ClusterList() {
    }

    public ClusterList(List<ClusterData> clusters) {
        this.clusters = clusters;
    }

    public List<ClusterData> getClusters() {
        return clusters;
    }

    public void setClusters(List<ClusterData> clusters) {
        this.clusters = clusters;
    }

    @Override
    public String toString() {
        return "ClusterList{" +
                "clusters=" + clusters +
                '}';
    }
}
