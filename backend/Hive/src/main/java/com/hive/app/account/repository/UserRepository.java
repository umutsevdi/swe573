package com.hive.app.account.repository;

import com.hive.app.account.model.UserEntity;
import com.hive.app.account.model.response.UserCardResponse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {

    UserEntity save(UserEntity userEntity);

    Optional<UserEntity> findByIdAndIsActiveTrue(Long id);

    Optional<UserEntity> findByUsernameAndIsActiveTrue(String username);

    @Query("""
            SELECT e.id as id,
                   e.fullName as fullName,
                   e.biography as biography,
                   e.userScore as userScore,
                   e.interests as interestsString
            FROM UserEntity e
            WHERE e.isActive = true
            AND   e.id = :id
            """
    )
    Optional<UserCardResponse> findCardById(Long id);

    @Query("""
            SELECT e.id as id,
                   e.fullName as fullName,
                   e.biography as biography,
                   e.userScore as userScore,
                   e.interests as interestsString
            FROM UserEntity e
            WHERE e.isActive = true
            """
    )
    List<UserCardResponse> findAllCard();
}
