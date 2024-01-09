package com.example.speer.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;


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
    private int sharingTransactionId;

    @ManyToOne
    @JoinColumn(name = "note_id")
    private NoteEntity noteEntity;

    @ManyToOne
    @JoinColumn
    private UserEntity sharedWithUser;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedNote that = (SharedNote) o;
        return sharingTransactionId == that.sharingTransactionId && Objects.equals(noteEntity, that.noteEntity) && Objects.equals(sharedWithUser, that.sharedWithUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sharingTransactionId, noteEntity, sharedWithUser);
    }

    @Override
    public String toString() {
        return noteEntity.getNote();
    }
}
