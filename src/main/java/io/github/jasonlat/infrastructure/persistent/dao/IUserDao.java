package io.github.jasonlat.infrastructure.persistent.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.jasonlat.infrastructure.persistent.po.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lijiaqiang@ljq1024.cc
 * @since 2025-06-17
 */
@Mapper
public interface IUserDao extends BaseMapper<User> {

    User queryOneWithEmail(@Param("email") String email);

    User queryOneWithUserId(@Param("id")Long id);

}
