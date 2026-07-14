package com.example.demonet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demonet.entity.CurationCollection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CurationCollectionMapper extends BaseMapper<CurationCollection> {

    @Select("""
            SELECT id, slug, title, description, cover_url AS coverUrl,
                   constraints_json AS constraintsJson, visible,
                   display_order AS displayOrder, created_at AS createdAt, updated_at AS updatedAt
            FROM curation_collections
            WHERE visible = 1
            ORDER BY display_order ASC, id ASC
            LIMIT #{limit}
            """)
    List<CurationCollection> selectVisible(@Param("limit") int limit);

    @Select("""
            SELECT id, slug, title, description, cover_url AS coverUrl,
                   constraints_json AS constraintsJson, visible,
                   display_order AS displayOrder, created_at AS createdAt, updated_at AS updatedAt
            FROM curation_collections
            WHERE slug = #{slug} AND visible = 1
            """)
    CurationCollection selectVisibleBySlug(@Param("slug") String slug);
}
