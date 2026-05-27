package com.example.demonet.controller;

import com.example.demonet.entity.Tag;
import com.example.demonet.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public List<Tag> list() {
        return tagService.listAll();
    }

    @PostMapping
    public Tag create(@RequestBody Map<String, String> body) {
        return tagService.create(body.get("name"));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        tagService.delete(id);
    }

    @GetMapping("/items/{itemId}")
    public List<Tag> getTags(@PathVariable Long itemId) {
        return tagService.getTagsForItem(itemId);
    }

    @PostMapping("/items/{itemId}")
    public void associate(@PathVariable Long itemId, @RequestBody Map<String, List<Long>> body) {
        tagService.associateItem(itemId, body.get("tagIds"));
    }
}
