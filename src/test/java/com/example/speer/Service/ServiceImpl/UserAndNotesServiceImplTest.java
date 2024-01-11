package com.example.speer.Service.ServiceImpl;

import com.example.speer.Entities.ElasticsearchDocuments.NoteEntityES;
import com.example.speer.Entities.NoteEntity;
import com.example.speer.Entities.SharedNote;
import com.example.speer.Entities.UserEntity;
import com.example.speer.Repository.ESRepository.NoteRepositoryES;
import com.example.speer.Repository.NoteRepository;
import com.example.speer.Repository.SharedNoteRepository;
import com.example.speer.Repository.UserRepository;
import com.example.speer.ResponseDTOs.NoteEntityDTO;
import com.example.speer.ResponseDTOs.SharedNoteDTO;
import com.example.speer.config.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

//@SpringBootTest
@Transactional
@ExtendWith(MockitoExtension.class)
class UserAndNotesServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    NoteRepository noteRepository;

    @Mock
    NoteRepositoryES noteRepositoryES;

    @Mock
    SharedNoteRepository sharedNoteRepository;


    private UserAndNotesServiceImpl userAndNotesServiceImpl;

    //================================================================================================================

    @BeforeEach
    void setUp() {
         this.userAndNotesServiceImpl = new UserAndNotesServiceImpl(userRepository,noteRepository,noteRepositoryES,sharedNoteRepository);
         int currentUserId = 1;
         UserEntity userEntity = createUserEntity(currentUserId,new ArrayList<>());
        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);
        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails,null);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);

        SecurityContextHolder.setContext(securityContext);
    }


    /*Testing getAllNotes() with three test cases:
    * 1-Empty self and empty shared notes
    * 2-Non-Empty self and empty shared notes
    * 3-Non-Empty self and non-empty shared notes*/
    //================================================================================================================
    //================================================================================================================
    @Test
    void getAllNotes_EmptySelfAndSharedNotes() {
        UserEntity userEntity = createUserEntity(1,new ArrayList<>());

        when(userRepository.findById(1)).thenReturn(Optional.of(userEntity));
        when(sharedNoteRepository.findAll()).thenReturn(new ArrayList<>());

        List<Object> actualResult = userAndNotesServiceImpl.getAllNotes();
        assertThat(actualResult.size()).isEqualTo(0);
    }

    @Test
    void getAllNotes_NonEmptySelfNotesEmptySharedNotes(){
        UserEntity userEntity = createUserEntity(1,new ArrayList<>());
        NoteEntity noteEntity = createNoteEntity(1,"This is a test note 1", userEntity);
        userEntity.getSelfNotesList().add(noteEntity);

        when(userRepository.findById(1)).thenReturn(Optional.of(userEntity));
        when(sharedNoteRepository.findAll()).thenReturn(new ArrayList<>());

        List<Object> expectedResult = new ArrayList<>();
        expectedResult.add(createNoteEntityDTO(noteEntity));

        List<Object> actualResult = userAndNotesServiceImpl.getAllNotes();

        assertThat(actualResult).containsExactlyInAnyOrderElementsOf(expectedResult);
    }

    @Test
    void getAllNotes_NonEmptySelfNotesNonEmptySharedNotes() {
        // Arrange the expectedResult
        List<Object> expectedResult = getExpectedResultForNonEmptySelfNotesNonEmptySharedNotes();

        // Act
        List<Object> actualResult = userAndNotesServiceImpl.getAllNotes();

        // Assert that the expectedResult is same as the actualResult
        assertThat(actualResult).containsExactlyInAnyOrderElementsOf(expectedResult);
    }
    //================================================================================================================
    //================================================================================================================


    /*Testing createNote()*/
    //================================================================================================================
    //================================================================================================================
    @ParameterizedTest
    @ValueSource(strings = {"Note 1", "Note 2", "Note 3"})
    void createNote(String inputNote) {
        UserEntity userEntity = createUserEntity(1,new ArrayList<>());

        when(userRepository.findById(1)).thenReturn(Optional.of(userEntity));

        userEntity.getSelfNotesList().add(new NoteEntity(1,inputNote,userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);

        String actualResult = userAndNotesServiceImpl.createNote(inputNote);
        String expectedResult = "Note successfully created";

        assertThat(actualResult).isEqualTo(expectedResult);

        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(noteRepositoryES, times(1)).save(any(NoteEntityES.class));
    }
    //================================================================================================================
    //================================================================================================================


    /*Testing getNoteById() with three test cases:
    * 1-When the user is owner of the note
    * 2-When user is note the owner but the note is shared with the use
    * 3-When neither the note has been shared nor the user owns it*/
    //================================================================================================================
    //================================================================================================================
    @Test
    void getNoteById_WhenUserIsOwner() throws Exception {
        int noteId = 1;
        UserEntity userEntity = createUserEntity(1,new ArrayList<>());
        NoteEntity noteEntity = createNoteEntity(1,"Test Note",userEntity);
        userEntity.getSelfNotesList().add(noteEntity);
        when(userRepository.findById(1)).thenReturn(Optional.of(userEntity));
        Object actualResult = userAndNotesServiceImpl.getNoteById(noteId);
        assertThat(actualResult).isEqualTo(noteEntity);

        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void getNoteById_WhenSharedWIthThisUser() throws Exception {
        //Let's say that note is being asked for the noteId = 2
        int noteId = 2;

        //Now first of all we will create a two userEntities
        UserEntity userEntity1 = createUserEntity(1,new ArrayList<>());
        UserEntity userEntity2 = createUserEntity(2,new ArrayList<>());

        //Now we will create a noteEntity with id = 2, which will be owned by userEntity2
        NoteEntity noteEntity = createNoteEntity(2,"Test Note", userEntity2);
        userEntity2.getSelfNotesList().add(noteEntity);

        //Now we will create a sharedNote by which we will share the above noteEntity with userEntity1
        //Mind it that this userEntity is actually owned by userEntity2 and our currently authenticated user
        //is userEntity1
        SharedNote sharedNote = createSharedNote(1,noteEntity,userEntity1);

        //Now we will setup when and then conditions
        when(userRepository.findById(1)).thenReturn(Optional.of(userEntity1));
        when(sharedNoteRepository.findAll()).thenReturn(new ArrayList<>(List.of(new SharedNote[]{sharedNote})));

        //Now as we see the note is being asked for noteId 2, which is shared with current user which is
        //userEntity1.
        // For this the expected result should be the above noteEntity
        Object expectedResult = sharedNote;

        //Now we will call the actual method
        Object actualResult = userAndNotesServiceImpl.getNoteById(noteId);

        //Now we will apply assertion
        assertThat(actualResult).isEqualTo(expectedResult);

        verify(userRepository, times(1)).findById(1);
        verify(sharedNoteRepository, times(1)).findAll();
    }

    @Test
    void getNoteById_WhenNeitherOwnedNorShared() {
        //Let's say that note is being asked for the noteId = 3
        int noteId = 2;

        //Now first of all we will create a two userEntities
        UserEntity userEntity1 = createUserEntity(1,new ArrayList<>());
        UserEntity userEntity2 = createUserEntity(2,new ArrayList<>());

        //Now we will create a noteEntity with id = 2, which will be owned by userEntity2
        NoteEntity noteEntity = createNoteEntity(2,"Test Note", userEntity2);
        userEntity2.getSelfNotesList().add(noteEntity);

        //Now we will setup when and then conditions
        when(userRepository.findById(1)).thenReturn(Optional.of(userEntity1));
        when(sharedNoteRepository.findAll()).thenReturn(new ArrayList<>());

        assertThrows(IllegalAccessException.class, () -> userAndNotesServiceImpl.getNoteById(noteId));
    }
    //================================================================================================================
    //================================================================================================================


    /*Testing updateNote() with two test cases:
    * 1-When the user is the owner of note
    * 2-When user is not the owner of the note*/
    //================================================================================================================
    //================================================================================================================
    @Test
    void updateNote_WhenUserIsOwner() throws Exception {
        //Let's say that note is being asked for the update is noteId = 1
        int noteId = 1;
        String updatedNote = "Updated Test Note";
        setUpUserAndNotes();
        //Now we will call the actual method
        String actualResult = userAndNotesServiceImpl.updateNote(updatedNote, noteId);
        String expectedResult = "Note updated successfully";

        assertThat(actualResult).isEqualTo(expectedResult);

        verify(userRepository, times(1)).findById(1);
        verify(noteRepositoryES, times(1)).findById(1);
        verify(noteRepositoryES, times(1)).save(any(NoteEntityES.class));

    }

    @Test
    void updateNote_WhenUserIsNotAnOwner() {
        //Let's say that note is being asked for the update is noteId = 1
        int noteId = 2;
        String updatedNote = "Updated Test Note";

        //Now first of all we will create a two userEntities
        setUpUserAndNotes();

        assertThrows(AccessDeniedException.class, () -> userAndNotesServiceImpl.updateNote(updatedNote, noteId));

        verify(userRepository, times(1)).findById(1);
    }
    //================================================================================================================
    //================================================================================================================

    /*Testing deleteNote() with two test cases:
    * 1-When user is the owner of note
    * 2-When user is not the owner of the note*/
    //================================================================================================================
    //================================================================================================================
    @Test
    void deleteNote_WhenUserIsOwnerOfTheNote() throws Exception {
        int noteId = 1;

        setUpUserAndNotes();

        String expectedResult = "Note deleted successfully";
        String actualResult = userAndNotesServiceImpl.deleteNote(noteId);

        assertThat(actualResult).isEqualTo(expectedResult);

        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(noteRepositoryES, times(1)).deleteById(noteId);
        verify(noteRepository, times(1)).deleteById(noteId);
    }

    @Test
    void deleteNote_WhenUserIsNotTheOwnerOfTheNote() {
        int noteId = 2;

        setUpUserAndNotes();

        assertThrows(AccessDeniedException.class, () -> userAndNotesServiceImpl.deleteNote(noteId));

        verify(userRepository, times(1)).findById(1);

    }
    //================================================================================================================
    //================================================================================================================



    //Testing shareNote() with two test cases:
    //1-When the user is owner of the note
    //2-When the user is not owner of the note
    //3-When the note has already been shared with user
    //================================================================================================================
    //================================================================================================================
    @Test
    void shareNote_WhenUserIsTheOwnerOfTheNote() throws Exception {
        int noteId = 1;
        int recipientId = 2;
        int currentUserId = 1;
        //First we create two userEntities, one the present user and another the recipient user
        UserEntity userEntity = createUserEntity(1,new ArrayList<>());
        UserEntity recipientEntity = createUserEntity(2,new ArrayList<>());

        //Now we will create a noteEntity with noteId 1 and will make userEntity as owner of the note
        NoteEntity noteEntity = createNoteEntity(noteId,"Test Note", userEntity);
        userEntity.getSelfNotesList().add(noteEntity);

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(userEntity));
        when(userRepository.findById(recipientId)).thenReturn(Optional.of(recipientEntity));
        when(sharedNoteRepository.findByNoteEntity(noteEntity)).thenReturn(null);
        when(noteRepositoryES.findById(noteId)).thenReturn(createNoteEntityES(noteId, currentUserId, noteEntity.getNote()));

        String expectedResult = "Note successfully shared";
        String actualResult = userAndNotesServiceImpl.shareNote(noteId,recipientId);

        assertThat(actualResult).isEqualTo(expectedResult);


        verify(userRepository, times(1)).findById(currentUserId);
        verify(userRepository, times(1)).findById(recipientId);

        verify(noteRepositoryES, times(1)).findById(noteId);
        verify(noteRepositoryES, times(1)).save(any(NoteEntityES.class));

        verify(sharedNoteRepository, times(1)).save(any(SharedNote.class));
        verify(sharedNoteRepository, times(1)).findByNoteEntity(noteEntity);

    }

    @Test
    void shareNote_WhenUserIsNotTheOwnerOfTheNote() {
        int noteId = 2;
        int recipientId = 2;
        int currentUserId = 1;
        //First we create two userEntities, one the present user and another the recipient user
        UserEntity userEntity = createUserEntity(1,new ArrayList<>());
        UserEntity recipientEntity = createUserEntity(2,new ArrayList<>());


        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(userEntity));
        when(userRepository.findById(recipientId)).thenReturn(Optional.of(recipientEntity));

        assertThrows(AccessDeniedException.class, () -> userAndNotesServiceImpl.shareNote(noteId,recipientId));

        verify(userRepository, times(1)).findById(currentUserId);
        verify(userRepository, times(1)).findById(recipientId);

    }

    @Test
    void shareNote_WhenNoteHasAlreadyBeenSharedWithRecipient() throws Exception {
        int noteId = 1;
        int recipientId = 2;
        int currentUserId = 1;
        //First we create two userEntities, one the present user and another the recipient user
        UserEntity userEntity = createUserEntity(1,new ArrayList<>());
        UserEntity recipientEntity = createUserEntity(2,new ArrayList<>());

        //Now we will create a noteEntity with noteId 1 and will make userEntity as owner of the note
        NoteEntity noteEntity = createNoteEntity(noteId,"Test Note", userEntity);
        userEntity.getSelfNotesList().add(noteEntity);

        //Now we will create a sharedNote which will imply that the note has already been shared with the recipient
        SharedNote sharedNote = createSharedNote(1,noteEntity,recipientEntity);

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(userEntity));
        when(userRepository.findById(recipientId)).thenReturn(Optional.of(recipientEntity));
        when(sharedNoteRepository.findByNoteEntity(noteEntity)).thenReturn(sharedNote);

        String expectedResult = "Note has already been shared with the recipient";
        String actualResult = userAndNotesServiceImpl.shareNote(noteId,recipientId);

        assertThat(actualResult).isEqualTo(expectedResult);

        verify(userRepository, times(1)).findById(currentUserId);
        verify(userRepository, times(1)).findById(recipientId);
        verify(sharedNoteRepository, times(1)).findByNoteEntity(noteEntity);
    }
    //================================================================================================================
    //================================================================================================================

    //================================================================================================================
    //================================================================================================================
    //Below are the private helper methods to be called by the methods of this class only
    //================================================================================================================
    //================================================================================================================
    private UserEntity createUserEntity(int userId, List<NoteEntity> selfNotesList) {
        return UserEntity
                .builder()
                .userEmail("user@example.com")
                .userId(userId)
                .username("userName")
                .password("password")
                .selfNotesList(selfNotesList)
                .build();
    }

    private List<Object> getExpectedResultForNonEmptySelfNotesNonEmptySharedNotes() {
        UserEntity userEntity1 = createUserEntity(1, new ArrayList<>());
        UserEntity userEntity2 = createUserEntity(2, new ArrayList<>());

        List<NoteEntity> selfNotesUser1 = createSelfNotesForUser(userEntity1, "userEntity1's first test note", "userEntity1's second test note");
        List<NoteEntity> selfNotesUser2 = createSelfNotesForUser(userEntity2, "userEntity2's first test note", "userEntity2's second test note", "userEntity2's third test note");

        List<SharedNote> sharedNotes = shareNotes(userEntity1, userEntity2, selfNotesUser2.get(0), selfNotesUser2.get(1));

        mockRepositories(userEntity1, sharedNotes);

        return combineDTOs(selfNotesUser1, sharedNotes);
    }

    private List<NoteEntity> createSelfNotesForUser(UserEntity userEntity, String... noteTexts) {
        List<NoteEntity> selfNotes = new ArrayList<>();
        for (int i = 0; i < noteTexts.length; i++) {
            NoteEntity noteEntity = createNoteEntity(i + 1, noteTexts[i], userEntity);
            selfNotes.add(noteEntity);
            userEntity.getSelfNotesList().add(noteEntity);
        }
        return selfNotes;
    }

    private List<SharedNote> shareNotes(UserEntity sharedWithUser, UserEntity sharingUser, NoteEntity... notes) {
        List<SharedNote> sharedNotes = new ArrayList<>();
        for (int i = 0; i < notes.length; i++) {
            SharedNote sharedNote = createSharedNote(i + 1, notes[i], sharedWithUser);
            sharedNotes.add(sharedNote);
        }
        return sharedNotes;
    }

    private void mockRepositories(UserEntity userEntity, List<SharedNote> sharedNotes) {
        when(userRepository.findById(userEntity.getUserId())).thenReturn(Optional.of(userEntity));
        when(sharedNoteRepository.findAll()).thenReturn(sharedNotes);
    }

    private List<Object> combineDTOs(List<NoteEntity> selfNotes, List<SharedNote> sharedNotes) {
        List<Object> result = new ArrayList<>();
        int sharingTransactionId = 1;
        for (SharedNote sharedNote : sharedNotes) {
            result.add(createSharedNoteDTO(sharingTransactionId, sharedNote.getSharedWithUser(), sharedNote));
            sharingTransactionId++;
        }
        for (NoteEntity noteEntity : selfNotes) {
            result.add(createNoteEntityDTO(noteEntity));
        }
        return result;
    }

    private Object createNoteEntityDTO(NoteEntity noteEntity) {
        return NoteEntityDTO
                .builder()
                .userId(noteEntity.getUserEntity().getUserId())
                .noteId(noteEntity.getNoteId())
                .note(noteEntity.getNote())
                .build();
    }

    private Object createSharedNoteDTO(int sharingTransactionID, UserEntity sharedWithUser, SharedNote sharedNote) {
        return SharedNoteDTO
                .builder()
                .sharedWithUserId(sharedWithUser.getUserId())
                .sharingTransactionId(sharingTransactionID)
                .noteId(sharedNote.getNoteEntity().getNoteId())
                .note(sharedNote.getNoteEntity().getNote())
                .build();
    }


    private SharedNote createSharedNote(int sharedTransactionId, NoteEntity noteEntity, UserEntity userEntity) {
        return SharedNote.builder()
                .sharingTransactionId(sharedTransactionId)
                .noteEntity(noteEntity)
                .sharedWithUser(userEntity)
                .build();
    }

    private NoteEntity createNoteEntity(int noteId, String note, UserEntity userEntity) {
        return NoteEntity.builder()
                .userEntity(userEntity)
                .note(note)
                .noteId(noteId)
                .build();
    }

    private void setUpUserAndNotes() {
        //Now first of all we will create a two userEntities
        UserEntity userEntity = createUserEntity(1, new ArrayList<>());

        //Now we will create a noteEntity with id = 1, which will be owned by userEntity
        NoteEntity noteEntity = createNoteEntity(1, "Test Note", userEntity);
        userEntity.getSelfNotesList().add(noteEntity);

        //Now we will also create a noteEntityES with id = 1, which will be owned by userEntity with id 1
        NoteEntityES noteEntityES = NoteEntityES
                .builder()
                .ownerId(1)
                .id(1)
                .note(noteEntity.getNote())
                .build();

        //Now we will setup when and then conditions
        when(userRepository.findById(1)).thenReturn(Optional.of(userEntity));
        lenient().when(noteRepositoryES.findById(1)).thenReturn(noteEntityES);
    }


    private NoteEntityES createNoteEntityES(int noteId, int currentUserId, String note) {
        return NoteEntityES
                .builder()
                .ownerId(currentUserId)
                .id(noteId)
                .note(note)
                .sharedWithUsers(new HashSet<>())
                .build();
    }
}