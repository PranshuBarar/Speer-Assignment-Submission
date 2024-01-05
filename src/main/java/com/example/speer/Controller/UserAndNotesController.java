package com.example.speer.Controller;

import com.example.speer.Service.ServiceImpl.CustomElasticSearchServiceImpl;
import com.example.speer.Service.ServiceImpl.UserAndNotesServiceImpl;
import com.example.speer.utils.CustomQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class UserAndNotesController {

    @Autowired
    UserAndNotesServiceImpl userAndNotesServiceImpl;

    @Autowired
    CustomElasticSearchServiceImpl elasticSearchService;
    @GetMapping("/notes") //http://localhost:8080/api/notes
    public ResponseEntity<?> getAllNotes() {
        return new ResponseEntity<>(userAndNotesServiceImpl.getAllNotes(),HttpStatus.OK);
    }

    @GetMapping("/notes/{noteId}") //http://localhost:8080/api/notes/<noteId>
    public ResponseEntity<?> getByNoteId(@PathVariable("noteId") int noteId) throws Exception {
        return new ResponseEntity<>(userAndNotesServiceImpl.getNoteById(noteId),HttpStatus.OK);
    }

    @PostMapping("/notes") //http://localhost:8080/api/notes
    public ResponseEntity<?> createNote(@RequestBody String note) throws Exception {
        return new ResponseEntity<>(userAndNotesServiceImpl.createNote(note),HttpStatus.OK);
    }

    @PutMapping("/notes/{noteId}") //http://localhost:8080/api/notes/<noteId>
    public ResponseEntity<?> updateNote(@RequestBody String note, @PathVariable("noteId") int noteId) throws Exception {
        return new ResponseEntity<>(userAndNotesServiceImpl.updateNote(note, noteId),HttpStatus.OK);
    }

    @DeleteMapping("/notes/{noteId}") //http://localhost:8080/api/notes/<noteId>
    public ResponseEntity<?> deleteNote(@PathVariable("noteId") int noteId) throws Exception {
        return new ResponseEntity<>(userAndNotesServiceImpl.deleteNote(noteId),HttpStatus.OK);
    }

    @PostMapping("/notes/{noteId}/share") //http://localhost:8080/api/notes/<noteId>/share
    public ResponseEntity<?> shareNote(@PathVariable("noteId") int noteId, @RequestBody int recipientId) throws Exception {
        return new ResponseEntity<>(userAndNotesServiceImpl.shareNote(noteId,recipientId),HttpStatus.OK);
    }

    @GetMapping("/search") //http://localhost:8080/api/search?q=<query>
    public ResponseEntity<CustomQuery> searchQuery(@RequestParam("q") String query) throws IOException {
        CustomQuery customQuery = elasticSearchService.searchQuery(query.trim().toLowerCase());
        return new ResponseEntity<>(customQuery, HttpStatus.OK);
    }
}

