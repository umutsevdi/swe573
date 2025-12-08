package com.hive.app.account.controller;

import com.hive.app.account.mapper.UserMapper;
import com.hive.app.account.model.UserDTO;
import com.hive.app.account.model.request.RegisterRequest;
import com.hive.app.account.model.response.UserCardResponse;
import com.hive.app.account.service.UserService;
import com.hive.config.exceptions.UserNotFoundException;
import com.hive.util.JwtPayload;
import com.hive.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final JwtUtil jwtUtil;
    private final UserService service;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserMapper mapper;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest dto) {
        if (service.findByUsername(dto.getUsername()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        UserDTO user = (UserDTO) auth.getPrincipal();
        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token, mapper.dtoToCard(user)));
    }

    @GetMapping("/renew")
    public ResponseEntity<AuthResponse> login(@RequestHeader(value = "Authorization") String authHeader) throws UserNotFoundException {
        JwtPayload payload = jwtUtil.parseToken(authHeader);
        if (!payload.expired()) {
            return ResponseEntity.ok(service
                    .findById(payload.id())
                    .map(user -> new AuthResponse(jwtUtil.generateToken(user),
                            mapper.dtoToCard(user)))
                    .orElseThrow(UserNotFoundException::new));

        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    public record AuthRequest(String username, String password) {
    }

    public record AuthResponse(String token, UserCardResponse user) {
    }
}