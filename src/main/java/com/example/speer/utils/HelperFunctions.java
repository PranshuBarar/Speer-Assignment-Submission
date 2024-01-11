package com.example.speer.utils;

import org.json.JSONArray;

import java.util.Arrays;

public class HelperFunctions {

    private HelperFunctions() {}

    private static final String SEARCH_FIELD = "note";

    //================================================================================================================
    //================================================================================================================
    /*
        Now the actual query which we want for the elasticsearch looks like this:
        {
          "from": 0,
          "size": 100,
          "track_total_hits": true,
          "sort": {
            "id": {
              "order": "asc"
            }
          },
          "query": {
            "bool": {
              "should": [
                {
                  "bool": {
                    "must": [
                      {
                        "query_string": {
                          "query": "*{query}*",  // Replace {query} with the actual query string
                          "fields": ["note"],
                          "default_operator": "AND"
                        }
                      },
                      {
                        "term": {
                          "ownerId": {userId}  // Replace {userId} with the actual user ID
                        }
                      }
                    ]
                  }
                },
                {
                  "bool": {
                    "must": [
                      {
                        "query_string": {
                          "query": "*{query}*",  // Replace {query} with the actual query string
                          "fields": ["note"],
                          "default_operator": "AND"
                        }
                      },
                      {
                        "terms": {
                          "sharedWithUsers": [{userId}]  // Replace {userId} with the actual user ID
                        }
                      }
                    ]
                  }
                }
              ]
            }
          },
          "highlight": {
            "fields": {
              "note": {}
            },
            "require_field_match": true
          }
        }
    */
    //================================================================================================================
    //================================================================================================================
    public static String buildMultiIndexMatchBody(String query, int userId) {
        return "{\n" +
                "\"from\": 0,\n" +
                "\"size\": 100,\n" +
                "\"track_total_hits\": true,\n" +
                "\"sort\" : {\n" +
                "      \"id\": {\"order\": \"asc\"}\n" +
                "      },\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"should\": [\n" +
                "        {\"bool\": {\n" +
                "          \"must\": [\n" +
                "            {\"query_string\": {\"query\": \"*" + query + "*\",\"fields\": [\"note\"], \"default_operator\": \"AND\"}},\n" +
                "            {\"term\": {\"ownerId\": " + userId + "}}\n" +
                "          ]\n" +
                "        }},\n" +
                "        {\"bool\": {\n" +
                "          \"must\": [\n" +
                "            {\"query_string\": {\"query\": \"*" + query + "*\",\"fields\": [\"note\"], \"default_operator\": \"AND\"}},\n" +
                "            {\"terms\": {\"sharedWithUsers\": [" + userId + "]}}\n" +
                "          ]\n" +
                "        }}\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  \"highlight\": {\n" +
                "    \"fields\": {\n" +
                "      \"note\": {}\n" +
                "    },\n" +
                "    \"require_field_match\": true\n" +
                " }\n" +
                "}";
    }




    public static String buildSearchUri(String elasticSearchUri,
                                        String elasticSearchIndex,
                                        String elasticSearchSearch) {
        return elasticSearchUri + elasticSearchIndex + elasticSearchSearch;
    }
}
