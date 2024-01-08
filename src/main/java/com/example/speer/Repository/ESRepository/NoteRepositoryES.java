package com.example.speer.Repository.ESRepository;

import com.example.speer.Entities.ElasticsearchDocuments.NoteEntityES;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepositoryES extends ElasticsearchRepository<NoteEntityES, Integer>{
    NoteEntityES findByNoteMySqlId(int noteMySqlId);

    void deleteByNoteMySqlId(int noteMySqlId);
}
