package io.github.jasonlat.infrastructure.persistent.dao;

import io.github.jasonlat.infrastructure.persistent.po.UserKey;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lijiaqiang@ljq1024.cc
 * @since 2025-08-09
 */
@Mapper
public interface IUserKeyDao extends BaseMapper<UserKey> {

    UserKey queryUserPublicKey(@Param("user") String user);
}
