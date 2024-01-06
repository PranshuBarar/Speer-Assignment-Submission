package com.example.speer.Service.ServiceImpl;

import com.example.speer.Entities.NoteEntity;
import com.example.speer.Entities.NoteEntityES;
import com.example.speer.Entities.SharedNote;
import com.example.speer.Entities.UserEntity;
import com.example.speer.Repository.ESRepo.NoteRepositoryES;
import com.example.speer.Repository.NoteRepository;
import com.example.speer.Repository.SharedNoteRepository;
import com.example.speer.Repository.UserRepository;
import com.example.speer.config.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserAndNotesServiceImpl {

    @Autowired
    UserRepository userRepository;

    @Autowired
    NoteRepository noteRepository;

    @Autowired
    NoteRepositoryES noteRepositoryES;

    @Autowired
    SharedNoteRepository sharedNoteRepository;


    private static final Logger LOGGER = LoggerFactory.getLogger(UserAndNotesServiceImpl.class);

    public String helloUser() throws AuthenticationException {
        int currentUserId = getCurrentUserId();
        return "Hello User with userId: " + currentUserId;
    }

    public List<Object> getAllNotes() throws EntityNotFoundException,SessionAuthenticationException  {
        //We will first have to find the current authenticated user
        int currentUserId = getCurrentUserId();
        Optional<UserEntity> optionalUserEntity = userRepository.findById(currentUserId);
        if(optionalUserEntity.isPresent()){
            UserEntity userEntity = optionalUserEntity.get();

            List<NoteEntity> selfNotesList = userEntity.getSelfNotesList();
            List<SharedNote> sharedNotesList = sharedNoteRepository.findAll()
                    .stream()
                    .filter(note -> note.getSharedWithUser().getId() == currentUserId)
                    .toList();

            List<Object> allNotes = new ArrayList<>(selfNotesList);
            allNotes.addAll(sharedNotesList);

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
                .userEntity(userEntity)
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



        //Now we will save the noteEntityES in the Elasticsearch db
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
    public Object getNoteById(int noteId) throws Exception {
        int currentUserId = getCurrentUserId();
        Optional<UserEntity> optionalUserEntity = userRepository.findById(currentUserId);
        if(optionalUserEntity.isPresent()) {
            UserEntity userEntity = optionalUserEntity.get();
            NoteEntity noteEntity = userEntity
                    .getSelfNotesList()
                    .stream()
                    .filter(note -> note.getId() == noteId)
                    .findFirst()
                    .orElse(null);

            if(noteEntity != null){
                return noteEntity;
            }
            else {
                SharedNote sharedNote = sharedNoteRepository
                        .findAll()
                        .stream()
                        .filter(note -> note.getSharedWithUser().getId() == currentUserId)
                        .findFirst()
                        .orElse(null);
                if(sharedNote == null){
                    throw new AccessDeniedException("We are sorry, neither you are owner, nor this note is shared with you");
                }
                else return sharedNote;
            }
        }
        throw new IllegalAccessException("You are not authenticated");
    }
    public String updateNote(String note, int noteId) throws Exception {
        int currentUserId = getCurrentUserId();
        Optional<UserEntity> optionalUserEntity = userRepository.findById(currentUserId);
        if(optionalUserEntity.isPresent()) {
            UserEntity userEntity = optionalUserEntity.get();
            NoteEntity matchingNote = getNoteEntity(noteId, userEntity);

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

    @Transactional
    public String deleteNote(int noteId) throws Exception {
        int currentUserId = getCurrentUserId();
        Optional<UserEntity> optionalUserEntity = userRepository.findById(currentUserId);
        if(optionalUserEntity.isPresent()) {
            UserEntity userEntity = optionalUserEntity.get();
            NoteEntity matchingNote = getNoteEntity(noteId, userEntity);

            if(matchingNote == null){
                throw new AccessDeniedException("You are not allowed to modify as you are not the owner of this note");
            }

            //Here we are deleting the note in MySQL DB
            noteRepository.deleteById(matchingNote.getId());

            // Delete from Elasticsearch also
            noteRepositoryES.deleteByNoteMySqlId(matchingNote.getId());

            System.out.println(noteId);
            System.out.println(matchingNote.getId());
            System.out.println(matchingNote.getNote());
            return "Note deleted successfully";
        }
        throw new AccessDeniedException("You are not authorized");
    }

    public String shareNote(int noteId, int recipientId) throws Exception{
        int currentUserId = getCurrentUserId();
        Optional<UserEntity> optionalCurrentUserEntity = userRepository.findById(currentUserId);
        Optional<UserEntity> optionalRecipientUserEntity = userRepository.findById(recipientId);
        if(optionalRecipientUserEntity.isPresent() && optionalCurrentUserEntity.isPresent()) {
            UserEntity currentUserEntity = optionalCurrentUserEntity.get();
            UserEntity recipientUserEntity = optionalRecipientUserEntity.get();

            NoteEntity matchingNote = getNoteEntity(noteId, currentUserEntity);
            if(matchingNote == null){
                throw new AccessDeniedException("You are not allowed to share as you are not the owner of this note");
            }

            SharedNote sharedNote = SharedNote.builder()
                    .sharedWithUser(recipientUserEntity)
                    .noteEntity(matchingNote)
                    .build();

            sharedNoteRepository.save(sharedNote);

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

    public static NoteEntity getNoteEntity(int noteId, UserEntity userEntity) {

        return userEntity.getSelfNotesList()
                .stream()
                .filter(noteEntity -> noteEntity.getId() == noteId)
                .findFirst()
                .orElse(null);
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
