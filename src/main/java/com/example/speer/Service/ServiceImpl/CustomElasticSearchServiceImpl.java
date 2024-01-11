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
import org.springframework.context.annotation.Bean;
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


    //This constructor is required specifically by CustomElasticSearchServiceImplTest class
    public CustomElasticSearchServiceImpl(@Value("${api.elasticsearch.uri}") String elasticSearchUri,  @Value("${api.elasticsearch.search}")String elasticSearchSearchPrefix, UserAndNotesServiceImpl userAndNotesServiceImpl) {
        this.elasticSearchUri = elasticSearchUri;
        this.elasticSearchSearchPrefix = elasticSearchSearchPrefix;
        this.userAndNotesServiceImpl = userAndNotesServiceImpl;
    }

    @Override
    public CustomQuery searchQuery(String query) throws IOException {
        //The logic is quite simple here:

        //We will first have to find the current authenticated user
        int currentUserId = userAndNotesServiceImpl.getCurrentUserId();

        //Now we will create a multi index matching body with the help of a Helper Function
        //This body will be sent with the request in the form of string
        String body = HelperFunctions.buildMultiIndexMatchBody(query,currentUserId);

        //And then return the result of the request made to the elasticsearch
        return executeHttpRequest(body);
    }

    private CustomQuery executeHttpRequest(String body) throws IOException {
        //Here we will create a httpClient for sending request
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            //We will make a new customquery object
            CustomQuery customQuery = new CustomQuery();

            //Here we will build the URL on which the request will be sent
            HttpPost httpPost = new HttpPost(HelperFunctions.buildSearchUri(elasticSearchUri, "note_index", elasticSearchSearchPrefix));

            //We will setup the headers with content accept and content type
            httpPost.setHeader(Constants.CONTENT_ACCEPT, Constants.APP_TYPE);
            httpPost.setHeader(Constants.CONTENT_TYPE, Constants.APP_TYPE);

            try {
                //Here we will setup the entity with the body which was made using buildMultiIndexMatchBody() function
                httpPost.setEntity(new StringEntity(body, Constants.ENCODING_UTF8));

                //Now finally we sent the request and receive the response in HttpResponse object
                HttpResponse response = httpClient.execute(httpPost);

                //================================================================
                //Now as the execution flow is reaching here it means the response has been received
                //Now the time is to arrange our result to be returned to the user
                //We will arrange this in a customQuery object and will return it
                //================================================================

                //Here we will fetch the entity from the response and store that result in a message String
                String message = EntityUtils.toString(response.getEntity());

                //We will create a new JSON Object and fill the object with the message received
                JSONObject jsonObject = new JSONObject(message);

                /*
                The structure of the custom query is something like this:
                public class CustomQuery {
                    private Float timeTook;
                    private Integer numberOfResults;
                    private String elements;
                }
                */

                //Now first of all we will fetch the total number of results (total hits)
                int totalHits = jsonObject.getJSONObject(Constants.HITS).getJSONObject(Constants.TOTAL_HITS).getInt("value");

                if (totalHits != 0) {
                    //if the number of results i.e. total hits are not 0 then we will fetch the JSON Array of hit results
                    JSONArray hitsArray = jsonObject.getJSONObject(Constants.HITS).getJSONArray(Constants.HITS);

                    //initialize an empty arraylist
                    List<String> notesList = new ArrayList<>();

                    //Now we will loop over that JSON array which contains all the hit results
                    for (int i = 0; i < hitsArray.length(); i++) {

                        JSONObject hitObject = hitsArray.getJSONObject(i);

                        //for each hit object we will fetch the source
                        JSONObject sourceObject = hitObject.getJSONObject("_source");

                        //Now we will get the note from that source and add it to the that empty list we intialized earlier for returning
                        String note = sourceObject.getString("note");
                        notesList.add(note);
                    }

                    //----------------------------------------------------------------
                    //Now here will fill the acquired results into a customQuery object
                    //----------------------------------------------------------------

                    //we will convert the noteList to string
                    customQuery.setElements(notesList.toString());

                    //The number of hits
                    customQuery.setNumberOfResults(totalHits);

                    //And the time took will be fetched from the jsonObject, converted to float
                    customQuery.setTimeTook((float) ((double) jsonObject.getInt(Constants.TOOK) / Constants.TO_MS));
                } else {
                    //If not hits are there it means no results found for the query
                    customQuery.setElements(null);
                    customQuery.setNumberOfResults(0);
                    customQuery.setTimeTook((float) ((double) jsonObject.getInt(Constants.TOOK) / Constants.TO_MS));
                }
            } catch (IOException | JSONException e) {
                System.out.println("Error while connecting to elastic engine");
                customQuery.setNumberOfResults(0);
            }

            return customQuery;
        }
    }

}
