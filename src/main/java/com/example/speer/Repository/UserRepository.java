package com.example.speer.Repository;

import com.example.speer.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    boolean existsByUserEmail(String email);

    UserEntity findByUserEmail(String email);


}
