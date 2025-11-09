package com.hive.app.events.service;

import com.hive.app.account.mapper.UserMapper;
import com.hive.app.account.model.UserDTO;
import com.hive.app.events.mapper.EventMapper;
import com.hive.app.events.model.*;
import com.hive.app.events.repository.EventRepository;
import com.hive.config.exceptions.NotFoundException;
import com.hive.config.exceptions.PermissionDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class EventService {
    private final EventRepository repository;
    private final EventMapper mapper;
    private final UserMapper userMapper;

    public List<EventListView> getUserEvents(Long userId, boolean showInactive) {
        if (showInactive) {
            return repository.findByOwner_Id(userId);
        }
        return repository.findByOwner_IdAndEventDateAfter(userId, LocalDate.now().minusDays(1));
    }

    public EventResponse newEvent(EventRequest request, UserDTO userDTO) {
        request.setTags(request.getTags().stream().map(String::toLowerCase).toList());
        EventEntity entity = mapper.requestToEntity(request);
        entity.setOwner(userMapper.dtoToEntity(userDTO));
        return mapper.entityToResponse(repository.save(entity));
    }

    public EventResponse updateEvent(Long id, EventRequest request, UserDTO userDTO) throws NotFoundException {
        EventDTO event = repository.findById(id)
                .map(mapper::entityToDTO)
                .orElseThrow(NotFoundException::new);
        if (!event.getOwner().getId().equals(userDTO.getId())) {
            throw new PermissionDeniedException();
        }
        log.info("{}", request);
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getTags() != null) {
            event.setTags(request.getTags().stream().map(String::toLowerCase).toList());
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
}
