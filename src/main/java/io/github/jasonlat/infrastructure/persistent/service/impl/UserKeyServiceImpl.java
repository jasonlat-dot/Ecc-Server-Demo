package io.github.jasonlat.infrastructure.persistent.service.impl;

import io.github.jasonlat.infrastructure.persistent.po.UserKey;
import io.github.jasonlat.infrastructure.persistent.dao.IUserKeyDao;
import io.github.jasonlat.infrastructure.persistent.service.IUserKeyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lijiaqiang@ljq1024.cc
 * @since 2025-08-09
 */
@Service
public class UserKeyServiceImpl extends ServiceImpl<IUserKeyDao, UserKey> implements IUserKeyService {

}
