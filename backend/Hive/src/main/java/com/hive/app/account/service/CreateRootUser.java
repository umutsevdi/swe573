package com.hive.app.account.service;

import com.hive.app.account.model.UserEntity;
import com.hive.app.account.model.UserRole;
import com.hive.app.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CreateRootUser implements CommandLineRunner {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${hive.username:root}")
    private String username;
    @Value("${hive.email:no-reply@hive.com}")
    private String email;
    @Value("${hive.password}")
    private String password;

    @Override
    public void run(String... args) {
        if (repository.findByUsernameAndActiveTrue(username.toLowerCase()).isEmpty()) {
            UserEntity entity = new UserEntity();
            entity.setUsername(username.toLowerCase());
            entity.setFullName(username);
            entity.setEmail(email);
            entity.setBiography("");
            entity.setInterests("");
            entity.setPassword(passwordEncoder.encode(password));
            entity.setActive(true);
            entity.setRole(UserRole.OWNER);
            repository.save(entity);
        }
    }
}
