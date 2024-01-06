package com.example.speer.config;

import com.example.speer.Entities.UserEntity;
import com.example.speer.EntryDtos.UserEntryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Book;

@Getter
@Setter
@RestController
@RequestMapping("/api")
@CrossOrigin
public class JwtAuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsServiceImpl jwtUserDetailsServiceImpl;

    @Operation(summary = "Authenticate to get access JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success Authentication",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Book.class)) }),
            @ApiResponse(responseCode = "400", description = "Failed Authentication",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Failed Authentication",
                    content = @Content) })
    @PostMapping("/auth/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest jwtRequest) throws Exception {
        String userEmail = jwtRequest.getUserEmail();
        String password = jwtRequest.getPassword();
        authenticate(userEmail, password);
        //If no exception thrown after running authenticate(), it means request is authenticated

        final CustomUserDetails customUserDetails = (CustomUserDetails) jwtUserDetailsServiceImpl.loadUserByUsername(userEmail);
        final String token = jwtTokenUtil.generateToken(customUserDetails);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<?> saveUser(@RequestBody UserEntryDto userEntryDto) throws Exception {
        try{
            UserEntity userEntity = jwtUserDetailsServiceImpl.save(userEntryDto);
            return new ResponseEntity<>(userEntity, HttpStatus.ACCEPTED);
        } catch (Exception e){
            return new ResponseEntity<>("User already Exists", HttpStatus.BAD_REQUEST);
        }

    }

    private void authenticate(String userEmail, String password) throws Exception {
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userEmail, password));
        } catch (DisabledException e){
            throw new Exception("USER_DISABLED", e);
        } catch(BadCredentialsException e){
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
