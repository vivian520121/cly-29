package com.cly.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cly.project.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
