package com.example.speer.Service;

import com.example.speer.utils.CustomQuery;

import java.io.IOException;

public interface ElasticSearchService {
    public CustomQuery searchQuery(String lowerCase) throws IOException;
}
