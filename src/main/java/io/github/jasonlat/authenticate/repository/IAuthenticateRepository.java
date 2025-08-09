package io.github.jasonlat.authenticate.repository;


import io.github.jasonlat.authenticate.model.entity.UserEntity;
import io.github.jasonlat.authenticate.model.entity.UserPublicKey;

/**
 * @author li--jiaqiang 2024−12−25
 */
public interface IAuthenticateRepository {

    Long registerWithEmail(String username, String encodedPassword);

    /**
     * 查询用户信息
     */
    UserEntity queryUserInfoWithEmail(String email);


    UserEntity queryUserInfoByUsername(String username);

    UserEntity queryUserInfoByUserId(String userId);

    void saveUserPublicKey(Long userId, String userPublicX, String userPublicY);

    UserPublicKey queryUserPublicKey(String userId);
}