package com.hive.app.events.service;

import com.hive.app.account.model.UserDTO;
import com.hive.app.events.mapper.EventMapper;
import com.hive.app.events.model.EventDTO;
import com.hive.app.events.model.EventEntity;
import com.hive.app.events.model.EventRequest;
import com.hive.app.events.model.EventType;
import com.hive.app.events.repository.EventRepository;
import com.hive.config.exceptions.NotFoundException;
import com.hive.config.exceptions.PermissionDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository repository;
    private final EventMapper mapper;

    public EventDTO newEvent(EventRequest request, UserDTO userDTO) {
        EventDTO dto = mapper.requestToDto(request);
        dto.setOwner(userDTO);
        return mapper.entityToDTO(repository.save(mapper.dtoToEntity(dto)));
    }

    public EventDTO updateEvent(Long id, EventRequest request, UserDTO userDTO) throws NotFoundException {
        EventEntity event = repository.findById(id).orElseThrow(NotFoundException::new);
        if (!event.getOwner().getId().equals(userDTO.getId())) {
            throw new PermissionDeniedException();
        }
        EventDTO requestDto = mapper.requestToDto(request);
        return requestDto;
    }

    public Optional<EventDTO> findById(Long id) {
        return repository.findById(id).map(mapper::entityToDTO);
    }

    public List<EventDTO> findByOwnerAndType(UserDTO user, EventType type) {
        return repository.findByOwner_IdAndType(user.getId(), type).stream().map(mapper::entityToDTO).toList();
    }
}
