package com.hive.app.account.repository;

import com.hive.app.account.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity save(UserEntity userEntity);

    Optional<UserEntity> findByIdAndIsActiveTrue(Long id);

    Optional<UserEntity> findByUsernameAndIsActiveTrue(String username);
}
