# OpenShift Data Exporter

OCP Data Exporter is a command line tool to extract configuration data from application namespaces running on OpenShift clusters.

The output, for each analyzed cluster, is an Excel file containing several sheets.

It extracts some data about namespaces, workloads (deployment, deployment config, statefulset, daemonset), their containers, services, routes, PVCs, jobs/cronjobs, horizontal pod autoscalers, build configs, imagestreams.

These data can be useful to have a wide view of the cluster content in order to make some analysis and statistics, for example before applications are migrated to a new OpenShift/Kubernetes cluster.

## Build
```shell
./mvnw clean package
```

##  Usage
```shell
usage: java -jar ocpdataexporter.jar  [-b <arg>] [-c <arg>] [-i <arg>] [-n <arg>] [-p <arg>] [-t <arg>] [-u <arg>]
 -b,--batch <arg>       Batch mode, reading cluster list from yaml file
 -c,--connect <arg>     Cluster connect url
 -i,--id <arg>          Cluster ID (name), used to name the output file
                        name (date and .xlsx extension are automatically
                        added)
 -n,--namespace <arg>   Single namespace to be exported (only export this
                        namespace)
 -p,--password <arg>    Password
 -t,--token <arg>       Auth token (alternative to username/password)
 -u,--username <arg>    Username
```

## Run 

### Run on one OCP cluster
```shell
java -jar target/ocpdataexporter.jar \ 
    -c <cluster url> \
    -t <auth token> \
    -t <auth token> \
    -i <cluster id>
```

### Run on multiple OCP clusters using a batch file 

Create a batch yaml file containing the connection data of the target clusters:

```yaml
clusters:
  - id: my-cluster1-id
    url: https://master.cluster1.my.ocp.domain:8443
    token: THE_CONNECTION_TOKEN
  - id: my-cluster2-id
    url: https://master.cluster2.my.ocp.domain:8443
    token: THE_CONNECTION_TOKEN
  - ...
``` 

Run in batch mode:
```shell
java -jar target/ocpdataexporter.jar -b my-batch-file.yml
```

## Extra configuration options
Inside the `application.properties` file extra behaviours can be configured.

### Namespace filters
The `ocpdataexporter.namespace.filter` can be configured to skip some namespaces based on a list of regular expression.

Example:
```properties
ocpdataexporter.namespace.filter=^default$|^openshift[-a-zA-Z0-9]*$|^kube[-a-zA-Z0-9]*$|^stackrox[-a-zA-Z0-9]*$|^portworx[-a-zA-Z0-9]*$|^monitoraggio$
```

### Labels/Annotations to extract from namespaces objects
The `ocpdataexporter.namespace.fields` can be configured to extract a list of string from the labels and annotations inside the namespace objects.
Each element of the list get searched among both the labels and the annotations of each analyzed namespace. 

Example:
```properties
ocpdataexporter.namespace.fields=some-name,project-name
```
