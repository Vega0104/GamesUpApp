package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.model.Publisher;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublisherServiceTest {

    @Mock
    private PublisherRepository publisherRepository;

    @InjectMocks
    private PublisherService publisherService;

    private Publisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new Publisher();
        publisher.setId(1L);
        publisher.setName("Electronic Arts");
    }

    // ========== CREATE TESTS ==========

    @Test
    void create_ShouldCreatePublisher_WhenValidName() {
        // Arrange
        when(publisherRepository.existsByName("Activision")).thenReturn(false);
        when(publisherRepository.save(any(Publisher.class))).thenAnswer(invocation -> {
            Publisher p = invocation.getArgument(0);
            p.setId(2L);
            return p;
        });

        // Act
        Publisher result = publisherService.create("Activision");

        // Assert
        assertNotNull(result);
        assertEquals("Activision", result.getName());
        verify(publisherRepository).existsByName("Activision");
        verify(publisherRepository).save(any(Publisher.class));
    }

    @Test
    void create_ShouldTrimName_WhenNameHasWhitespace() {
        // Arrange
        when(publisherRepository.existsByName("Bethesda")).thenReturn(false);
        when(publisherRepository.save(any(Publisher.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Publisher result = publisherService.create("  Bethesda  ");

        // Assert
        assertEquals("Bethesda", result.getName());
    }

    @Test
    void create_ShouldThrowException_WhenNameIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> publisherService.create(null)
        );
        assertEquals("Publisher name cannot be null or empty", exception.getMessage());
        verify(publisherRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenNameIsEmpty() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> publisherService.create("   ")
        );
        assertEquals("Publisher name cannot be null or empty", exception.getMessage());
        verify(publisherRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenNameAlreadyExists() {
        // Arrange
        when(publisherRepository.existsByName("Electronic Arts")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> publisherService.create("Electronic Arts")
        );
        assertEquals("Publisher with name 'Electronic Arts' already exists", exception.getMessage());
        verify(publisherRepository, never()).save(any());
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    void findById_ShouldReturnPublisher_WhenIdExists() {
        // Arrange
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));

        // Act
        Optional<Publisher> result = publisherService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Electronic Arts", result.get().getName());
        verify(publisherRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // Arrange
        when(publisherRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Publisher> result = publisherService.findById(99L);

        // Assert
        assertFalse(result.isPresent());
        verify(publisherRepository).findById(99L);
    }

    @Test
    void findById_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> publisherService.findById(null)
        );
        assertEquals("Invalid publisher ID", exception.getMessage());
        verify(publisherRepository, never()).findById(any());
    }

    @Test
    void findById_ShouldThrowException_WhenIdIsZero() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> publisherService.findById(0L)
        );
        assertEquals("Invalid publisher ID", exception.getMessage());
    }

    @Test
    void findById_ShouldThrowException_WhenIdIsNegative() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> publisherService.findById(-1L)
        );
        assertEquals("Invalid publisher ID", exception.getMessage());
    }

    // ========== FIND BY NAME TESTS ==========

    @Test
    void findByName_ShouldReturnPublisher_WhenNameExists() {
        // Arrange
        when(publisherRepository.findByName("Electronic Arts")).thenReturn(Optional.of(publisher));

        // Act
        Optional<Publisher> result = publisherService.findByName("Electronic Arts");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Electronic Arts", result.get().getName());
        verify(publisherRepository).findByName("Electronic Arts");
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenNameDoesNotExist() {
        // Arrange
        when(publisherRepository.findByName("Unknown")).thenReturn(Optional.empty());

        // Act
        Optional<Publisher> result = publisherService.findByName("Unknown");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByName_ShouldThrowException_WhenNameIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> publisherService.findByName(null)
        );
        assertEquals("Publisher name cannot be null or empty", exception.getMessage());
    }

    @Test
    void findByName_ShouldThrowException_WhenNameIsEmpty() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> publisherService.findByName("  ")
        );
        assertEquals("Publisher name cannot be null or empty", exception.getMessage());
    }

    // ========== FIND ALL TESTS ==========

    @Test
    void findAll_ShouldReturnAllPublishers() {
        // Arrange
        Publisher publisher2 = new Publisher();
        publisher2.setId(2L);
        publisher2.setName("Activision");

        when(publisherRepository.findAll()).thenReturn(Arrays.asList(publisher, publisher2));

        // Act
        List<Publisher> result = publisherService.findAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Electronic Arts", result.get(0).getName());
        assertEquals("Activision", result.get(1).getName());
        verify(publisherRepository).findAll();
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoPublishers() {
        // Arrange
        when(publisherRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Publisher> result = publisherService.findAll();

        // Assert
        assertTrue(result.isEmpty());
    }

    // ========== UPDATE TESTS ==========

    @Test
    void update_ShouldUpdatePublisher_WhenValidData() {
        // Arrange
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(publisherRepository.existsByName("Square Enix")).thenReturn(false);
        when(publisherRepository.save(any(Publisher.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Publisher result = publisherService.update(1L, "Square Enix");

        // Assert
        assertEquals("Square Enix", result.getName());
        verify(publisherRepository).findById(1L);
        verify(publisherRepository).save(publisher);
    }

    @Test
    void update_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        when(publisherRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> publisherService.update(99L, "New Name")
        );
        assertEquals("Publisher not found with id: 99", exception.getMessage());
        verify(publisherRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowException_WhenNameAlreadyExists() {
        // Arrange
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(publisherRepository.existsByName("Activision")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> publisherService.update(1L, "Activision")
        );
        assertEquals("Publisher with name 'Activision' already exists", exception.getMessage());
        verify(publisherRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> publisherService.update(null, "Name")
        );
        assertEquals("Invalid publisher ID", exception.getMessage());
    }

    @Test
    void update_ShouldThrowException_WhenNameIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> publisherService.update(1L, null)
        );
        assertEquals("Publisher name cannot be null or empty", exception.getMessage());
    }

    // ========== DELETE TESTS ==========

    @Test
    void delete_ShouldDeletePublisher_WhenIdExists() {
        // Arrange
        when(publisherRepository.existsById(1L)).thenReturn(true);
        doNothing().when(publisherRepository).deleteById(1L);

        // Act
        publisherService.delete(1L);

        // Assert
        verify(publisherRepository).existsById(1L);
        verify(publisherRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        when(publisherRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> publisherService.delete(99L)
        );
        assertEquals("Publisher not found with id: 99", exception.getMessage());
        verify(publisherRepository, never()).deleteById(any());
    }

    @Test
    void delete_ShouldThrowException_WhenIdIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> publisherService.delete(null)
        );
        assertEquals("Invalid publisher ID", exception.getMessage());
    }
}