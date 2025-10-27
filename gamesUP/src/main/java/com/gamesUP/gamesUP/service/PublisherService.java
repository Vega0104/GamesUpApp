package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PublisherService {

    private final PublisherRepository publisherRepository;

    public PublisherService(PublisherRepository publisherRepository) {
        this.publisherRepository = publisherRepository;
    }

    public Publisher create(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Publisher name cannot be null or empty");
        }

        String trimmedName = name.trim();

        if (publisherRepository.existsByName(trimmedName)) {
            throw new IllegalArgumentException("Publisher with name '" + trimmedName + "' already exists");
        }

        Publisher publisher = new Publisher();
        publisher.setName(trimmedName);

        return publisherRepository.save(publisher);
    }

    public Optional<Publisher> findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid publisher ID");
        }
        return publisherRepository.findById(id);
    }

    public Optional<Publisher> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Publisher name cannot be null or empty");
        }
        return publisherRepository.findByName(name.trim());
    }

    public List<Publisher> findAll() {
        return publisherRepository.findAll();
    }

    public Publisher update(Long id, String name) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid publisher ID");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Publisher name cannot be null or empty");
        }

        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Publisher not found with id: " + id));

        String trimmedName = name.trim();

        if (!publisher.getName().equals(trimmedName) && publisherRepository.existsByName(trimmedName)) {
            throw new IllegalArgumentException("Publisher with name '" + trimmedName + "' already exists");
        }

        publisher.setName(trimmedName);

        return publisherRepository.save(publisher);
    }

    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid publisher ID");
        }

        if (!publisherRepository.existsById(id)) {
            throw new IllegalArgumentException("Publisher not found with id: " + id);
        }

        publisherRepository.deleteById(id);
    }
}