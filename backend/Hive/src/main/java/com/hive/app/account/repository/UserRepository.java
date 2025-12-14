package com.hive.app.account.repository;

import com.hive.app.account.model.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {

    UserEntity save(UserEntity userEntity);

    Optional<UserEntity> findByIdAndActiveTrue(Long id);

    Optional<UserEntity> findByUsernameAndActiveTrue(String username);

    @Query("""
            SELECT e.id as id,
                   e.fullName as fullName,
                   e.biography as biography,
                   e.userScore as userScore,
                   e.interests as interestsString,
                   e.suspended as suspended,
                   e.role as role
            FROM UserEntity e
            WHERE e.active = true
            AND   e.id = :id
            """
    )
    Optional<UserCardView> findCardById(Long id);

    @Query("""
            SELECT e.id as id,
                   e.fullName as fullName,
                   e.biography as biography,
                   e.userScore as userScore,
                   e.interests as interestsString,
                   e.suspended as suspended,
                   e.role as role
            FROM UserEntity e
            WHERE e.active = true
            """
    )
    List<UserCardView> findAllCard();
}
