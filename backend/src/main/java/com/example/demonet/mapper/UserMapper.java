package com.example.demonet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demonet.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT id, username, email, role, enabled, created_at FROM users ORDER BY created_at DESC")
    List<Map<String, Object>> selectUserList();
}
