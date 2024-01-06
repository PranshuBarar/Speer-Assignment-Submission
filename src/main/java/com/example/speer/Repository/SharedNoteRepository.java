package com.example.speer.Repository;

import com.example.speer.Entities.NoteEntity;
import com.example.speer.Entities.SharedNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharedNoteRepository extends JpaRepository<SharedNote,Integer> {

    @Query(value = "SELECT sn FROM SharedNotes sn WHERE sn.note.user.id = :userId", nativeQuery = true)
    List<SharedNote> findAllByUserId(int userId);

    SharedNote findByNoteEntity(NoteEntity noteEntity);
}
