package com.hive.app.account.service;

import com.hive.app.account.mapper.UserMapper;
import com.hive.app.account.model.UserDTO;
import com.hive.app.account.model.UserRole;
import com.hive.app.account.model.request.RegisterRequest;
import com.hive.app.account.model.request.UpdateUserRequest;
import com.hive.app.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.PropertyValueException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository repository;
    private final UserMapper mapper;

    @Cacheable(cacheNames = "usersById", key = "#id")
    public Optional<UserDTO> findById(Long id) {
        return repository.findByIdAndIsActiveTrue(id).map(mapper::entityToDto);
    }

    @Cacheable(cacheNames = "usersByUsername", key = "#username")
    public Optional<UserDTO> findByUsername(String username) {
        return repository.findByUsernameAndIsActiveTrue(username)
                .map(mapper::entityToDto);
    }

    @CacheEvict(cacheNames = {"usersById", "usersByUsername"}, allEntries = true)
    @Transactional
    public void create(RegisterRequest request) throws PropertyValueException {
        var dto = mapper.requestToDto(request);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setTimeBank(10);
        dto.setUserScore(100);
        dto.setActive(true);
        dto.setRole(UserRole.MEMBER);
        dto.setAccountTerminated(false);
        repository.save(mapper.dtoToEntity(dto));
    }

    @Cacheable(cacheNames = "usersByUsername", key = "#username")
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByUsernameAndIsActiveTrue(username)
                .map(mapper::entityToDto)
                .orElseThrow(() -> new UsernameNotFoundException("User not " +
                        "found"));
    }

    @CacheEvict(cacheNames = {"usersById", "usersByUsername"}, allEntries = true)
    @Transactional
    public UserDTO updateUser(UserDTO dto, UpdateUserRequest request) throws UsernameNotFoundException {
        boolean changed = false;
        if (request.getBiography() != null && !request.getBiography().isBlank() && !request.getBiography().equals(dto.getBiography())) {
            changed = true;
            dto.setBiography(request.getBiography());
        }
        if (request.getInterests() != null && !request.getInterests().isEmpty() && !request.getInterests().equals(dto.getInterests())) {
            changed = true;
            dto.setInterests(request.getInterests());
        }
        if (changed) {
            repository.save(mapper.dtoToEntity(dto));
        }
        return dto;
    }

    @CacheEvict(cacheNames = {"usersById", "usersByUsername"}, allEntries = true)
    @Transactional
    public void deactivateUser(UserDTO dto) {
        dto.setActive(false);
        repository.save(mapper.dtoToEntity(dto));
    }
}
