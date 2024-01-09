package com.example.speer.ResponseDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Objects;

@AllArgsConstructor
@Builder
public class NoteEntityDTO {
    private int noteId;

    private String note;

    private int userId;

    @Override
    public String toString() {
        return note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteEntityDTO that = (NoteEntityDTO) o;
        return noteId == that.noteId && userId == that.userId && Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noteId, note, userId);
    }
}
