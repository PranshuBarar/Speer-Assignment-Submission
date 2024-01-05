package com.example.speer.Controller;

import com.example.speer.Entities.NoteEntity;
import com.example.speer.Service.ServiceImpl.CustomElasticSearchService;
import com.example.speer.Service.ServiceImpl.UserAndNotesService;
import com.example.speer.utils.CustomQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UserAndNotesController {

    @Autowired
    UserAndNotesService userAndNotesService;

    @Autowired
    CustomElasticSearchService elasticSearchService;
    @GetMapping("/notes") //http://localhost:8080/api/notes
    public ResponseEntity<?> getAllNotes() {
        return new ResponseEntity<>(userAndNotesService.getAllNotes(),HttpStatus.OK);
    }

    @GetMapping("/notes/{noteId}") //http://localhost:8080/api/notes/<noteId>
    public ResponseEntity<?> getByNoteId(@PathVariable("noteId") int noteId) throws Exception {
        return new ResponseEntity<>(userAndNotesService.getNoteById(noteId),HttpStatus.OK);
    }

    @PostMapping("/notes") //http://localhost:8080/api/notes/createNote
    public ResponseEntity<?> createNote(@RequestBody String note) throws Exception {
        return new ResponseEntity<>(userAndNotesService.createNote(note),HttpStatus.OK);
    }

    @PutMapping("/notes/{noteId}") //http://localhost:8080/api/notes/<noteId>
    public ResponseEntity<?> updateNote(@RequestBody String note, @PathVariable("noteId") int noteId) throws Exception {
        return new ResponseEntity<>(userAndNotesService.updateNote(note, noteId),HttpStatus.OK);
    }

    @DeleteMapping("/notes/{noteId}") //http://localhost:8080/api/notes/<noteId>
    public ResponseEntity<?> deleteNote(@PathVariable("noteId") int noteId) throws Exception {
        return new ResponseEntity<>(userAndNotesService.deleteNote(noteId),HttpStatus.OK);
    }

    @PostMapping("/notes/{noteId}/share") //http://localhost:8080/api/notes/<noteId>/share
    public ResponseEntity<?> shareNote(@PathVariable("noteId") int noteId, @RequestBody int recipientId) throws Exception {
        return new ResponseEntity<>(userAndNotesService.shareNote(noteId,recipientId),HttpStatus.OK);
    }

    @GetMapping("/search") //http://localhost:8080/api/search?q=<query>
    public ResponseEntity<CustomQuery> searchQuery(@RequestParam("q") String query) throws IOException {
        CustomQuery customQuery = elasticSearchService.searchQuery(query.trim().toLowerCase());
        return new ResponseEntity<>(customQuery, HttpStatus.OK);
    }
}

