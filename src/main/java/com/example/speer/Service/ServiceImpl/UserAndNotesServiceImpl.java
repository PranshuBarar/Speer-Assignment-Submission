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
                    .filter(note -> note.getSharedWithUser().getUserId() == currentUserId)
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
            noteId = updatedNoteEntity.getNoteId();
        }



        //Now we will save the noteEntityES in the Elasticsearch db
        NoteEntityES noteEntityES = NoteEntityES.builder()
                .note(note)
                .ownerId(userEntity.getUserId())
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
                    .filter(note -> note.getNoteId() == noteId)
                    .findFirst()
                    .orElse(null);

            if(noteEntity != null){
                return noteEntity;
            }
            else {
                SharedNote sharedNote = sharedNoteRepository
                        .findAll()
                        .stream()
                        .filter(note -> note.getSharedWithUser().getUserId() == currentUserId)
                        .findFirst()
                        .orElse(null);
                if(sharedNote == null){
                    throw new Exception("We are sorry, neither you are owner, nor this note is shared with you");
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
            int noteMySqlId = matchingNote.getNoteId();
            NoteEntityES noteEntityES = noteRepositoryES.findByNoteMySqlId(noteMySqlId);
            noteEntityES.setNote(note);
            noteRepositoryES.save(noteEntityES);


            return "Note updated successfully";
        }
        throw new AccessDeniedException("You are not authorized");
    }

    @Transactional
    public String deleteNote(int noteId) throws Exception {
        //================================================================
        /*First of all we will find the userId of current authenticated user
         * and then we will fetch the UserEntity from the database using that userId*/

        int currentUserId = getCurrentUserId();
        Optional<UserEntity> optionalUserEntity = userRepository.findById(currentUserId);
        if (optionalUserEntity.isPresent()) {
            UserEntity userEntity = optionalUserEntity.get();

            //================================================================
            /*Now we will fetch the NoteEntity from the database using given noteId*/
            NoteEntity matchingNote = getNoteEntity(noteId, userEntity);

            /*If NoteEntity is null we will throw the exception that either this noteEntity with this noteId
             * does not exist or the current user is not the owner*/
            if (matchingNote == null) {
                throw new AccessDeniedException("You are not allowed to modify as either this note with id " +
                        +noteId + " does not exist or you are not the owner of this note");
            }

            /*If execution flow reaches here it means note exists*/

            //Here we are deleting the note from noteRepo
            userEntity.getSelfNotesList().remove(matchingNote);
            userRepository.save(userEntity);

            // Delete from Elasticsearch also
            noteRepositoryES.deleteByNoteMySqlId(matchingNote.getNoteId());

            return "Note deleted successfully";
        }
        throw new AccessDeniedException("You are not authorized");
    }

    public String shareNote(int noteId, int recipientId) throws Exception{
        //================================================================
        /*First of all we will find the userId of current authenticated user
        * and then we will fetch the UserEntity from the database using that userId
        * Also we will find the recipient UserEntity from the database using given recipientId*/

        int currentUserId = getCurrentUserId();
        Optional<UserEntity> optionalCurrentUserEntity = userRepository.findById(currentUserId);
        Optional<UserEntity> optionalRecipientUserEntity = userRepository.findById(recipientId);
        //================================================================
        if(optionalRecipientUserEntity.isPresent() && optionalCurrentUserEntity.isPresent()) {

            /*Now if the execution flow reaches this block it means currentUserEntity and recipientUserEntity
            have been found in the database hence we will extract them from the optional objects
            * */
            UserEntity currentUserEntity = optionalCurrentUserEntity.get();
            UserEntity recipientUserEntity = optionalRecipientUserEntity.get();

            //==========================================================
            /*Now we will find the NoteEntity for the given noteId from the database
            * Refer to the getNoteEntity() function for the process of finding NoteEntity*/
            //==========================================================
            NoteEntity matchingNote = getNoteEntity(noteId, currentUserEntity);

            //If matchingNote is not found, it means either user doesn't owns this note
            // or he never created this note. Therefore, we will throw an exception
            if(matchingNote == null){
                throw new AccessDeniedException("You are not allowed to share as you are not the owner of this note");
            }

            //==========================================================
            /*Now if the execution flow reaches here it means matchingNote has been found now
            * we will check that whether this note has already been shared with the recipient or not*/

            /* Pseudo Code for checking
            //==========================

            * if(sharedNoteRepo contains matchingNote
            * [we will check this by comparing the noteEntity.getId() of each sharedNote]
            * then)
            *
            *       {
            *           if the above condition is met it means this matching Note is in
            *           the sharedNoteRepo ->
            *                  (then we will check that whether that note has been shared with
            *                   recipient by comparing sharedUser.getId() with recipientId)
            *
            *                 {
            *                     if the execution flow reaches this block, it means that this owner has already shared
            *                     this note with the recipient, hence no need to share it again
            *                 }
            *
            *       }
            */

            //================================================================
            //Implementation of above pseudo code
            //================================================================
            {
                SharedNote sharedNote = sharedNoteRepository.findByNoteEntity(matchingNote);

                if (sharedNote != null) {
                    if (sharedNote.getSharedWithUser().getUserId() == recipientId) {
                        return "Note has already been shared with the recipient";
                    }
                }
            }
            //================================================================

            /*
            * If code reaches here it means note has not been shared with recipient
            Hence now we will make a new entry in sharedNote table and share this note
            with the recipient
            *
            */

            //Let's create a new sharedNote
            SharedNote sharedNote = SharedNote.builder()
                    .sharedWithUser(recipientUserEntity)
                    .noteEntity(matchingNote)
                    .build();

            //Save this sharedNote in the repository
            sharedNoteRepository.save(sharedNote);

            //================================================================
            //Also update the information in Elasticsearch
            NoteEntityES noteEntityES = noteRepositoryES.findByNoteMySqlId(noteId);
            noteEntityES.getSharedWithUsers().add(recipientId);

            noteRepositoryES.save(noteEntityES);
            //================================================================

            return "Note successfully shared";
        }

        //If code reaches this block it means it never entered the above if block
        //Hence we will throw an exception
        throw new AccessDeniedException("You are not authorized");
    }

    //============================================================================================
    //These are helper methods for the above methods
    //============================================================================================

    public static NoteEntity getNoteEntity(int noteId, UserEntity userEntity) {

        return userEntity.getSelfNotesList()
                .stream()
                .filter(noteEntity -> noteEntity.getNoteId() == noteId)
                .findFirst()
                .orElse(null);
    }

    public int getCurrentUserId()  {
        CustomUserDetails customUserDetails = getCurrentUserDetails();
        if(customUserDetails != null){
            return customUserDetails.getUserEntity().getUserId();
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
