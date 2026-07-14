package com.example.demonet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demonet.entity.CurationCollection;
import com.example.demonet.mapper.CurationCollectionMapper;
import com.example.demonet.mapper.CurationCollectionItemMapper;
import com.example.demonet.mapper.ItemMapper;
import com.example.demonet.dto.request.AdminSceneRequests.SceneSaveReq;
import com.example.demonet.common.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSceneServiceTest {

    @Mock
    private CurationCollectionMapper collectionMapper;
    @Mock
    private CurationCollectionItemMapper collectionItemMapper;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private AdminSceneService adminSceneService;

    @Test
    void listsVisibleAndHiddenCollectionsInDisplayOrder() {
        CurationCollection draft = new CurationCollection();
        draft.setSlug("solo-quick-start");
        draft.setVisible(0);
        when(collectionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(draft));

        List<CurationCollection> result = adminSceneService.listCollections();

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getVisible());
    }

    @Test
    void rejectsMalformedSceneConstraintsBeforeDatabaseWrite() {
        SceneSaveReq request = new SceneSaveReq();
        request.setSlug("solo-quick-start");
        request.setTitle("一个人，30 分钟内，在线开始");
        request.setDescription("测试场景");
        request.setConstraintsJson("not-json");
        request.setVisible(0);
        request.setDisplayOrder(0);

        BusinessException error = assertThrows(BusinessException.class, () -> adminSceneService.createCollection(request));

        assertEquals(400, error.getStatus().value());
    }
}
