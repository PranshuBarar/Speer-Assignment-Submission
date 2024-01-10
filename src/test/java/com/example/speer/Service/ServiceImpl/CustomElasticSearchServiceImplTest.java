package com.example.speer.Service.ServiceImpl;

import com.example.speer.Entities.NoteEntity;
import com.example.speer.Entities.UserEntity;
import com.example.speer.Repository.ESRepository.NoteRepositoryES;
import com.example.speer.Repository.NoteRepository;
import com.example.speer.Repository.SharedNoteRepository;
import com.example.speer.Repository.UserRepository;
import com.example.speer.config.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CustomElasticSearchServiceImplTest {


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

    //================================================================================================
    //================================================================================================





}