package com.example.speer.Repository;

import com.example.speer.Entities.NoteEntity;
import com.example.speer.Entities.NoteEntityES;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepositoryES extends ElasticsearchRepository<NoteEntityES, String>{
//    public NoteEntityES findByNoteMySqlId(int noteMySqlId);
//
//    public void deleteByNoteMySqlId(int noteMySqlId);
}
