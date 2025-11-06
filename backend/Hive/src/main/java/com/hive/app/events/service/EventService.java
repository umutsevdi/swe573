package com.hive.app.events.service;

import com.hive.app.events.mapper.EventMapper;
import com.hive.app.events.model.EventDTO;
import com.hive.app.events.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository repository;
    private final EventMapper mapper = EventMapper.INSTANCE;

    public void createEvent() {

    }

    public Optional<EventDTO> findById(Long id) {
        return repository.findById(id).map(mapper::entityToDTO);
    }

}
