package com.example.speer.Service.ServiceImpl;

import com.example.speer.Entities.NoteEntity;
import com.example.speer.Entities.NoteEntityES;
import com.example.speer.Entities.UserEntity;
import com.example.speer.Repository.NoteRepositoryES;
import com.example.speer.Repository.NoteRepository;
import com.example.speer.Repository.UserRepository;
import com.example.speer.config.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserAndNotesServiceImpl {

    @Autowired
    UserRepository userRepository;

    @Autowired
    NoteRepository noteRepository;

    @Autowired
    NoteRepositoryES noteRepositoryES;


    public String helloUser() throws AuthenticationException {
        int currentUserId = getCurrentUserId();
        return "Hello User with userId: " + currentUserId;
    }

    public List<NoteEntity> getAllNotes() throws EntityNotFoundException,SessionAuthenticationException  {
        //We will first have to find the current authenticated user
        int currentUserId = getCurrentUserId();
        Optional<UserEntity> optionalUserEntity = userRepository.findById(currentUserId);
        if(optionalUserEntity.isPresent()){
            UserEntity userEntity = optionalUserEntity.get();

            List<NoteEntity> selfNotesList = userEntity.getSelfNotesList();
            Set<NoteEntity> sharedNotes = userEntity.getSharedNotes();

            List<NoteEntity> allNotes = new ArrayList<>(selfNotesList);
            allNotes.addAll(sharedNotes);

            return allNotes;

        } else {
            throw new EntityNotFoundException("No user with this ID "+ currentUserId);
        }
    }

    public String createNote(String note) throws AccessDeniedException {
        //we will get the current authenticated user Id
        int currentUserId = getCurrentUserId();

        //Find the userEntity for that userId from the userRepository
        UserEntity userEntity = userRepository.findById(currentUserId).get();

        //We will create a new note
        NoteEntity noteEntity = NoteEntity
                .builder()
                .note(note)
                .build();

        //Here we will add this new note to the notelist of current authenticated user
        userEntity.getSelfNotesList().add(noteEntity);

        //Now since UserEntity is parent of NoteEntity hence we will only save the
        //UserEntity, due to cascading effect NoteEntity will automatically get saved
        UserEntity updateUserEntity = userRepository.save(userEntity);
        int listSize = updateUserEntity.getSelfNotesList().size();
        int noteId;
        if(listSize == 0) {
            noteId = 1;
        } else{
            NoteEntity updatedNoteEntity = updateUserEntity.getSelfNotesList().get(listSize - 1);
            noteId = updatedNoteEntity.getId();
        }



        //Now we will save the noteentityES in the Elasticsearch db
        NoteEntityES noteEntityES = NoteEntityES.builder()
                .note(note)
                .ownerId(userEntity.getId())
                .noteMySqlId(noteId)
                .build();

        noteRepositoryES.save(noteEntityES);

        return "Note successfully created";
    }




    //============================================================================================
    //============================================================================================
    public NoteEntity getNoteById(int noteId) throws Exception {
        int currentUserId = getCurrentUserId();
        Optional<UserEntity> optionalUserEntity = userRepository.findById(currentUserId);
        if(optionalUserEntity.isPresent()) {
            UserEntity userEntity = optionalUserEntity.get();

            //it means this user is not the owner of this note
            //Now we will check whether this note has been shared with this user or not
            NoteEntity matchingNote = userEntity.getSelfNotesList()
                    .stream()
                    .filter(noteEntity -> noteEntity.getId() == noteId)
                    .findFirst()
                    .orElse(null);


            if (matchingNote == null) {
                matchingNote = userEntity.getSharedNotes()
                        .stream()
                        .filter(noteEntity -> noteEntity.getId() == noteId)
                        .findFirst()
                        .orElse(null);
                if(matchingNote == null) {
                    throw new IllegalStateException("Neither you are owner nor this note has been shared with you, hence not allowed");
                }
                else return matchingNote;
            }
        }
        else {
            throw new IllegalAccessException("You are not authenticated");
        }
        return null;
    }
    public String updateNote(String note, int noteId) throws Exception {
        int currentUserId = getCurrentUserId();
        Optional<UserEntity> optionalUserEntity = userRepository.findById(currentUserId);
        if(optionalUserEntity.isPresent()) {
            NoteEntity matchingNote = getNoteEntity(noteId, optionalUserEntity);

            if(matchingNote == null){
                throw new AccessDeniedException("You are not allowed to modify as you are not the owner of this note");
            }

            //Here we are updating the note in MySQL DB
            matchingNote.setNote(note);
            noteRepository.save(matchingNote);

            //Here we are updating the note in ElasticSearch as well
            int noteMySqlId = matchingNote.getId();
            NoteEntityES noteEntityES = noteRepositoryES.findByNoteMySqlId(noteMySqlId);
            noteEntityES.setNote(note);
            noteRepositoryES.save(noteEntityES);


            return "Note updated successfully";
        }
        throw new AccessDeniedException("You are not authorized");
    }

    public String deleteNote(int noteId) throws Exception {
        int currentUserId = getCurrentUserId();
        Optional<UserEntity> optionalUserEntity = userRepository.findById(currentUserId);
        if(optionalUserEntity.isPresent()) {
            NoteEntity matchingNote = getNoteEntity(noteId, optionalUserEntity);

            if(matchingNote == null){
                throw new AccessDeniedException("You are not allowed to modify as you are not the owner of this note");
            }

            //Here we are deleting the note in MySQL DB
            noteRepository.deleteById(noteId);

            //Here we are deleting the note in ElasticSearch as well
            noteRepositoryES.deleteByNoteMySqlId(noteId);


            return "Note deleted successfully";
        }
        throw new AccessDeniedException("You are not authorized");
    }

    public String shareNote(int noteId, int recipientId) throws Exception{
        int currentUserId = getCurrentUserId();
        Optional<UserEntity> optionalUserEntity = userRepository.findById(currentUserId);
        if(optionalUserEntity.isPresent()) {
            NoteEntity matchingNote = getNoteEntity(noteId, optionalUserEntity);
            if(matchingNote == null){
                throw new AccessDeniedException("You are not allowed to share as you are not the owner of this note");
            }

            UserEntity recipientUserEntity = userRepository.findById(recipientId).get();
            matchingNote.getSharedWithUsers().add(recipientUserEntity);
            recipientUserEntity.getSharedNotes().add(matchingNote);

            noteRepository.save(matchingNote);
            userRepository.save(recipientUserEntity);

            NoteEntityES noteEntityES = noteRepositoryES.findByNoteMySqlId(noteId);
            noteEntityES.getSharedWithUsers().add(recipientId);
            noteRepositoryES.save(noteEntityES);

            return "Note successfully shared";
        }
        throw new AccessDeniedException("You are not authorized");
    }









    //============================================================================================
    //These are helper methods for the above methods
    //============================================================================================

    public static NoteEntity getNoteEntity(int noteId, Optional<UserEntity> optionalUserEntity) {
        UserEntity userEntity = optionalUserEntity.get();

        NoteEntity matchingNote = userEntity.getSelfNotesList()
                .stream()
                .filter(noteEntity -> noteEntity.getId() == noteId)
                .findFirst()
                .orElse(null);
        return matchingNote;
    }

    public int getCurrentUserId()  {
        CustomUserDetails customUserDetails = getCurrentUserDetails();
        if(customUserDetails != null){
            return customUserDetails.getUserEntity().getId();
        }
        else {
            throw new SessionAuthenticationException("This session is not authenticated");
        }
    }

    public CustomUserDetails getCurrentUserDetails(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() instanceof CustomUserDetails){
            return (CustomUserDetails) authentication.getPrincipal();
        }
        return null;
    }
}
