package ocpdataexporter;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ocpdataexporter.client.ClusterClient;
import ocpdataexporter.exporter.ExcelXLSXDataExporter;
import org.apache.commons.cli.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootApplication
public class OCPDataExporter implements CommandLineRunner {

    @Autowired
    private DataRetrieverService dataRetrieverService;

    public static void main(String[] args) {
		SpringApplication.run(OCPDataExporter.class, args);
	}

    @Override
    public void run(String... args) {
        Options options = new Options();
        options.addOption("c", "connect", true, "Cluster connect url");
        options.addOption("u", "username", true, "Username");
        options.addOption("p", "password", true, "Password");
        options.addOption("t", "token", true, "Auth token (alternative to username/password)");
        options.addOption("i", "id", true, "Cluster ID (name), used to name the output file name (date and .xlsx extension are automatically added)");
        options.addOption("n", "namespace", true, "Single namespace to be exported (only export this namespace)");
        options.addOption("b", "batch", true, "Batch mode, reading cluster list from yaml file");

        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        String batchFile = null;

        ClusterData singleClusterData = new ClusterData();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("b")) {
                batchFile = cmd.getOptionValue("b");
            }
            else {
                if (!cmd.hasOption("c")) {
                    throw new ParseException("Missing cluster url parameter");
                }
                singleClusterData.setUrl(cmd.getOptionValue("c"));

                if (!cmd.hasOption("i")) {
                    throw new ParseException("Missing cluster id parameter");
                }
                singleClusterData.setId(cmd.getOptionValue("i"));

                if (cmd.hasOption("u") && cmd.hasOption("p")) {
                    singleClusterData.setUsername(cmd.getOptionValue("u"));
                    singleClusterData.setPassword(cmd.getOptionValue("p"));
                } else if (cmd.hasOption("t")) {
                    singleClusterData.setToken(cmd.getOptionValue("t"));
                } else {
                    throw new ParseException("Missing authentication credentials parameters");
                }

                if (cmd.hasOption("n")) {
                    singleClusterData.setNamespace(cmd.getOptionValue("n"));
                }
            }
        } catch (ParseException e) {
            formatter.printHelp("java -jar ocpdataexporter.jar ", options, true);
            System.err.println(e.getMessage());
            System.exit(1);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String date = sdf.format(new Date());

        if (batchFile!=null) {
            // read batch file
            ClusterList clusterList = null;
            try {
                File file = new File(batchFile);
                ObjectMapper om = new ObjectMapper(new YAMLFactory());
                clusterList = om.readValue(file, ClusterList.class);
            } catch (StreamReadException e) {
                System.err.println(e.getMessage());
                System.exit(3);
            } catch (DatabindException e) {
                System.err.println(e.getMessage());
                System.exit(4);
            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(5);
            }

            for (ClusterData clusterData: clusterList.getClusters()) {
                try {
                    System.out.println("--------------------------------------------------------------------------------");
                    System.out.println("Exporting cluster " + clusterData.getId() + " (" + clusterData.getUrl() + ")");
                    System.out.println("--------------------------------------------------------------------------------");
                    exportClusterData(clusterData, date);
                } catch (Exception e) {
                    System.err.println("Blocking error on the current cluster, skipping to the next one");
                    e.printStackTrace();
                }
                finally {
                    System.out.println();
                }
            }
        }
        else {
            exportClusterData(singleClusterData, date);
        }
    }

    private void exportClusterData(ClusterData clusterData, String date) {
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + clusterData.getId() + "-" + date + ".xlsx";
        ExcelXLSXDataExporter dataExporter = new ExcelXLSXDataExporter(fileLocation);

        ClusterClient clusterClient = new ClusterClient();
        if (clusterData.authByToken()) {
            clusterClient.connect(clusterData.getUrl(), clusterData.getToken());
        }
        else {
            clusterClient.connect(clusterData.getUrl(), clusterData.getUsername(), clusterData.getPassword());
        }

        if (clusterData.getNamespace()!=null) {
            System.out.println("Single namespace selection filter: " + clusterData.getNamespace());
            dataRetrieverService.retrieveData(dataExporter, clusterData.getNamespace(), clusterClient);
        }
        else {
            dataRetrieverService.retrieveData(dataExporter, clusterClient);
        }
    }
}
