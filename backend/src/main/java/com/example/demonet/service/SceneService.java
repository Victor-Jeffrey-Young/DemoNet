package com.example.demonet.service;

import com.example.demonet.common.BusinessException;
import com.example.demonet.dto.scene.SceneDetail;
import com.example.demonet.dto.scene.SceneItemSummary;
import com.example.demonet.dto.scene.SceneSummary;
import com.example.demonet.entity.CurationCollection;
import com.example.demonet.mapper.CurationCollectionItemMapper;
import com.example.demonet.mapper.CurationCollectionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SceneService {

    private final CurationCollectionMapper collectionMapper;
    private final CurationCollectionItemMapper collectionItemMapper;

    public List<SceneSummary> listVisibleScenes(int limit) {
        return collectionMapper.selectVisible(limit).stream()
                .map(this::toSummary)
                .toList();
    }

    public SceneDetail getVisibleScene(String slug) {
        CurationCollection collection = collectionMapper.selectVisibleBySlug(slug);
        if (collection == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "场景不存在或未发布");
        }

        SceneDetail detail = new SceneDetail();
        copySummary(collection, detail);
        List<SceneItemSummary> items = collectionItemMapper.selectPublishedItems(collection.getId());
        detail.setItems(items);
        return detail;
    }

    private SceneSummary toSummary(CurationCollection collection) {
        SceneSummary summary = new SceneSummary();
        copySummary(collection, summary);
        return summary;
    }

    private void copySummary(CurationCollection source, SceneSummary target) {
        target.setSlug(source.getSlug());
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setCoverUrl(source.getCoverUrl());
        target.setConstraintsJson(source.getConstraintsJson());
    }
}
