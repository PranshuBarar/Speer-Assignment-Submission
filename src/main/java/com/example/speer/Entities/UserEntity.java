package com.example.speer.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String userEmail;
    private String password;
    private String username;

    @OneToMany(mappedBy ="userEntity", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<NoteEntity> SelfNotesList = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "shared_notes",
            joinColumns = @JoinColumn(name = "shared_with_user"),
            inverseJoinColumns = @JoinColumn(name = "shared_note")
    )
    private Set<NoteEntity> sharedNotes = new HashSet<>();
}
