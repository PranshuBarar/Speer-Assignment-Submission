package com.example.speer.Service.ServiceImpl;

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
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.StatusResultMatchersExtensionsKt.isEqualTo;

@SpringBootTest
@Transactional
//@WithMockUser(username = "user@example.com")
//@WithUserDetails("user@example.com")
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
        expectedResult.add(noteEntity);

        List<Object> actualResult = userAndNotesServiceImpl.getAllNotes();

        assertThat(actualResult).isEqualTo(expectedResult);
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

    @Test
    void createNote() {

    }

    @Test
    void getNoteById() {

    }

    @Test
    void updateNote() {

    }

    @Test
    void deleteNote() {

    }

    @Test
    void shareNote() {

    }

    @Test
    void getNoteEntity() {

    }

    @Test
    void getCurrentUserId() {

    }

    @Test
    void getCurrentUserDetails() {

    }

    //================================================================================================================
    /*Private method to be called by the function of this class only*/
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
            result.add(createSharedNoteDTO(sharingTransactionId,sharedNote.getSharedWithUser(), sharedNote));
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
}