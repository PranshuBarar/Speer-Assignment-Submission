package com.example.speer.Repository.ESRepository;

import com.example.speer.Entities.ElasticsearchDocuments.NoteEntityES;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepositoryES extends ElasticsearchRepository<NoteEntityES, Integer>{
    NoteEntityES findById(int noteMySqlId);

    void deleteById(int noteMySqlId);
}
