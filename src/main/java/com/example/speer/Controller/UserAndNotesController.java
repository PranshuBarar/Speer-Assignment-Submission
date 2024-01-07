package com.example.speer.Controller;

import com.example.speer.Service.ServiceImpl.CustomElasticSearchServiceImpl;
import com.example.speer.Service.ServiceImpl.UserAndNotesServiceImpl;
import com.example.speer.utils.CustomQuery;
import io.github.bucket4j.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Notes", description = "The Notes APIs contain all the operations that can be performed on a user's note.")
public class UserAndNotesController {

    private static final String X_RATE_TOKEN_AVAILABLE = "X_RATE_TOKEN_AVAILABLE";
    private static final String X_RATE_REFILL_TIME = "X_RATE_REFILL_TIME";

    private static final String TOO_MANY_REQUESTS = "TOO_MANY_REQUESTS";
    @Autowired
    UserAndNotesServiceImpl userAndNotesServiceImpl;

    @Autowired
    CustomElasticSearchServiceImpl elasticSearchService;

    private Bucket bucket;


    @Operation(summary = "Get your all the notes (Owned by you as well as shared by you)")
    @GetMapping("/notes") //http://localhost:8080/api/notes
    public ResponseEntity<?> getAllNotes() {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            try {
                List<Object> responseList = userAndNotesServiceImpl.getAllNotes();
                return ResponseEntity.ok().header(X_RATE_TOKEN_AVAILABLE, Long.toString(probe.getRemainingTokens()))
                        .body(responseList);
            } catch (Exception e) {
                return ResponseEntity.ok().header(X_RATE_TOKEN_AVAILABLE, Long.toString(probe.getRemainingTokens()))
                        .body(e.getMessage());
            }
        } else {
            return ResponseEntity.ok().header(X_RATE_REFILL_TIME, Long.toString(probe.getNanosToWaitForRefill() / 1_000_000_000))
                    .body(TOO_MANY_REQUESTS);
        }
    }

    @Operation(summary = "Get your note by noteId (Either you should be owner of this note or " +
            " this note must be shared to you by any other user)")
    @GetMapping("/notes/{noteId}") //http://localhost:8080/api/notes/<noteId>
    public ResponseEntity<?> getByNoteId(@PathVariable("noteId") int noteId) throws Exception {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            try {
                Object responseObject = userAndNotesServiceImpl.getNoteById(noteId);
                return ResponseEntity.ok().header(X_RATE_TOKEN_AVAILABLE, Long.toString(probe.getRemainingTokens()))
                        .body(responseObject);
            } catch (Exception e) {
                return ResponseEntity.ok().header(X_RATE_TOKEN_AVAILABLE, Long.toString(probe.getRemainingTokens()))
                        .body(e.getMessage());
            }
        } else {
            return ResponseEntity.ok().header(X_RATE_REFILL_TIME, Long.toString(probe.getNanosToWaitForRefill() / 1_000_000_000))
                    .body(TOO_MANY_REQUESTS);
        }
    }

    @Operation(summary = "Add a new note")
    @PostMapping("/notes") //http://localhost:8080/api/notes
    public ResponseEntity<?> createNote(@RequestBody String note) throws Exception {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            try {
                return new ResponseEntity<>(userAndNotesServiceImpl.createNote(note), HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            return ResponseEntity.ok().header(X_RATE_REFILL_TIME, Long.toString(probe.getNanosToWaitForRefill() / 1_000_000_000))
                    .body(TOO_MANY_REQUESTS);
        }
    }

    @Operation(summary = "Update Note by noteId")
    @PutMapping("/notes/{noteId}") //http://localhost:8080/api/notes/<noteId>
    public ResponseEntity<?> updateNote(@RequestBody String note, @PathVariable("noteId") int noteId) throws Exception {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            try {
                return new ResponseEntity<>(userAndNotesServiceImpl.updateNote(note, noteId), HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            return ResponseEntity.ok().header(X_RATE_REFILL_TIME, Long.toString(probe.getNanosToWaitForRefill() / 1_000_000_000))
                    .body(TOO_MANY_REQUESTS);
        }
    }

    @Operation(summary = "Update Note by noteId (You must own this note, in order to delete it)")
    @DeleteMapping("/notes/{noteId}") //http://localhost:8080/api/notes/<noteId>
    public ResponseEntity<?> deleteNote(@PathVariable("noteId") int noteId) throws Exception {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            try {
                return new ResponseEntity<>(userAndNotesServiceImpl.deleteNote(noteId), HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            return ResponseEntity.ok().header(X_RATE_REFILL_TIME, Long.toString(probe.getNanosToWaitForRefill() / 1_000_000_000))
                    .body(TOO_MANY_REQUESTS);
        }
    }

    @Operation(summary = "Share your note with another user with his userId (recipientId)")
    @PostMapping("/notes/{noteId}/share") //http://localhost:8080/api/notes/<noteId>/share
    public ResponseEntity<?> shareNote(@PathVariable("noteId") int noteId, @RequestBody int recipientId) throws Exception {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            try {
                return new ResponseEntity<>(userAndNotesServiceImpl.shareNote(noteId, recipientId), HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            return ResponseEntity.ok().header(X_RATE_REFILL_TIME, Long.toString(probe.getNanosToWaitForRefill() / 1_000_000_000))
                    .body(TOO_MANY_REQUESTS);
        }
    }

    @Operation(summary = "Search your notes (or notes shared with you by another user) using Keywords")
    @GetMapping("/search") //http://localhost:8080/api/search?q=<query>
    public ResponseEntity<?> searchQuery(@RequestParam("q") String query) throws IOException {
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            try {
                CustomQuery customQuery = elasticSearchService.searchQuery(query.trim().toLowerCase());
                return new ResponseEntity<>(customQuery, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            return ResponseEntity.ok().header(X_RATE_REFILL_TIME, Long.toString(probe.getNanosToWaitForRefill() / 1_000_000_000))
                    .body(TOO_MANY_REQUESTS);
        }
    }

    @PostConstruct
    public void setupBucket() {
        Bandwidth limit = Bandwidth.classic(3, Refill.intervally(3, Duration.ofSeconds(50)));
        this.bucket = Bucket4j.builder().addLimit(limit).build();
    }

}

