package com.example.speer.Service.ServiceImpl;

import com.example.speer.Service.ElasticSearchService;
import com.example.speer.utils.Constants;
import com.example.speer.utils.CustomQuery;
import com.example.speer.utils.HelperFunctions;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomElasticSearchServiceImpl implements ElasticSearchService {

    @Value("${api.elasticsearch.uri}")
    private String elasticSearchUri;

    @Value("${api.elasticsearch.search}")
    private String elasticSearchSearchPrefix;

    @Autowired
    UserAndNotesServiceImpl userAndNotesServiceImpl;

    @Override
    public CustomQuery searchQuery(String query) throws IOException {
        int currentUserId = userAndNotesServiceImpl.getCurrentUserId();
        String body = HelperFunctions.buildMultiIndexMatchBody(query,currentUserId);
        return executeHttpRequest(body);
    }

    private CustomQuery executeHttpRequest(String body) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            CustomQuery customQuery = new CustomQuery();
            HttpPost httpPost = new HttpPost(HelperFunctions.buildSearchUri(elasticSearchUri, "note_index", elasticSearchSearchPrefix));
            httpPost.setHeader(Constants.CONTENT_ACCEPT, Constants.APP_TYPE);
            httpPost.setHeader(Constants.CONTENT_TYPE, Constants.APP_TYPE);

            try {
                httpPost.setEntity(new StringEntity(body, Constants.ENCODING_UTF8));
                HttpResponse response = httpClient.execute(httpPost);
                String message = EntityUtils.toString(response.getEntity());
                JSONObject myObject = new JSONObject(message);

                int totalHits = myObject.getJSONObject(Constants.HITS).getJSONObject(Constants.TOTAL_HITS).getInt("value");
                if (totalHits != 0) {
                    JSONArray hitsArray = myObject.getJSONObject(Constants.HITS).getJSONArray(Constants.HITS);
                    List<String> notesList = new ArrayList<>();

                    for (int i = 0; i < hitsArray.length(); i++) {
                        JSONObject hitObject = hitsArray.getJSONObject(i);
                        JSONObject sourceObject = hitObject.getJSONObject("_source");
                        String note = sourceObject.getString("note");
                        notesList.add(note);
                    }

                    customQuery.setElements(notesList.toString());
                    customQuery.setNumberOfResults(totalHits);
                    customQuery.setTimeTook((float) ((double) myObject.getInt(Constants.TOOK) / Constants.TO_MS));
                } else {
                    customQuery.setElements(null);
                    customQuery.setNumberOfResults(0);
                    customQuery.setTimeTook((float) ((double) myObject.getInt(Constants.TOOK) / Constants.TO_MS));
                }
            } catch (IOException | JSONException e) {
                System.out.println("Error while connecting to elastic engine");
                customQuery.setNumberOfResults(0);
            }

            return customQuery;
        }
    }

}
