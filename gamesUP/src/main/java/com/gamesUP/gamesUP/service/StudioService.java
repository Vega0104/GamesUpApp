package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.Author;
import com.gamesUP.gamesUP.repository.AuthorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudioService {

    private final AuthorRepository studioRepository;

    public StudioService(AuthorRepository authorRepository) {
        this.studioRepository = authorRepository;
    }

    public Author create(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Studio name cannot be null or empty");
        }

        String trimmedName = name.trim();

        if (studioRepository.existsByName(trimmedName)) {
            throw new IllegalArgumentException("Studio with name '" + trimmedName + "' already exists");
        }

        Author author = new Author();
        author.setName(trimmedName);

        return studioRepository.save(author);
    }

    public Optional<Author> findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid studio ID");
        }
        return studioRepository.findById(id);
    }

    public Optional<Author> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Studio name cannot be null or empty");
        }
        return studioRepository.findByName(name.trim());
    }

    public List<Author> findAll() {
        return studioRepository.findAll();
    }

    public Author update(Long id, String name) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid studio ID");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Studio name cannot be null or empty");
        }

        Author author = studioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Studio not found with id: " + id));

        String trimmedName = name.trim();

        if (!author.getName().equals(trimmedName) && studioRepository.existsByName(trimmedName)) {
            throw new IllegalArgumentException("Studio with name '" + trimmedName + "' already exists");
        }

        author.setName(trimmedName);

        return studioRepository.save(author);
    }

    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid studio ID");
        }

        if (!studioRepository.existsById(id)) {
            throw new IllegalArgumentException("Studio not found with id: " + id);
        }

        studioRepository.deleteById(id);
    }
}