package com.example.speer.Entities;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "shared_notes")
public class SharedNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "note_id")
    private NoteEntity noteEntity;

    @ManyToOne
    @JoinColumn
    private UserEntity sharedWithUser;
}
