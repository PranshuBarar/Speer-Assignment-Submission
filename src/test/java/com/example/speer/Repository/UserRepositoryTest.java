package com.example.speer.Repository;

import com.example.speer.Entities.UserEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        UserEntity userEntity = UserEntity.builder()
                .userEmail("user@example.com")
                .password("password")
                .username("user")
                .build();
        userRepository.save(userEntity);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void existsByUserEmail() {

        Boolean actualResult = userRepository.existsByUserEmail("user@example.com");

        assertThat(actualResult).isTrue();
    }


    @Test
    void findByUserEmail() {

        UserEntity userEntityFromDB = userRepository.findByUserEmail("user@example.com");
        String actualResultUserEmail = userEntityFromDB.getUserEmail();

        assertThat(actualResultUserEmail).isEqualTo("user@example.com");
    }


}