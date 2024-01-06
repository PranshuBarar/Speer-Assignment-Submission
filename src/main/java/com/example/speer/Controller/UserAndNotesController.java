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
        try{
            return new ResponseEntity<>(userAndNotesServiceImpl.getAllNotes(),HttpStatus.OK);
        } catch(Exception e){
            return  new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/notes/{noteId}") //http://localhost:8080/api/notes/<noteId>
    public ResponseEntity<?> getByNoteId(@PathVariable("noteId") int noteId) throws Exception {
        try{
            return new ResponseEntity<>(userAndNotesServiceImpl.getNoteById(noteId),HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/notes") //http://localhost:8080/api/notes
    public ResponseEntity<?> createNote(@RequestBody String note) throws Exception {
        try{
            return new ResponseEntity<>(userAndNotesServiceImpl.createNote(note),HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/notes/{noteId}") //http://localhost:8080/api/notes/<noteId>
    public ResponseEntity<?> updateNote(@RequestBody String note, @PathVariable("noteId") int noteId) throws Exception {
        try {
            return new ResponseEntity<>(userAndNotesServiceImpl.updateNote(note, noteId),HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/notes/{noteId}") //http://localhost:8080/api/notes/<noteId>
    public ResponseEntity<?> deleteNote(@PathVariable("noteId") int noteId) throws Exception {
        try{
            return new ResponseEntity<>(userAndNotesServiceImpl.deleteNote(noteId),HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/notes/{noteId}/share") //http://localhost:8080/api/notes/<noteId>/share
    public ResponseEntity<?> shareNote(@PathVariable("noteId") int noteId, @RequestBody int recipientId) throws Exception {
        try{
            return new ResponseEntity<>(userAndNotesServiceImpl.shareNote(noteId,recipientId),HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/search") //http://localhost:8080/api/search?q=<query>
    public ResponseEntity<?> searchQuery(@RequestParam("q") String query) throws IOException {
        try{
            CustomQuery customQuery = elasticSearchService.searchQuery(query.trim().toLowerCase());
            return new ResponseEntity<>(customQuery, HttpStatus.OK);
        } catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }
}

