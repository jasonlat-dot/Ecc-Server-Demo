package io.github.jasonlat.trigger.http;

import cc.jq1024.middleware.redisson.IRedissonService;
import com.alibaba.fastjson.JSON;
import io.github.jasonlat.authenticate.model.entity.RegisterRequest;
import io.github.jasonlat.authenticate.model.entity.SingInEntity;
import io.github.jasonlat.authenticate.model.entity.UserEntity;
import io.github.jasonlat.authenticate.service.IAccountService;
import io.github.jasonlat.authenticate.service.IAuthenticateService;
import io.github.jasonlat.middleware.annotations.decrypt.RequestDecryption;
import io.github.jasonlat.middleware.annotations.encrypt.RequestEncryption;
import io.github.jasonlat.middleware.domain.model.entity.ServerPublicKeyData;
import io.github.jasonlat.middleware.domain.model.valobj.EccDecryptType;
import io.github.jasonlat.middleware.domain.service.ECCSecurityService;
import io.github.jasonlat.trigger.model.SignInRequest;
import io.github.jasonlat.trigger.model.SingInResponse;
import io.github.jasonlat.types.enums.ResponseCode;
import io.github.jasonlat.types.exception.AppException;
import io.github.jasonlat.types.model.Response;
import io.github.jasonlat.types.utils.Validator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.*;


