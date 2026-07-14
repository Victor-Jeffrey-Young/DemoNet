package com.example.demonet.controller;

import com.example.demonet.dto.request.AdminSceneRequests.SceneItemsSaveReq;
import com.example.demonet.dto.request.AdminSceneRequests.SceneSaveReq;
import com.example.demonet.entity.CurationCollection;
import com.example.demonet.dto.scene.AdminSceneItem;
import com.example.demonet.service.AdminSceneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/scenes")
@RequiredArgsConstructor
public class AdminSceneController {

    private final AdminSceneService adminSceneService;

    @GetMapping
    public List<CurationCollection> list() {
        return adminSceneService.listCollections();
    }

    @GetMapping("/{id}")
    public CurationCollection detail(@PathVariable Long id) {
        return adminSceneService.getCollection(id);
    }

    @GetMapping("/{id}/items")
    public List<AdminSceneItem> items(@PathVariable Long id) {
        return adminSceneService.getCollectionItems(id);
    }

    @PostMapping
    public CurationCollection create(@Valid @RequestBody SceneSaveReq request) {
        return adminSceneService.createCollection(request);
    }

    @PutMapping("/{id}")
    public CurationCollection update(@PathVariable Long id, @Valid @RequestBody SceneSaveReq request) {
        return adminSceneService.updateCollection(id, request);
    }

    @PutMapping("/{id}/items")
    public Map<String, String> replaceItems(@PathVariable Long id, @Valid @RequestBody SceneItemsSaveReq request) {
        adminSceneService.replaceItems(id, request);
        return Map.of("message", "场景条目已保存");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminSceneService.deleteCollection(id);
        return ResponseEntity.noContent().build();
    }
}
