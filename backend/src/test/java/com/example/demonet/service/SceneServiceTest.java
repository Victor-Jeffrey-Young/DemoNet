package com.example.demonet.service;

import com.example.demonet.common.BusinessException;
import com.example.demonet.dto.scene.SceneDetail;
import com.example.demonet.dto.scene.SceneItemSummary;
import com.example.demonet.dto.scene.SceneSummary;
import com.example.demonet.entity.CurationCollection;
import com.example.demonet.mapper.CurationCollectionItemMapper;
import com.example.demonet.mapper.CurationCollectionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SceneServiceTest {

    @Mock
    private CurationCollectionMapper collectionMapper;

    @Mock
    private CurationCollectionItemMapper collectionItemMapper;

    @InjectMocks
    private SceneService sceneService;

    @Test
    void listVisibleScenesExposesOnlyPublicSummaryFields() {
        CurationCollection collection = collection(1L, "solo-quick-start", "一个人，30 分钟内，在线开始");
        collection.setConstraintsJson("{\"timeMinutes\":30}");
        when(collectionMapper.selectVisible(4)).thenReturn(List.of(collection));

        List<SceneSummary> result = sceneService.listVisibleScenes(4);

        assertThat(result).singleElement().satisfies(scene -> {
            assertThat(scene.getSlug()).isEqualTo("solo-quick-start");
            assertThat(scene.getTitle()).isEqualTo("一个人，30 分钟内，在线开始");
            assertThat(scene.getConstraintsJson()).isEqualTo("{\"timeMinutes\":30}");
        });
    }

    @Test
    void detailIncludesOnlyPublishedItemsAndPublicReasons() {
        CurationCollection collection = collection(2L, "friends-night", "3–5 人的宿舍/朋友夜");
        SceneItemSummary item = new SceneItemSummary();
        item.setSlug("overcooked-2");
        item.setReason("十分钟即可进入合作状态");
        when(collectionMapper.selectVisibleBySlug("friends-night")).thenReturn(collection);
        when(collectionItemMapper.selectPublishedItems(2L)).thenReturn(List.of(item));

        SceneDetail result = sceneService.getVisibleScene("friends-night");

        assertThat(result.getSlug()).isEqualTo("friends-night");
        assertThat(result.getItems()).singleElement().satisfies(sceneItem -> {
            assertThat(sceneItem.getSlug()).isEqualTo("overcooked-2");
            assertThat(sceneItem.getReason()).isEqualTo("十分钟即可进入合作状态");
        });
    }

    @Test
    void detailRejectsHiddenOrMissingScene() {
        when(collectionMapper.selectVisibleBySlug("missing")).thenReturn(null);

        assertThatThrownBy(() -> sceneService.getVisibleScene("missing"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("场景不存在或未发布");
    }

    private CurationCollection collection(Long id, String slug, String title) {
        CurationCollection collection = new CurationCollection();
        collection.setId(id);
        collection.setSlug(slug);
        collection.setTitle(title);
        collection.setDescription("场景说明");
        return collection;
    }
}
