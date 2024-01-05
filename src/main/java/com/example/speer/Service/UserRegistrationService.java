package com.example.speer.Service;

import com.example.speer.Entities.UserEntity;
import com.example.speer.EntryDtos.UserEntryDto;

public interface UserRegistrationService {
    UserEntity signup(UserEntryDto userEntryDto) throws Exception;
}
