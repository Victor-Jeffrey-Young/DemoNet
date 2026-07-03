package com.example.demonet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demonet.entity.InviteCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface InviteCodeMapper extends BaseMapper<InviteCode> {
    @Select("SELECT ic.*, u.username as used_by_name FROM invite_codes ic LEFT JOIN users u ON ic.used_by = u.id ORDER BY ic.created_at DESC")
    List<Map<String, Object>> selectWithUsers();
}
