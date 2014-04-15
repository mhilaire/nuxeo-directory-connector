package org.nuxeo.directory.connector.json;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.nuxeo.directory.connector.ConnectorBasedDirectoryDescriptor;
import org.nuxeo.directory.connector.EntryConnector;
import org.nuxeo.directory.connector.InMemorySearchHelper;
import org.nuxeo.ecm.core.api.ClientException;

public class JsonInMemoryDirectoryConnector extends BaseJSONDirectoryConnector
        implements EntryConnector {

    protected Map<String, String> params;

    public ArrayList<HashMap<String, Object>> results;

    protected final InMemorySearchHelper searchHelper;

    public JsonInMemoryDirectoryConnector() {
        searchHelper = new InMemorySearchHelper(this);
    }

    protected ArrayList<HashMap<String, Object>> getJsonStream() {
        ArrayList<HashMap<String, Object>> mapList = new ArrayList<HashMap<String, Object>>();

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode responseAsJson = call(params.get("url"), objectMapper);

        JsonNode resultsNode = responseAsJson.get("results");
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
        };
        for (int i = 0; i < resultsNode.size(); i++) {
            try {
                Map<String, Object> map = new HashMap<String, Object>();
                map = objectMapper.readValue(resultsNode.get(i), typeRef);
                mapList.add((HashMap<String, Object>) map);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mapList;
    }

    public List<String> getEntryIds() {
        List<String> ids = new ArrayList<String>();
        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                ids.add(results.get(i).get("trackId").toString());
            }
        }
        return ids;
    }

    public Map<String, Object> getEntryMap(String id) {
        Map<String, Object> rc = new HashMap<String, Object>();
        rc = null;
        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                if (results.get(i).get("trackId").toString().equals(id)) {
                    rc = results.get(i);
                    break;
                }
            }
        }
        return rc;
    }

    public boolean hasEntry(String id) throws ClientException {
        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                if (results.get(i).get("trackId").equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void init(ConnectorBasedDirectoryDescriptor descriptor) {
        super.init(descriptor);
        params = descriptor.getParameters();
        results = this.getJsonStream();
    }

    @Override
    public List<String> queryEntryIds(Map<String, Serializable> filter,
            Set<String> fulltext) {
        return searchHelper.queryEntryIds(filter, fulltext);
    }

}