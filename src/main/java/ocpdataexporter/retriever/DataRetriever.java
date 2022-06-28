package ocpdataexporter.retriever;

import ocpdataexporter.client.ClusterClient;
import io.fabric8.kubernetes.api.model.IntOrString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DataRetriever {

    private ClusterClient client;

    protected DataRetriever(ClusterClient client) {
        this.client = client;
    }

    protected ClusterClient getClient() {
        return client;
    }

    protected String getIntOrStringValue(IntOrString value) {
        if (value.getKind() == null) {
            Integer intValue = value.getIntVal();
            if (intValue != null) {
                return "" + intValue;
            } else {
                String stringValue = value.getStrVal();
                if (stringValue != null) {
                    return stringValue;
                } else {
                    return "null";
                }
            }
        } else if (value.getKind() == 0) {
            return "" +  value.getIntVal();
        } else if (value.getKind() == 1) {
            return value.getStrVal();
        } else {
            return "null";
        }
    }

    protected String safeNullableObjectAsBooleanString(Object object, String exists, String notExists) {
        return object!=null ? exists : notExists;
    }

    protected String safeNullableObjectAsIntegerString(Object object) {
        return safeNullableObjectAsIntegerString(object, "");
    }
    protected String safeNullableObjectAsIntegerString(Object object, String defaultValue) {
        return object!=null ? ((Integer) object).toString() : defaultValue;
    }

    protected String safeNullableObjectAsLongString(Object object) {
        return safeNullableObjectAsLongString(object, "");
    }
    protected String safeNullableObjectAsLongString(Object object, String defaultValue) {
        return object!=null ? ((Long) object).toString() : defaultValue;
    }

    protected String stringListToString(List<String> list, String separator) {
        StringBuilder sb = new StringBuilder();
        list.stream().forEach(s -> {
            sb.append(s).append(separator);
        });
        return sb.length()>0 ? sb.delete(sb.length() - separator.length(), sb.length()).toString() : "";
    }

    protected boolean notNull(Object object) {
        return object!=null;
    }

    protected String safeNullable(String value, String defaultValue) {
        return value!=null ? value : defaultValue;
    }

    protected String safeNullable(String value) {
        return value!=null ? value : "";
    }

    protected <T> List<T> safeNullable(List<T> value, List<T> defaultValue) {
        return value!=null ? value : defaultValue;
    }

    protected <T> List<T> safeNullable(List<T> value) {
        return value!=null ? value : new ArrayList<>();
    }

    protected <K,V> Map<K,V> safeNullable(Map<K,V> value, Map<K,V> defaultValue) {
        return value!=null ? value : defaultValue;
    }

    protected <K,V> Map<K,V> safeNullable(Map<K,V> value) {
        return value!=null ? value : new HashMap<>();
    }

}
