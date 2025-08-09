package io.github.jasonlat.authenticate.service;

import cc.jq1024.middleware.redisson.IRedissonService;
import cc.jq1024.middleware.token.service.ITokenService;

import io.github.jasonlat.authenticate.model.entity.SingInEntity;
import io.github.jasonlat.authenticate.model.entity.UserEntity;
import io.github.jasonlat.authenticate.model.entity.UserPublicKey;
import io.github.jasonlat.authenticate.repository.IAuthenticateRepository;
import io.github.jasonlat.authenticate.service.engine.IMessageLoader;
import io.github.jasonlat.authenticate.service.engine.factory.AuthenticateFactory;
import io.github.jasonlat.types.exception.CacheCodeIllegalException;
import io.github.jasonlat.types.exception.UsernameOrPasswordNotCorrectException;
import io.github.jasonlat.types.security.constants.SecurityConstant;
import io.github.jasonlat.types.security.model.SecurityUser;
import io.github.jasonlat.types.security.util.CacheService;
import io.github.jasonlat.types.utils.IdUtils;
import io.github.jasonlat.types.utils.RandomCodeUtil;
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.github.jasonlat.types.enums.ResponseCode.CACHE_CODE_ILLEGAL;
import static io.github.jasonlat.types.utils.Constant.LOGIN_CODE_EXP;


/**
 * @author li--jiaqiang 2024−12−23
 */
@Service
public class AuthenticateService implements IAuthenticateService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticateService.class);

    private final AuthenticateFactory authenticateFactory;
    private final ITokenService tokenService;
    private final IRedissonService redissonService;
    private final UserDetailsService userDetailsService;
    private final CacheService cacheService;
    private final PasswordEncoder passwordEncoder;
    private final IAuthenticateRepository userRepository;

    public AuthenticateService(AuthenticateFactory authenticateFactory, ITokenService tokenService, IRedissonService redissonService, PasswordEncoder passwordEncoder, UserDetailsService userDetailsService, CacheService cacheService, PasswordEncoder passwordEncoder1, IAuthenticateRepository userRepository) {
        this.authenticateFactory = authenticateFactory;
        this.tokenService = tokenService;
        this.redissonService = redissonService;
        this.userDetailsService = userDetailsService;
        this.cacheService = cacheService;
        this.passwordEncoder = passwordEncoder1;
        this.userRepository = userRepository;
    }

    /**
     * 快捷登录
     * @param singInEntity 登录实体
     */
    @Override
    public UserEntity signIn(SingInEntity singInEntity)  {
        // 1. 查询数据库用户信息
        UserEntity userEntity = userRepository.queryUserInfoWithEmail(singInEntity.getUsername());
        // 校验密码是否正确
        boolean matches = passwordEncoder.matches(singInEntity.getPassword(), userEntity.getPassword());
        if (!matches) {
            throw new UsernameOrPasswordNotCorrectException("Password is incorrect.");
        }
        // 2 执行security
        // 手动调用 userDetailsService() 方法
        SecurityUser securityUser = (SecurityUser) userDetailsService.loadUserByUsername(userEntity.getUserId().toString());
        // 缓存用户信息
        cacheService.setAuthorizationWithUsername(userEntity.getUserId().toString(), securityUser, SecurityConstant.TOKEN_EXPIRE, TimeUnit.SECONDS);
        log.info("用户:{} 密码登录执行 loadUserByUsername() 方法完成 - Cache SecurityUser",securityUser.getUsername());

        // 3. 验证成功，生成token进行返回
        String jwt = tokenService.createJWT(new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;
            {
                put("email", singInEntity.getUsername());
                put("roleType", userEntity.getUserType());
                put("lastLoginTimi", userEntity.getLastLoginTime());
                put("userId", userEntity.getUserId());
            }
        }, userEntity.getUserId().toString());

        // 4. 随机生成一个token
        String token = IdUtils.simpleUUID();

        // 5. 将token存入Redis
        redissonService.setValue(token, jwt, SecurityConstant.TOKEN_EXPIRE, TimeUnit.SECONDS);
        log.info("用户:{} 登录成功，生成token: {}", singInEntity.getUsername(), token);

        // 6. 返回用户信息
        return UserEntity.builder()
                .token(token)
                .email(singInEntity.getUsername())
                .build();
    }

    @Override
    public String genCode(String username) {
        return genCode(username, LOGIN_CODE_EXP, 4);
    }

    @Override
    public String genCode(String username,int length) {
        return genCode(username,  LOGIN_CODE_EXP, length);
    }

    @Override
    public String genCode(String username, int cacheTime, int length) {
        String value = redissonService.getValue(username);
        if (!StringUtils.isAnyBlank(value)) {
            redissonService.setValue(username, value, LOGIN_CODE_EXP, TimeUnit.MINUTES);
            return value;
        }
        value = RandomCodeUtil.generateRandomCode(length);
        redissonService.setValue(username, value, cacheTime, TimeUnit.MINUTES);
        log.info("生成验证码: {}", value);
        return value;
    }

    @Override
    public void sendCode(String contactInformation, String code) {
        Map<String, IMessageLoader> messageLoaderMap = authenticateFactory.getMessageLoaderMap();
        AuthenticateFactory.MessageLoaderType messageLoaderType = AuthenticateFactory.getMessageLoaderType(contactInformation);
        IMessageLoader iMessageLoader = messageLoaderMap.get(messageLoaderType.getActuatorName());
        iMessageLoader.sendCode(code, contactInformation);
        log.info("验证码：{} 已发送：{}", code, contactInformation);
    }

    @Override
    public void verifyCode(String username, String code) {
        String value = redissonService.getValue(username);
       if (code == null || !code.equals(value)) {
            log.error("验证码校验失败: {}, {}", code, value);
           throw new CacheCodeIllegalException(CACHE_CODE_ILLEGAL.getCode(), CACHE_CODE_ILLEGAL.getInfo());
       }
        log.info("验证码校验成功: {}", code);
       // 删除验证码
        redissonService.remove(username);
    }

    @Override
    public boolean verifyToken(String simpleUUID) {
        String jwtToken = redissonService.getValue(simpleUUID);
        if (StringUtils.isAnyBlank(jwtToken)) {
            return false;
        }
        return tokenService.isVerify(jwtToken);
    }

    @Override
    public UserPublicKey queryUserPublicKey(String user) {
        return userRepository.queryUserPublicKey(user);
    }

}