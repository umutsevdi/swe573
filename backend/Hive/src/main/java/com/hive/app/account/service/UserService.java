package com.hive.app.account.service;

import com.hive.app.account.mapper.UserMapper;
import com.hive.app.account.model.UserDTO;
import com.hive.app.account.model.UserRole;
import com.hive.app.account.model.request.RegisterRequest;
import com.hive.app.account.model.request.UpdateUserRequest;
import com.hive.app.account.model.response.UserCardResponse;
import com.hive.app.account.repository.UserRepository;
import com.hive.config.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.PropertyValueException;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository repository;
    private final UserMapper mapper;

    @Cacheable(cacheNames = "userByIdCard", key = "#id")
    public Optional<UserCardResponse> findByIdCard(Long id) {
        return repository.findCardById(id);
    }

    @Cacheable(cacheNames = "userById", key = "#id")
    public Optional<UserDTO> findById(Long id) {
        return repository.findByIdAndIsActiveTrue(id).map(mapper::entityToDto);
    }

    @Cacheable(cacheNames = "userByUsername", key = "#username")
    public Optional<UserDTO> findByUsername(String username) {
        return repository.findByUsernameAndIsActiveTrue(username)
                .map(mapper::entityToDto);
    }

    @Caching(evict = {
            @CacheEvict(value = "userList"),
            @CacheEvict(value = "userByIdCard", allEntries = true),
            @CacheEvict(value = "userByUsername", allEntries = true),
            @CacheEvict(value = "userById", allEntries = true)}
    )
    @Transactional
    public void create(RegisterRequest request) throws PropertyValueException {
        var dto = mapper.requestToDto(request);
        dto.setInterests(request.getInterests().stream().map(e-> e.toLowerCase().replace(' ', '-')).toList());
        dto.setCreatedAt(LocalDateTime.now());
        dto.setTimeBank(10);
        dto.setUserScore(100);
        dto.setActive(true);
        dto.setRole(UserRole.MEMBER);
        dto.setAccountTerminated(false);
        repository.save(mapper.dtoToEntity(dto));
    }

    @Cacheable(cacheNames = "userByUsername", key = "#username")
    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return repository.findByUsernameAndIsActiveTrue(username)
                .map(mapper::entityToDto)
                .orElseThrow(() -> new UsernameNotFoundException("User not " +
                        "found"));
    }

    @Caching(evict = {
            @CacheEvict(value = "userByUsername", key = "#dto.username"),
            @CacheEvict(value = "userByIdCard", key = "#dto.id"),
            @CacheEvict(value = "userById", key = "#dto.id")}
    )
    @Transactional
    public UserDTO updateUser(UserDTO dto, UpdateUserRequest request) throws UsernameNotFoundException {
        boolean changed = false;
        if (request.getBiography() != null && !request.getBiography().isBlank() && !request.getBiography().equals(dto.getBiography())) {
            changed = true;
            dto.setBiography(request.getBiography());
        }
        if (request.getInterests() != null && !request.getInterests().isEmpty() && !request.getInterests().equals(dto.getInterests())) {
            changed = true;
            dto.setInterests(request.getInterests().stream().map(e -> e.toLowerCase().replace(' ', '-')).toList());
        }
        if (changed) {
            repository.save(mapper.dtoToEntity(dto));
        }
        return dto;
    }

    @Caching(evict = {
            @CacheEvict(value = "userByUsername", key = "#dto.username"),
            @CacheEvict(value = "userByIdCard", key = "#dto.id"),
            @CacheEvict(value = "userById", key = "#dto.id")}
    )
    @Transactional
    public void deactivateUser(UserDTO dto) {
        dto.setActive(false);
        repository.save(mapper.dtoToEntity(dto));
    }

    @Caching(evict = {
            @CacheEvict(value = "userByUsername", key = "#dto.username"),
            @CacheEvict(value = "userById", key = "#dto.id")}
    )
    @Transactional
    public void freezeTime(UserDTO dto, int time) {
        dto.setTimeBank(dto.getTimeBank() - time);
        dto.setFrozenAmount(dto.getFrozenAmount() + time);
        repository.save(mapper.dtoToEntity(dto));
    }

    @Caching(evict = {
            @CacheEvict(value = "userByUsername", key = "#dto.username"),
            @CacheEvict(value = "userById", key = "#dto.id")}
    )
    @Transactional
    public void unfreezeTime(UserDTO dto, int time) {
        dto.setTimeBank(dto.getTimeBank() + time);
        dto.setFrozenAmount(dto.getFrozenAmount() - time);
        repository.save(mapper.dtoToEntity(dto));
    }

    @Caching(evict = {
            @CacheEvict(value = "userByUsername", key = "#dto.username"),
            @CacheEvict(value = "userById", key = "#dto.id")}
    )
    @Transactional
    public void transferTime(UserDTO dto, int time) {
        dto.setFrozenAmount(dto.getFrozenAmount() - time);
        repository.save(mapper.dtoToEntity(dto));
    }

    // Administration operations
    @Cacheable(cacheNames = "userList")
    public List<UserCardResponse> getUsers() {
        return repository.findAllCard();
    }

    @Transactional
    public void banUser(Long id, boolean decision) throws UserNotFoundException {
        UserEntity e = repository.findById(id).orElseThrow(UserNotFoundException::new);
        e.setAccountTerminated(decision);
        repository.save(e);
    }
}
