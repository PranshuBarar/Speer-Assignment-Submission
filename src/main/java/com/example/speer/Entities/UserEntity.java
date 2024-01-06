package com.example.speer.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;
    private String userEmail;
    private String password;
    private String username;

    @OneToMany(mappedBy ="userEntity", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<NoteEntity> SelfNotesList = new ArrayList<>();

}
