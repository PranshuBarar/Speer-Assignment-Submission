package com.example.speer.Entities.ElasticsearchDocuments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "note_index", createIndex = true)
public class NoteEntityES {

    @Id
    private int id;

    private String note;

    private int ownerId;

    private Set<Integer> sharedWithUsers = new HashSet<>();

}
