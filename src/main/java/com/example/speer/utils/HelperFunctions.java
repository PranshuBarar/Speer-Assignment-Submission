package com.example.speer.utils;

import org.json.JSONArray;

import java.util.Arrays;

public class HelperFunctions {

    private HelperFunctions() {}

    private static final String SEARCH_FIELD = "note";

//    /**
//     * Construct the query body for multi index matching
//     *
//     * @param query String
//     * @return String
//     */

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
                "      \"must\": [\n" +
                "        {\"query_string\": {\"query\": \"*" + query + "*\",\"fields\": [\"" + SEARCH_FIELD + "\"], \"default_operator\": \"AND\"}},\n" +
                "        {\"term\": {\"ownerId\": " + userId + "}},\n" +
                "        {\"terms\": {\"sharedWithUsers\": [" + userId + "]}}\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  \"highlight\": {\n" +
                "    \"fields\": {\n" +
                "      \"*\": {}\n" +
                "    },\n" +
                "    \"require_field_match\": true\n" +
                " }\n" +
                "}";
    }


//    public static String buildMultiIndexMatchBody(String query, int currentuUserId) {
//        return "{\n" +
//                "\"from\": 0,\n" +
//                "\"size\": 100,\n" +
//                "\"track_total_hits\": true,\n" +
//                "\"sort\" : {\n" +
//                "      \"id\": {\"order\": \"asc\"}\n" +
//                "      },\n" +
//                "  \"query\": {\n" +
//                "    \"query_string\" : {\n" +
//                "      \"query\":      \"*" + query + "*\",\n" +
//                "      \"fields\":     [\"" + searchField + "\",\n" +
//                "      \"default_operator\": \"AND\"\n" +
//                "    }\n" +
//                "  },\n" +
//                "  \"highlight\": {\n" +
//                "    \"fields\": {\n" +
//                "      \"*\": {}\n" +
//                "    },\n" +
//                "    \"require_field_match\": true\n" +
//                " }\n" +
//                "}";
//    }

    public static String buildSearchUri(String elasticSearchUri,
                                        String elasticSearchIndex,
                                        String elasticSearchSearch) {
        return elasticSearchUri + elasticSearchIndex + elasticSearchSearch;
    }
}
