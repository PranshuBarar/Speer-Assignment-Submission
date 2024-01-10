package com.example.speer.Repository.ESRepository;

import com.example.speer.Entities.ElasticsearchDocuments.NoteEntityES;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
@Transactional
class NoteRepositoryESTest {

    @Autowired
    NoteRepositoryES noteRepositoryES;

    @BeforeEach
    void setUp() {
        NoteEntityES noteEntityES = NoteEntityES
                .builder()
                .note("This is a sample note")
                .id(123)
                .ownerId(456)
                .build();
        noteRepositoryES.save(noteEntityES);
    }

    @AfterEach
    void tearDown() {
        noteRepositoryES.deleteAll();
    }

    @Test
    void findByNoteMySqlId() {
        NoteEntityES noteEntityES = noteRepositoryES.findById(123);
        int actualResult = noteEntityES.getId();
        int expectedResult = 123;

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void deleteByNoteMySqlId() {
        noteRepositoryES.deleteById(123);
        NoteEntityES noteEntityES = noteRepositoryES.findById(123);
        assertThat(noteEntityES).isNull();
    }
}