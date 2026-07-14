package com.example.demonet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demonet.common.BusinessException;
import com.example.demonet.dto.request.AdminSceneRequests.SceneItemReq;
import com.example.demonet.dto.request.AdminSceneRequests.SceneItemsSaveReq;
import com.example.demonet.dto.request.AdminSceneRequests.SceneSaveReq;
import com.example.demonet.dto.scene.AdminSceneItem;
import com.example.demonet.entity.CurationCollection;
import com.example.demonet.entity.Item;
import com.example.demonet.mapper.CurationCollectionItemMapper;
import com.example.demonet.mapper.CurationCollectionMapper;
import com.example.demonet.mapper.ItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminSceneService {

    private final CurationCollectionMapper collectionMapper;
    private final CurationCollectionItemMapper collectionItemMapper;
    private final ItemMapper itemMapper;
    private final ObjectMapper objectMapper;

    public List<CurationCollection> listCollections() {
        return collectionMapper.selectList(new LambdaQueryWrapper<CurationCollection>()
                .orderByAsc(CurationCollection::getDisplayOrder)
                .orderByAsc(CurationCollection::getId));
    }

    public CurationCollection getCollection(Long id) {
        CurationCollection collection = collectionMapper.selectById(id);
        if (collection == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "场景不存在");
        }
        return collection;
    }

    public List<AdminSceneItem> getCollectionItems(Long id) {
        getCollection(id);
        return collectionItemMapper.selectAdminItems(id);
    }

    @Transactional
    public CurationCollection createCollection(SceneSaveReq request) {
        CurationCollection collection = toCollection(request, new CurationCollection());
        collectionMapper.insert(collection);
        return collection;
    }

    @Transactional
    public CurationCollection updateCollection(Long id, SceneSaveReq request) {
        CurationCollection collection = getCollection(id);
        toCollection(request, collection);
        collectionMapper.updateById(collection);
        return collection;
    }

    @Transactional
    public void replaceItems(Long collectionId, SceneItemsSaveReq request) {
        getCollection(collectionId);
        List<SceneItemReq> requestedItems = request.getItems();
        Set<Long> itemIds = new HashSet<>();
        for (SceneItemReq item : requestedItems) {
            if (!itemIds.add(item.getItemId())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "同一内容不能重复加入场景");
            }
        }
        List<Item> existingItems = itemIds.isEmpty() ? List.of() : itemMapper.selectByIds(itemIds);
        if (existingItems.size() != itemIds.size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "存在不存在的内容 ID");
        }
        collectionItemMapper.deleteByCollectionId(collectionId);
        for (int index = 0; index < requestedItems.size(); index++) {
            SceneItemReq item = requestedItems.get(index);
            collectionItemMapper.insert(collectionId, item.getItemId(), index, item.getReason().trim(), blankToNull(item.getEditorNote()));
        }
    }

    @Transactional
    public void deleteCollection(Long id) {
        getCollection(id);
        collectionMapper.deleteById(id);
    }

    private CurationCollection toCollection(SceneSaveReq request, CurationCollection collection) {
        validateConstraintsJson(request.getConstraintsJson());
        collection.setSlug(request.getSlug().trim());
        collection.setTitle(request.getTitle().trim());
        collection.setDescription(request.getDescription().trim());
        collection.setCoverUrl(blankToNull(request.getCoverUrl()));
        collection.setConstraintsJson(blankToNull(request.getConstraintsJson()));
        collection.setVisible(request.getVisible());
        collection.setDisplayOrder(request.getDisplayOrder());
        return collection;
    }

    private void validateConstraintsJson(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            if (!objectMapper.readTree(value).isObject()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "场景约束必须是 JSON 对象");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "场景约束不是有效 JSON");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
