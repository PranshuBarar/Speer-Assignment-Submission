package com.example.speer.Service.ServiceImpl;

import com.example.speer.Entities.NoteEntity;
import com.example.speer.Entities.ElasticsearchDocuments.NoteEntityES;
import com.example.speer.Entities.SharedNote;
import com.example.speer.Entities.UserEntity;
import com.example.speer.Repository.ESRepository.NoteRepositoryES;
import com.example.speer.Repository.NoteRepository;
import com.example.speer.Repository.SharedNoteRepository;
import com.example.speer.Repository.UserRepository;
import com.example.speer.ResponseDTOs.NoteEntityDTO;
import com.example.speer.ResponseDTOs.SharedNoteDTO;
import com.example.speer.Service.UserAndNotesService;
import com.example.speer.config.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserAndNotesServiceImpl implements UserAndNotesService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    NoteRepository noteRepository;

    @Autowired
    NoteRepositoryES noteRepositoryES;

    @Autowired
    SharedNoteRepository sharedNoteRepository;

    /*This constructor has been made here as it is specifically required in UserAndNotesServiceImplTest class*/
    public UserAndNotesServiceImpl(UserRepository userRepository, NoteRepository noteRepository, NoteRepositoryES noteRepositoryES, SharedNoteRepository sharedNoteRepository) {
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
        this.noteRepositoryES = noteRepositoryES;
        this.sharedNoteRepository = sharedNoteRepository;
    }

    public List<Object> getAllNotes() throws EntityNotFoundException,SessionAuthenticationException  {

        /*
            The process for getting all notes is like this:
            1-First we will fetch the current authenticated user entity
            2-Retrieve all the self notes (notes owned by the user)
            3-Convert them into DTO and fill them in an empty array list
            4-Retrieve all the shared notes (notes shared with the user by some other user)
            5-Convert them into DTO and add them in the same arraylist
            6-Return the list
        */


        //We will first have to find the current authenticated user
        int currentUserId = getCurrentUserId();
        //We will fetch the userEntity for this userId
        Optional<UserEntity> optionalUserEntity = userRepository.findById(currentUserId);
        if(optionalUserEntity.isPresent()){
            UserEntity userEntity = optionalUserEntity.get();

            //We will initialize an empty arraylist which will be return afterwords
            List<Object> allNotes = new ArrayList<>();

            //We will fetch the note entities of this user
            List<NoteEntity> selfNotesList = userEntity.getSelfNotesList();

            //If the above list is not empty will convert it into DTO and add in the arraylist to be returned
            if(!selfNotesList.isEmpty()){
                List<NoteEntityDTO> selfNotesDTOList = selfNotesList
                        .stream()
                        .map(note -> new NoteEntityDTO(
                                note.getNoteId(),
                                note.getNote(),
                                note.getUserEntity().getUserId()))
                        .toList();
                allNotes.addAll(selfNotesDTOList);
            }

            //Now we will fetch all the sharedNotes
            List<SharedNote> sharedNotesList = sharedNoteRepository.findAll();

            //If the above list is not empty will convert it into DTO and add in the arraylist to be returned
            List<SharedNoteDTO> sharedNoteDTOList;
            if(!sharedNotesList.isEmpty()){
                sharedNoteDTOList = sharedNotesList
                        .stream()
                        .filter(note -> note.getSharedWithUser().getUserId() == currentUserId)
                        .map(note -> new SharedNoteDTO(
                                note.getSharedWithUser().getUserId(),
                                note.getSharingTransactionId(),
                                note.getNoteEntity().getNoteId(),
                                note.getNoteEntity().getNote()))
                        .toList();
                allNotes.addAll(sharedNoteDTOList);
            }

            //We will return the arraylist
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
        NoteEntity updatedNoteEntity = updateUserEntity.getSelfNotesList().get(listSize - 1);
        int noteId = updatedNoteEntity.getNoteId();
        NoteEntityES noteEntityES = NoteEntityES.builder()
                .note(note)
                .ownerId(userEntity.getUserId())
                .id(noteId)
                .build();

        //We will also save the created note in the Elasticsearch database
        noteRepositoryES.save(noteEntityES);

        //Return the response
        return "Note successfully created";
    }

    //============================================================================================
    //============================================================================================
    public Object getNoteById(int noteId) throws Exception {
        //we will get the current authenticated user Id
        int currentUserId = getCurrentUserId();

        //Find the userEntity for that userId from the userRepository
        Optional<UserEntity> optionalUserEntity = userRepository.findById(currentUserId);

        if(optionalUserEntity.isPresent()) {
            UserEntity userEntity = optionalUserEntity.get();

            //We will fetch the note with asked noteId
            NoteEntity noteEntity = userEntity
                    .getSelfNotesList()
                    .stream()
                    .filter(note -> note.getNoteId() == noteId)
                    .findFirst()
                    .orElse(null);

            if(noteEntity != null){
                return noteEntity;
            }

            //If the execution flow reaches here it means note is not owned by the user
            //Now we will check whether the note has been shared by someone to this user
            //If yes then we will return that
            else {
                SharedNote sharedNote = sharedNoteRepository
                        .findAll()
                        .stream()
                        .filter(note -> note.getSharedWithUser().getUserId() == currentUserId)
                        .findFirst()
                        .orElse(null);

                //Now if still sharedNote is null, it means neither the note owned by this user nor shared
                if(sharedNote == null){
                    throw new IllegalAccessException("We are sorry, neither you are owner, nor this note is shared with you by any user");
                }
                else return sharedNote;
            }
        }
        throw new IllegalAccessException("You are not authenticated");
    }
    public String updateNote(String note, int noteId) throws Exception {
        //we will get the current authenticated user Id
        int currentUserId = getCurrentUserId();

        //Find the userEntity for that userId from the userRepository
        Optional<UserEntity> optionalUserEntity = userRepository.findById(currentUserId);

        if(optionalUserEntity.isPresent()) {
            UserEntity userEntity = optionalUserEntity.get();

            //We find the note with given noteId
            NoteEntity matchingNote = getNoteEntity(noteId, userEntity);

            //If note has not been found, it means the note is not owned by this user
            if(matchingNote == null){
                throw new AccessDeniedException("You are not allowed to modify as you are not the owner of this note");
            }

            //Here we are updating the note in MySQL DB
            matchingNote.setNote(note);
            noteRepository.save(matchingNote);

            //Here we are updating the note in ElasticSearch as well
            int noteMySqlId = matchingNote.getNoteId();
            NoteEntityES noteEntityES = noteRepositoryES.findById(noteMySqlId);
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

            noteRepository.deleteById(noteId);

            // Delete from Elasticsearch also
            noteRepositoryES.deleteById(matchingNote.getNoteId());

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
            * If execution flow reaches here it means note has not been shared with recipient
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
            NoteEntityES noteEntityES = noteRepositoryES.findById(noteId);
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
    //These are helper methods for the above methods. These can be kept private also but as these functions are
    //also being called by CustomElasticSearchServiceImpl class hence, I have kept them public
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
