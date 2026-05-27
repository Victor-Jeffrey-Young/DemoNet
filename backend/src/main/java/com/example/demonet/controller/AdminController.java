package com.example.demonet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demonet.config.RabbitMQConfig;
import com.example.demonet.entity.Item;
import com.example.demonet.mapper.ItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RabbitTemplate rabbitTemplate;
    private final ItemMapper itemMapper;

    @PostMapping("/fetch/steam")
    public Map<String, String> triggerSteam(@RequestBody Map<String, List<Long>> body) {
        List<Long> appIds = body.get("appIds");
        rabbitTemplate.convertAndSend("", RabbitMQConfig.QUEUE_STEAM, appIds);
        return Map.of("message", "Steam fetch task queued for " + appIds.size() + " appIds");
    }

    @PostMapping("/fetch/tmdb")
    public Map<String, String> triggerTMDB(@RequestBody Map<String, String> body) {
        String query = body.get("query");
        rabbitTemplate.convertAndSend("", RabbitMQConfig.QUEUE_TMDB, query);
        return Map.of("message", "TMDB fetch task queued for query: " + query);
    }

    @GetMapping("/pending")
    public IPage<Item> listPending(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<Item> p = new Page<>(page, size);
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getStatus, 0);
        wrapper.orderByDesc(Item::getCreatedAt);
        return itemMapper.selectPage(p, wrapper);
    }

    @PutMapping("/approve/{id}")
    public Item approve(@PathVariable Long id) {
        Item item = itemMapper.selectById(id);
        if (item != null) {
            item.setStatus(1);
            itemMapper.updateById(item);
        }
        return item;
    }

    @PutMapping("/reject/{id}")
    public void reject(@PathVariable Long id) {
        itemMapper.deleteById(id);
    }
}
