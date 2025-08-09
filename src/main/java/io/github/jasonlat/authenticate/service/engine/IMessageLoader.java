package io.github.jasonlat.authenticate.service.engine;

/**
 * @author li--jiaqiang 2024−12−23
 */
public interface IMessageLoader {

    /**
     * 发送验证码
     */
    void sendCode(String code, String contactInformation);

}