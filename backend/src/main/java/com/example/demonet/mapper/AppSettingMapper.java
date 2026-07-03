package com.example.demonet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demonet.entity.AppSetting;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppSettingMapper extends BaseMapper<AppSetting> {
}
