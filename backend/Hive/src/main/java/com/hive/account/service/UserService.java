package com.hive.account.service;

import com.hive.account.mapper.UserMapper;
import com.hive.account.model.RegisterRequest;
import com.hive.account.model.UserDTO;
import com.hive.account.model.UserRole;
import com.hive.account.repository.UserRepository;
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
        return repository.findById(id).map(mapper::entityToDto);
    }

    public Optional<UserDTO> findByUsername(String username) {
        return repository.findByUsername(username).map(mapper::entityToDto);
    }

    public void create(RegisterRequest request) throws PropertyValueException {
        var dto = mapper.requestToDto(request);
        dto.setCreateDate(LocalDateTime.now());
        dto.setUserScore(100);
        dto.setActive(true);
        dto.setRole(UserRole.MEMBER);
        dto.setAccountTerminated(false);
        repository.save(mapper.dtoToEntity(dto));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByUsername(username)
                .map(mapper::entityToDto)
                .orElseThrow(() -> new UsernameNotFoundException("User not " +
                        "found"));
    }
}
