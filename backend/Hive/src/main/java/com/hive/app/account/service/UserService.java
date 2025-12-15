package com.hive.app.account.service;

import com.hive.app.account.mapper.UserMapper;
import com.hive.app.account.model.UserDTO;
import com.hive.app.account.model.UserEntity;
import com.hive.app.account.model.UserRole;
import com.hive.app.account.model.request.RegisterRequest;
import com.hive.app.account.model.request.UpdateUserRequest;
import com.hive.app.account.model.response.UserCardResponse;
import com.hive.app.account.model.response.UserProfileResponse;
import com.hive.app.account.repository.UserRepository;
import com.hive.app.async.notification.model.NotificationType;
import com.hive.app.async.notification.service.NotificationService;
import com.hive.app.async.transaction.model.Identifier;
import com.hive.app.async.transaction.model.TransactionRecord;
import com.hive.app.async.transaction.model.TransactionType;
import com.hive.app.async.transaction.service.TransactionService;
import com.hive.config.exceptions.LogicException;
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
    private final TransactionService transactionService;
    private final UserMapper mapper;
    private final NotificationService notificationService;

    @Cacheable(cacheNames = "userByIdCard", key = "#id")
    public Optional<UserCardResponse> findByIdCard(Long id) {
        return repository.findCardById(id).map(mapper::viewToCard);
    }

    @Cacheable(cacheNames = "userById", key = "#id")
    public Optional<UserDTO> findById(Long id) {
        return repository.findByIdAndActiveTrue(id).map(mapper::entityToDto);
    }

    @Cacheable(cacheNames = "userByUsername", key = "#username")
    public Optional<UserDTO> findByUsername(String username) {
        return repository.findByUsernameAndActiveTrue(username)
                .map(mapper::entityToDto);
    }

    @Caching(evict = {
            @CacheEvict(value = "userList", allEntries = true),
            @CacheEvict(value = "userByIdCard", allEntries = true),
            @CacheEvict(value = "userByUsername", allEntries = true),
            @CacheEvict(value = "userById", allEntries = true)}
    )
    @Transactional
    public void create(RegisterRequest request) throws PropertyValueException {
        var dto = mapper.requestToDto(request);
        dto.setInterests(request.getInterests().stream().map(e -> e.toLowerCase().replace(' ', '-')).toList());
        dto.setCreatedAt(LocalDateTime.now());
        dto.setTimeBank(10);
        dto.setUserScore(100);
        dto.setActive(true);
        dto.setRole(UserRole.MEMBER);
        dto.setSuspended(false);
        repository.save(mapper.dtoToEntity(dto));

        transactionService.insertTransaction(new TransactionRecord(
                TransactionType.USER_REGISTER,
                new Identifier(dto.getId(), Identifier.IdType.USER),
                new Identifier(dto.getId(), Identifier.IdType.USER),
                "Account was created.")
        );
    }

    @Cacheable(cacheNames = "userByUsername", key = "#username")
    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return repository.findByUsernameAndActiveTrue(username)
                .map(mapper::entityToDto)
                .orElseThrow(() -> new UsernameNotFoundException("User not " +
                        "found"));
    }

    @Caching(evict = {
            @CacheEvict(value = "userList", allEntries = true),
            @CacheEvict(value = "userByUsername", key = "#dto.username"),
            @CacheEvict(value = "userByIdCard", key = "#dto.id"),
            @CacheEvict(value = "userById", key = "#dto.id")}
    )
    @Transactional
    public UserProfileResponse updateUser(UserDTO dto, UpdateUserRequest request) throws UsernameNotFoundException {
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
            transactionService.insertTransaction(new TransactionRecord(
                    TransactionType.USER_UPDATE,
                    new Identifier(dto.getId(), Identifier.IdType.USER),
                    new Identifier(dto.getId(), Identifier.IdType.USER),
                    "User updated it's profile.")
            );
        }
        return mapper.dtoToProfile(dto);
    }

    @Caching(evict = {
            @CacheEvict(value = "userList", allEntries = true),
            @CacheEvict(value = "userByUsername", key = "#dto.username"),
            @CacheEvict(value = "userByIdCard", key = "#dto.id"),
            @CacheEvict(value = "userById", key = "#dto.id")}
    )
    @Transactional
    public void deactivateUser(UserDTO dto) {
        dto.setActive(false);
        repository.save(mapper.dtoToEntity(dto));
        transactionService.insertTransaction(new TransactionRecord(
                TransactionType.USER_DELETE,
                new Identifier(dto.getId(), Identifier.IdType.USER),
                new Identifier(dto.getId(), Identifier.IdType.USER),
                "User deactivated it's account.")
        );
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
        return repository.findAllCard().stream().map(mapper::viewToCard).toList();
    }

    @Caching(evict = {
            @CacheEvict(value = "userList", allEntries = true),
            @CacheEvict(value = "userByUsername", allEntries = true),
            @CacheEvict(value = "userByIdCard", key = "#id"),
            @CacheEvict(value = "userById", key = "#id")}
    )
    @Transactional
    public void suspendUser(Long id, Boolean decision, UserDTO self) throws UserNotFoundException, LogicException {
        UserEntity e = repository.findById(id).orElseThrow(UserNotFoundException::new);
        if (e.getRole() != UserRole.MEMBER) {
            throw new LogicException("Can not suspend administrators.");
        }
        e.setSuspended(decision);
        repository.save(e);
        transactionService.insertTransaction(new TransactionRecord(
                TransactionType.ADMIN_SUSPEND_USER,
                new Identifier(self.getId(), Identifier.IdType.USER),
                new Identifier(id, Identifier.IdType.USER),
                "Administrator set user.suspend to VALUE".replace("VALUE", decision.toString())
        ));
    }

    @Caching(evict = {
            @CacheEvict(value = "userList", allEntries = true),
            @CacheEvict(value = "userByUsername", allEntries = true),
            @CacheEvict(value = "userByIdCard", key = "#id"),
            @CacheEvict(value = "userById", key = "#id")}
    )
    @Transactional
    public void setUserRole(Long id, UserRole role, UserDTO self) throws UserNotFoundException, LogicException {
        UserEntity e = repository.findById(id).orElseThrow(UserNotFoundException::new);
        if (e.getRole() == role) {
            return;
        }
        if (role == UserRole.OWNER) {
            throw new LogicException("Can not assign Owner role.");
        }
        UserRole oldRole = e.getRole();
        e.setRole(role);
        repository.save(e);
        transactionService.insertTransaction(new TransactionRecord(
                TransactionType.ADMIN_ASSIGN_ROLE,
                new Identifier(self.getId(), Identifier.IdType.USER),
                new Identifier(id, Identifier.IdType.USER),
                "Administrator set user role 'OLD_ROLE' to 'NEW_ROLE'"
                        .replace("OLD_ROLE", oldRole.toString())
                        .replace("NEW_ROLE", role.toString())
        ));

        notificationService.insertNotification(
                List.of(e.getId()),
                NotificationType.USER_ROLE_UPDATE,
                new Identifier(e.getId(), Identifier.IdType.USER)
        );
    }
}
