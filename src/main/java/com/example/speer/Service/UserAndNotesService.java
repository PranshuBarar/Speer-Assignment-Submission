package com.example.speer.Service;

import com.example.speer.Entities.NoteEntity;
import com.example.speer.Entities.UserEntity;
import com.example.speer.EntryDtos.UserEntryDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;

import java.util.List;

public interface UserAndNotesService {

    List<Object> getAllNotes() throws EntityNotFoundException, SessionAuthenticationException;

    String createNote(String note) throws AccessDeniedException;

    Object getNoteById(int noteId) throws Exception;

    String updateNote(String note, int noteId) throws Exception;

    String deleteNote(int noteId) throws Exception;

    String shareNote(int noteId, int recipientId) throws Exception;

}
