package ocpdataexporter;

public class ClusterData {

    private String id;    // Required
    private String url;     // Required
    private String token;
    private String username;
    private String password;
    private String namespace;

    public ClusterData() {
    }

    public ClusterData(String id, String url, String token, String username, String password, String namespace) {
        this.id = id;
        this.url = url;
        this.token = token;
        this.username = username;
        this.password = password;
        this.namespace = namespace;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public boolean authByToken() {
        return token!=null;
    }

    public boolean authByUsernamePassword() {
        return token!=null;
    }

    @Override
    public String toString() {
        return "ClusterData{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", token='" + token + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
