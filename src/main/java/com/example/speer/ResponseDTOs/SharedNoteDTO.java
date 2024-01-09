package com.example.speer.ResponseDTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@Builder
public class SharedNoteDTO {
    private int sharedWithUserId;
    private int sharingTransactionId;
    private int noteId;
    private String note;

    public SharedNoteDTO(int sharedWithUserId, int sharingTransactionId, int noteId, String note) {
        this.sharedWithUserId = sharedWithUserId;
        this.sharingTransactionId = sharingTransactionId;
        this.noteId = noteId;
        this.note = note;
    }

    @Override
    public String toString() {
        return note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedNoteDTO that = (SharedNoteDTO) o;
        return noteId == that.noteId && Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noteId, note);
    }
}
