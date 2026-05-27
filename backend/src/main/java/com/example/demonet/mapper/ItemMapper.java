package com.example.demonet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demonet.entity.Item;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItemMapper extends BaseMapper<Item> {
}
