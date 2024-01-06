package com.example.speer.Entities;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "note_index", createIndex = true)
public class NoteEntityES {

    @Id
    private String id = UUID.randomUUID().toString();

    private String note;

    private int noteMySqlId;

    private int ownerId;

    private Set<Integer> sharedWithUsers = new HashSet<>();

}
