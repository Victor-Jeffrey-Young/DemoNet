package com.example.demonet.mapper;

import com.example.demonet.dto.scene.SceneItemSummary;
import com.example.demonet.dto.scene.AdminSceneItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CurationCollectionItemMapper {

    @Select("""
            SELECT i.id, i.type, i.title, i.slug, i.cover_url AS coverUrl, i.status,
                   ci.reason, ci.editor_note AS editorNote
            FROM curation_collection_items ci
            JOIN items i ON i.id = ci.item_id
            WHERE ci.collection_id = #{collectionId}
            ORDER BY ci.display_order ASC, i.id ASC
            """)
    List<AdminSceneItem> selectAdminItems(@Param("collectionId") Long collectionId);

    @Delete("DELETE FROM curation_collection_items WHERE collection_id = #{collectionId}")
    int deleteByCollectionId(@Param("collectionId") Long collectionId);

    @Insert("""
            INSERT INTO curation_collection_items (collection_id, item_id, display_order, reason, editor_note)
            VALUES (#{collectionId}, #{itemId}, #{displayOrder}, #{reason}, #{editorNote})
            """)
    int insert(@Param("collectionId") Long collectionId, @Param("itemId") Long itemId,
               @Param("displayOrder") int displayOrder, @Param("reason") String reason,
               @Param("editorNote") String editorNote);

    @Select("""
            SELECT i.id, i.type, i.title, i.slug, i.cover_url AS coverUrl,
                   i.wide_cover_url AS wideCoverUrl, i.description, ci.reason
            FROM curation_collection_items ci
            JOIN items i ON i.id = ci.item_id
            WHERE ci.collection_id = #{collectionId} AND i.status = 1
            ORDER BY ci.display_order ASC, i.id ASC
            """)
    List<SceneItemSummary> selectPublishedItems(@Param("collectionId") Long collectionId);
}
