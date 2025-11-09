package com.hive.app.events.service;

import com.hive.app.account.model.UserDTO;
import com.hive.app.events.mapper.EventMapper;
import com.hive.app.events.model.EventDTO;
import com.hive.app.events.model.EventRequest;
import com.hive.app.events.model.EventResponse;
import com.hive.app.events.repository.EventRepository;
import com.hive.config.exceptions.NotFoundException;
import com.hive.config.exceptions.PermissionDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository repository;
    private final EventMapper mapper;

    public EventResponse newEvent(EventRequest request, UserDTO userDTO) {
        EventDTO dto = mapper.requestToDto(request);
        dto.setOwner(userDTO);
        return mapper.entityToResponse(repository.save(mapper.dtoToEntity(dto)));
    }

    public EventResponse updateEvent(Long id, EventRequest request, UserDTO userDTO) throws NotFoundException {
        EventDTO event = repository.findById(id)
                .map(mapper::entityToDTO)
                .orElseThrow(NotFoundException::new);
        if (!event.getOwner().getId().equals(userDTO.getId())) {
            throw new PermissionDeniedException();
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getTags() != null) {
            event.setTags(request.getTags());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getMaxParticipants() != null) {
            event.setMaxParticipants(request.getMaxParticipants());
        }
        if (request.getType() != null) {
            event.setType(request.getType());
        }
        if (request.getDuration() != null) {
            event.setDuration(request.getDuration());
        }
        return mapper.entityToResponse(repository.save(mapper.dtoToEntity(event)));
    }

    public EventResponse getEvent(Long id) throws NotFoundException {
        return repository.findById(id)
                .map(mapper::entityToResponse)
                .orElseThrow(NotFoundException::new);
    }

    public EventDTO findById(Long id) throws NotFoundException {
        return repository.findById(id)
                .map(mapper::entityToDTO)
                .orElseThrow(NotFoundException::new);
    }
}
