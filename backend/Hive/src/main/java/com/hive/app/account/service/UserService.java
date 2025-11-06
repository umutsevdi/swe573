package com.hive.app.account.service;

import com.hive.app.account.mapper.UserMapper;
import com.hive.app.account.model.UserDTO;
import com.hive.app.account.model.UserEntity;
import com.hive.app.account.model.UserRole;
import com.hive.app.account.model.request.RegisterRequest;
import com.hive.app.account.model.request.UpdateUserRequest;
import com.hive.app.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.PropertyValueException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository repository;
    private final UserMapper mapper = UserMapper.INSTANCE;

    public Optional<UserDTO> findById(Long id) {
        return repository.findByIdAndIsActiveTrue(id).map(mapper::entityToDto);
    }

    public Optional<UserDTO> findByUsername(String username) {
        return repository.findByUsernameAndIsActiveTrue(username)
                .map(mapper::entityToDto);
    }

    public void create(RegisterRequest request) throws PropertyValueException {
        var dto = mapper.requestToDto(request);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUserScore(100);
        dto.setActive(true);
        dto.setRole(UserRole.MEMBER);
        dto.setAccountTerminated(false);
        repository.save(mapper.dtoToEntity(dto));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByUsernameAndIsActiveTrue(username)
                .map(mapper::entityToDto)
                .orElseThrow(() -> new UsernameNotFoundException("User not " +
                        "found"));
    }

    public UserDTO updateUser(Long id, UpdateUserRequest request) throws UsernameNotFoundException {
        UserDTO dto = repository.findByIdAndIsActiveTrue(id)
                .map(mapper::entityToDto)
                .orElseThrow(() -> new UsernameNotFoundException("User" +
                        " not " +
                        "found"));
        boolean changed = false;
        if (!request.getBio().isBlank() && !request.getBio().equals(dto.getBiography())) {
            changed = true;
            dto.setBiography(request.getBio());
        }
        if (!request.getInterests().isEmpty() && !request.getInterests().equals(dto.getInterests())) {
            changed = true;
            dto.setInterests(request.getInterests());
        }
        if (changed) {
            repository.save(mapper.dtoToEntity(dto));
        }
        return dto;
    }

    public void deactivateUser(Long id) {
        UserEntity entity = repository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found"));
        entity.setActive(false);
        repository.save(entity);
    }
}