/**
 * @author li--jiaqiang 2025−06−17
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/auth/")
@CrossOrigin(value = {"*"})
public class AuthenticateController {

    private final IAuthenticateService authenticateService;
    private final ECCSecurityService eccSecurityService;
    private final IAccountService accountService;
    private final IRedissonService redissonService;

    /**
     * 登录
     * @param signInRequest 登录请求实体
     */
    @RequestDecryption(requestType = EccDecryptType.NOT_IDENTIFICATION)
    @RequestEncryption()
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public Response<SingInResponse> login(@RequestBody SignInRequest signInRequest) {
        try {
            log.info("UserAuthenticateController.signIn(). 登录开始执行...");
            // 1. 参数校验
            if (StringUtils.isAnyBlank(signInRequest.getPassword(), signInRequest.getUsername())) {
                log.warn("UserAuthenticateController.signIn(). 参数错误: {}", JSON.toJSONString(signInRequest));
                return Response.<SingInResponse>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }
            // 密码正则校验：必须包括数字和大小写英文字符，不能包含特殊字符
            if (!signInRequest.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")) {
                log.warn("密码长度至少为8位,且必须包含数字和大小写英文字符");
                throw new IllegalArgumentException("密码必须包含数字和大小写英文字符，且长度至少为8位");
            }

            // 2. 业务
            SingInEntity singInEntity = SingInEntity.builder()
                    .username(signInRequest.getUsername())
                    .password(signInRequest.getPassword())
                    .build();
            UserEntity userEntity = authenticateService.signIn(singInEntity);
            // 3. 参数转换
            SingInResponse singInResponse = SingInResponse.builder()
                    .token(userEntity.getToken())
                    .avatarUrl(userEntity.getAvatarUrl())
                    .email(userEntity.getEmail())
                    .build();
            // 4. 返回结果
            log.info("UserAuthenticateController.signIn(). 执行完毕");
            return Response.<SingInResponse>builder()
                    .data(singInResponse)
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (AppException exception) {
            log.error("AppException: ",exception);
            return Response.<SingInResponse>builder()
                    .code(exception.getCode())
                    .info(exception.getInfo())
                    .build();
        } catch (Exception exception) {
            log.error("Exception: ",exception);
            return Response.<SingInResponse>builder()
                    .code(ResponseCode.SERVER_ERROR.getCode())
                    .info(ResponseCode.SERVER_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 获取服务器公钥
     */
    @PostMapping("/server/key")
    public Response<ServerPublicKeyData> getServerPublic() {
        ServerPublicKeyData serverPublicData = eccSecurityService.getServerPublicData();
        return Response.<ServerPublicKeyData>builder()
                .data(serverPublicData)
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
    }

    /**
     * 生成验证码
     */
    @RequestMapping(value = "code", method = RequestMethod.GET)
    public Response<String> genCode(@RequestParam("email") String email) {
        try {
            log.info("UserAuthenticateController.genCode() 开始准备发送验证码: {}", email);
            if (StringUtils.isAnyBlank(email)) {
                return Response.<String>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }
            // 生成验证码
            String code = authenticateService.genCode(email);
            // 发送验证码
            authenticateService.sendCode(email, code);
            return Response.<String>builder()
                    .data(code)
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (IllegalArgumentException exception) {
            log.error("UserAuthenticateController.genCode: {}", "参数异常");
            return Response.<String>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(exception.getMessage() == null ? ResponseCode.ILLEGAL_PARAMETER.getCode() : exception.getMessage())
                    .build();
        } catch (AppException exception) {
            log.error("UserAuthenticateController.genCode - AppException: {}", exception.getInfo());
            return Response.<String>builder()
                    .code(exception.getCode())
                    .info(exception.getInfo())
                    .build();
        } catch (Exception exception) {
            log.error("UserAuthenticateController.genCode - Exception: {}", exception.getMessage());
            return Response.<String>builder()
                    .code(ResponseCode.SERVER_ERROR.getCode())
                    .info(ResponseCode.SERVER_ERROR.getInfo())
                    .build();
        }

    }


    @RequestDecryption(requestType = EccDecryptType.REGISTER)
    @RequestEncryption()
    @RequestMapping(value = "register", method = RequestMethod.POST)
    public Response<String> register(@RequestBody RegisterRequest registerRequest) {
        try {
            log.debug("UserAuthenticateController.register 开始注册: {}", registerRequest.toString());
            // 参数校验
            if (StringUtils.isAnyBlank(registerRequest.getEmail(), registerRequest.getPassword(),
                    registerRequest.getConfirmPassword(), registerRequest.getVerificationCode(),
                    registerRequest.getUserPublicX(), registerRequest.getUserPublicY())) {
                log.warn("register 参数异常");
                throw new IllegalArgumentException();
            }
            // 业务
            // 0. 校验验证码
            authenticateService.verifyCode(registerRequest.getEmail(), registerRequest.getVerificationCode());
            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                log.warn("两次输入的密码不一致");
                throw new IllegalArgumentException("两次输入的密码不一致");
            }
            // 密码正则校验：必须包括数字和大小写英文字符，不能包含特殊字符
            if (!Validator.validatePassword(registerRequest.getPassword())) {
                log.warn("密码必须包含数字和大小写英文字符，且长度至少为8位");
                throw new IllegalArgumentException("密码必须包含数字和大小写英文字符，且长度至少为8位");
            }
            accountService.verifyAccountExist(registerRequest.getEmail());
            // 2. 进行注册
            Long userId = accountService.register(registerRequest.getEmail(), registerRequest.getPassword());
            // 3. 保存公钥
            accountService.saveUserPublicKey(userId, registerRequest.getUserPublicX(), registerRequest.getUserPublicY());
            // 返回结果
            return Response.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data("注册成功")
                    .build();
        } catch (IllegalArgumentException exception) {
            log.error("UserAuthenticateController.register: {}", "参数异常");
            return Response.<String>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(exception.getMessage() == null ? ResponseCode.ILLEGAL_PARAMETER.getCode() : exception.getMessage())
                    .build();
        } catch (AppException exception) {
            log.error("UserAuthenticateController.register - AppException: {}", exception.getInfo());
            return Response.<String>builder()
                    .code(exception.getCode())
                    .info(exception.getInfo())
                    .build();
        } catch (Exception exception) {
            log.error("UserAuthenticateController.register - Exception: {}", exception.getMessage());
            return Response.<String>builder()
                    .code(ResponseCode.SERVER_ERROR.getCode())
                    .info(ResponseCode.SERVER_ERROR.getInfo())
                    .build();
        }
    }


    @RequestMapping(value = "verifyToken", method = RequestMethod.POST)
    public Response<Boolean> verifyToken(@Header("Authorization") String userToken) {
        try {
            String simpleUUID = userToken.replace("Bearer ", "");
            boolean result = authenticateService.verifyToken(simpleUUID);
            return Response.ok(result);
        } catch (Exception exception) {
            log.error("UserAuthenticateController.verifyToken - Exception: {}", exception.getMessage());
            return Response.<Boolean>builder()
                    .data(Boolean.FALSE)
                    .code(ResponseCode.SERVER_ERROR.getCode())
                    .info(ResponseCode.SERVER_ERROR.getInfo())
                    .build();
        }
    }

}