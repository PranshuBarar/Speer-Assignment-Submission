package com.example.speer.Convertors;

import com.example.speer.Entities.UserEntity;
import com.example.speer.EntryDtos.UserEntryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserConvertor {


    static PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public static UserEntity UserEntryDtoToUserEntityConvertor(UserEntryDto userEntryDto){
        return UserEntity
                .builder()
                .username(userEntryDto.getUsername())
                .userEmail(userEntryDto.getUserEmail())
                .password(passwordEncoder.encode(userEntryDto.getPassword()))
                .build();
    }
}
