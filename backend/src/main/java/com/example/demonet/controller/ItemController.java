package com.example.demonet.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demonet.entity.Item;
import com.example.demonet.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public IPage<Item> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> tags) {
        return itemService.listItems(page, size, type, keyword, tags);
    }

    @GetMapping("/{slug}")
    public Item detail(@PathVariable String slug) {
        return itemService.getBySlug(slug);
    }

    @PostMapping
    public Item create(@RequestBody Item item) {
        return itemService.createItem(item);
    }

    @PutMapping("/{id}")
    public Item update(@PathVariable Long id, @RequestBody Item item) {
        return itemService.updateItem(id, item);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        itemService.deleteItem(id);
    }

    @GetMapping("/hot")
    public List<Item> hot(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "6") Integer limit) {
        return itemService.listHotItems(type, limit);
    }

    @GetMapping("/types")
    public List<Item> byType(@RequestParam String type) {
        return itemService.listByType(type);
    }

    @GetMapping("/recommended")
    public List<Item> recommended(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "6") Integer limit) {
        return itemService.listRecommended(type, limit);
    }
}
