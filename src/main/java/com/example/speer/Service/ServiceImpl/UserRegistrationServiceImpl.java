package com.example.speer.Service.ServiceImpl;

import com.example.speer.Convertors.UserConvertor;
import com.example.speer.Entities.UserEntity;
import com.example.speer.EntryDtos.UserEntryDto;
import com.example.speer.Repository.UserRepository;
import com.example.speer.Service.UserRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {

    @Autowired
    UserRepository userRepository;

    public UserEntity signup(UserEntryDto userEntryDto) throws Exception {
        if(userRepository.existsByUserEmail(userEntryDto.getUserEmail())){
            throw new Exception("User already registered");
        }
        UserEntity userEntity = UserConvertor.UserEntryDtoToUserEntityConvertor(userEntryDto);
        return userRepository.save(userEntity);
    }
}
