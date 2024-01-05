package com.example.speer.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "notes")
public class NoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String note;

    @ManyToOne
    @JoinColumn
    UserEntity userEntity;

    @ManyToMany(mappedBy = "sharedNotes")
    private Set<UserEntity> sharedWithUsers = new HashSet<>();
}
