package com.example.speer.EntryDtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserEntryDto {
    private String username;
    private String password;
    private String userEmail;
}

