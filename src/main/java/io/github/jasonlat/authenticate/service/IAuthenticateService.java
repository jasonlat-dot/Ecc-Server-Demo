package io.github.jasonlat.authenticate.service;


import io.github.jasonlat.authenticate.model.entity.SingInEntity;
import io.github.jasonlat.authenticate.model.entity.UserEntity;
import io.github.jasonlat.authenticate.model.entity.UserPublicKey;

import java.lang.reflect.InvocationTargetException;

/**
 * @author li--jiaqiang 2024−12−23
 */
public interface IAuthenticateService {
    /**
     * 登录业务
     * @param singInEntity 登录实体
     */
    UserEntity signIn(SingInEntity singInEntity) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException;

    /**
     * 生成验证码
     */
    String genCode(String contactInformation);

    /**
     * 生成验证码
     */
    String genCode(String contactInformation, int length);

    /**
     * 生成验证码
     */
    String genCode(String contactInformation,int cacheTime, int length);

    /**
     * 发送验证码
     * @param contactInformation 电话 or 邮箱
     * @param code 验证码
     */
    void sendCode(String contactInformation,  String code);

    /**
     * 校验验证码
     */
    void verifyCode(String username, String code);

    boolean verifyToken(String simpleUUID);

    UserPublicKey queryUserPublicKey(String userId);
}