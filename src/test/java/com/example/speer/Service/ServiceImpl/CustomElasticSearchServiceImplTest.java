package com.example.speer.Service.ServiceImpl;

import com.example.speer.Entities.ElasticsearchDocuments.NoteEntityES;
import com.example.speer.Entities.NoteEntity;
import com.example.speer.Entities.UserEntity;
import com.example.speer.Repository.ESRepository.NoteRepositoryES;
import com.example.speer.Repository.NoteRepository;
import com.example.speer.Repository.SharedNoteRepository;
import com.example.speer.Repository.UserRepository;
import com.example.speer.config.CustomUserDetails;
import com.example.speer.utils.CustomQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CustomElasticSearchServiceImplTest {

    /**
     * This test requires a real Elasticsearch database to be set up prior to testing,
     * as these tests interact with the Elasticsearch database using HTTP URIs.
     */

    @Mock
    UserRepository userRepository;

    @Mock
    NoteRepository noteRepository;

    @Autowired
    NoteRepositoryES noteRepositoryES;

    @Mock
    SharedNoteRepository sharedNoteRepository;

    private UserAndNotesServiceImpl userAndNotesServiceImpl;

    @Value("${api.elasticsearch.uri}")
    private String elasticSearchUri;

    @Value("${api.elasticsearch.search}")
    private String elasticSearchSearchPrefix;

    private CustomElasticSearchServiceImpl customElasticSearchServiceImpl;


    @BeforeEach
    void setUp() {
        this.userAndNotesServiceImpl = new UserAndNotesServiceImpl(userRepository,noteRepository,noteRepositoryES,sharedNoteRepository);
        this.customElasticSearchServiceImpl = new CustomElasticSearchServiceImpl(elasticSearchUri,elasticSearchSearchPrefix,userAndNotesServiceImpl);

        int currentUserId = 1;
        UserEntity userEntity = createUserEntity(currentUserId,new ArrayList<>());
        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);
        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails,null);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);

        SecurityContextHolder.setContext(securityContext);
    }



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

    private List<NoteEntity> createSelfNotesForUser(UserEntity userEntity, String... noteTexts) {
        List<NoteEntity> selfNotes = new ArrayList<>();
        for (int i = 0; i < noteTexts.length; i++) {
            NoteEntity noteEntity = createNoteEntity(i + 1, noteTexts[i], userEntity);
            selfNotes.add(noteEntity);
            userEntity.getSelfNotesList().add(noteEntity);
        }
        return selfNotes;
    }

    private NoteEntity createNoteEntity(int noteId, String note, UserEntity userEntity) {
        return NoteEntity.builder()
                .userEntity(userEntity)
                .note(note)
                .noteId(noteId)
                .build();
    }
    //================================================================================================
    //================================================================================================

    @Test
    void searchQuery() throws IOException {
        //We will create two dummy users
        UserEntity userEntity1 = createUserEntity(1,new ArrayList<>());
        UserEntity userEntity2 = createUserEntity(2,new ArrayList<>());

        //Here we are creating note entities for each user
        List<NoteEntity> noteEntityListForUser1 = createSelfNotesForUser(userEntity1,"I am a computer scientist", "I am mathematician", "Everybody must be a scientist");
        List<NoteEntity> noteEntityListForUser2 = createSelfNotesForUser(userEntity2,"I love Machine Learning", "Do Artificial Intelligence involves advanced mathematics?");


        //Now we will save all these noteEntities in the elasticsearch database
        for(NoteEntity noteEntity : noteEntityListForUser1) {
            NoteEntityES noteEntityES = convertNoteEntityToNoteEntityES(noteEntity);
            NoteEntityES noteEntityES1 = noteRepositoryES.save(noteEntityES);
        }

        for(NoteEntity noteEntity : noteEntityListForUser2) {
            NoteEntityES noteEntityES = convertNoteEntityToNoteEntityES(noteEntity);
            NoteEntityES noteEntityES1 = noteRepositoryES.save(noteEntityES);
        }


        String expectedResult = "[Everybody must be a scientist]";

        CustomQuery actualSearchResult = customElasticSearchServiceImpl.searchQuery("scien");

        String actualResult = actualSearchResult.getElements();

        assertThat(actualResult).isEqualTo(expectedResult);

    }

    private NoteEntityES convertNoteEntityToNoteEntityES(NoteEntity noteEntity) {
        return NoteEntityES
                .builder()
                .ownerId(noteEntity.getUserEntity().getUserId())
                .id(noteEntity.getNoteId())
                .note(noteEntity.getNote())
                .build();
    }


}