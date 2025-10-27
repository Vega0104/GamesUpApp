//package com.gamesUP.gamesUP.controller;
//
//import com.gamesUP.gamesUP.service.GameService;
//import com.gamesUP.gamesUP.model.Game;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//import java.net.URI;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/games")
//@Validated
//public class GameController {
//
//    private final GameService gameService;
//    private final GameMapper mapper;
//
//    public GameController(GameService gameService, GameMapper mapper) {
//        this.gameService = gameService;
//        this.mapper = mapper;
//    }
//
//    // CREATE
//    @PostMapping
//    public ResponseEntity<GameResponse> create(@Valid @RequestBody GameRequest req) {
//        Game created = gameService.create(
//                req.title(),
//                req.description(),
//                req.releaseDate(),
//                req.basePrice(),
//                req.currency(),
//                req.pegiRating(),
//                req.categoryId(),
//                req.publisherId(),
//                req.studioId()
//        );
//        return ResponseEntity
//                .created(URI.create("/api/games/" + created.getId()))
//                .body(mapper.toResponse(created));
//    }
//
//    // READ by ID
//    @GetMapping("/{id}")
//    public ResponseEntity<GameResponse> getById(@PathVariable @Min(1) Long id) {
//        return gameService.findById(id)
//                .map(mapper::toResponse)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    // READ by slug
//    @GetMapping("/slug/{slug}")
//    public ResponseEntity<GameResponse> getBySlug(@PathVariable @NotBlank String slug) {
//        return gameService.findBySlug(slug)
//                .map(mapper::toResponse)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    // LIST all
//    @GetMapping
//    public List<GameResponse> listAll() {
//        return gameService.findAll().stream().map(mapper::toResponse).toList();
//    }
//
//    // SEARCH by title (contains, case-insensitive)
//    @GetMapping("/search")
//    public List<GameResponse> searchByTitle(@RequestParam("title") @NotBlank String title) {
//        return gameService.findByTitle(title).stream().map(mapper::toResponse).toList();
//    }
//
//    // FILTER by category
//    @GetMapping("/category/{categoryId}")
//    public List<GameResponse> byCategory(@PathVariable @Min(1) Long categoryId) {
//        return gameService.findByCategory(categoryId).stream().map(mapper::toResponse).toList();
//    }
//
//    // FILTER by publisher
//    @GetMapping("/publisher/{publisherId}")
//    public List<GameResponse> byPublisher(@PathVariable @Min(1) Long publisherId) {
//        return gameService.findByPublisher(publisherId).stream().map(mapper::toResponse).toList();
//    }
//
//    // FILTER by studio (auteur/dev team)
//    @GetMapping("/studio/{studioId}")
//    public List<GameResponse> byStudio(@PathVariable @Min(1) Long studioId) {
//        return gameService.findByStudio(studioId).stream().map(mapper::toResponse).toList();
//    }
//
//    // UPDATE
//    @PutMapping("/{id}")
//    public ResponseEntity<GameResponse> update(@PathVariable @Min(1) Long id,
//                                               @Valid @RequestBody GameRequest req) {
//        Game updated = gameService.update(
//                id,
//                req.title(),
//                req.description(),
//                req.releaseDate(),
//                req.basePrice(),
//                req.currency(),
//                req.pegiRating(),
//                req.categoryId(),
//                req.publisherId(),
//                req.studioId()
//        );
//        return ResponseEntity.ok(mapper.toResponse(updated));
//    }
//
//    // DELETE
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable @Min(1) Long id) {
//        gameService.delete(id);
//        return ResponseEntity.noContent().build();
//    }
//}
