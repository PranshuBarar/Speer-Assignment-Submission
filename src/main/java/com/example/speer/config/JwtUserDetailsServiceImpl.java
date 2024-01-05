package com.example.speer.config;

import com.example.speer.Entities.UserEntity;
import com.example.speer.EntryDtos.UserEntryDto;
import com.example.speer.Repository.UserRepository;
import com.example.speer.Service.ServiceImpl.UserRegistrationService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
public class JwtUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private UserRepository userRepository;


    @Override
    public CustomUserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUserEmail(userEmail);
        if(userEntity == null) {
            throw new UsernameNotFoundException("User with the following email not found: "+ userEmail);
        }
        return new CustomUserDetails(userEntity);
    }

    public UserEntity save(UserEntryDto userEntryDto) throws Exception {
        return userRegistrationService.signup(userEntryDto);
    }

}
