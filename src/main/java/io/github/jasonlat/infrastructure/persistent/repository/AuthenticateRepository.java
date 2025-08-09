package io.github.jasonlat.infrastructure.persistent.repository;

import io.github.jasonlat.authenticate.model.entity.UserEntity;
import io.github.jasonlat.authenticate.model.entity.UserPublicKey;
import io.github.jasonlat.authenticate.repository.IAuthenticateRepository;
import io.github.jasonlat.infrastructure.persistent.dao.IUserDao;
import io.github.jasonlat.infrastructure.persistent.dao.IUserKeyDao;
import io.github.jasonlat.infrastructure.persistent.po.User;
import io.github.jasonlat.infrastructure.persistent.po.UserKey;
import io.github.jasonlat.types.exception.RegisterFailedException;
import io.github.jasonlat.types.snow.SnowflakeIdGenerator;
import org.springframework.stereotype.Repository;

import java.util.Date;

import static io.github.jasonlat.types.enums.ResponseCode.REGISTER_FAILED_ERROR;


/**
 * @author li--jiaqiang 2024−12−25
 */
@Repository
public class AuthenticateRepository implements IAuthenticateRepository {

    private final IUserDao userDao;
    private final IUserKeyDao userKeyDao;
    private final SnowflakeIdGenerator snowflakeIdService;
    public AuthenticateRepository(IUserDao userDao, IUserKeyDao userKeyDao, SnowflakeIdGenerator snowflakeIdService) {
        this.userDao = userDao;
        this.userKeyDao = userKeyDao;
        this.snowflakeIdService = snowflakeIdService;
    }

    @Override
    public Long registerWithEmail(String email, String encodedPassword) {
        User user = User.builder()
                .id(snowflakeIdService.nextId())
                .email(email)
                .createdAt(new Date())
                .updatedAt(new Date())
                .password(encodedPassword)
                .userType("NORMAL")
                .build();
        int status = userDao.insert(user);
        if (status != 1) {
            throw new RegisterFailedException(REGISTER_FAILED_ERROR.getCode(), REGISTER_FAILED_ERROR.getInfo());
        }
        // 返回用户ID
        return user.getId();
    }

    @Override
    public UserEntity queryUserInfoWithEmail(String email) {
        User user = userDao.queryOneWithEmail(email);
        // 类型转换
        return getUserEntity(user);
    }

    private UserEntity getUserEntity(User user) {
        if (null == user) {
            return null;
        }
        return UserEntity.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .lastLoginTime(user.getLastLogin())
                .password(user.getPassword())
                .build();
    }


    @Override
    public UserEntity queryUserInfoByUsername(String username) {
        return queryUserInfoWithEmail(username);
    }

    @Override
    public UserEntity queryUserInfoByUserId(String userId) {
        Long id = Long.parseLong(userId);
        User user = userDao.queryOneWithUserId(id);
        // 类型转换
        return getUserEntity(user);
    }

    @Override
    public void saveUserPublicKey(Long userId, String userPublicX, String userPublicY) {
        if (null == userId) {
            throw new RegisterFailedException(REGISTER_FAILED_ERROR.getCode(), "save user public key failed");
        }
        UserEntity userEntity = queryUserInfoByUserId(userId.toString());
        if (null == userEntity) {
            throw new RegisterFailedException(REGISTER_FAILED_ERROR.getCode(), "save user public key failed");
        }

        UserKey userKey = new UserKey();
        userKey.setUserId(userEntity.getUserId());
        userKey.setPublicX(userPublicX);
        userKey.setPublicY(userPublicY);
        userKey.setUsername(userEntity.getEmail());

        userKeyDao.insert(userKey);
    }

    @Override
    public UserPublicKey queryUserPublicKey(String user) {
        UserKey userKey = userKeyDao.queryUserPublicKey(user);
        if (null != userKey) {
            return UserPublicKey.builder()
                    .userPublicX(userKey.getPublicX())
                    .userPublicY(userKey.getPublicY())
                    .build();
        }
        return null;
    }

}