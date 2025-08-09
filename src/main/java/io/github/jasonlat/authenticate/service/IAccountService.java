package io.github.jasonlat.authenticate.service;

/**
 * @author li--jiaqiang 2025−06−17
 */
public interface IAccountService {

    void verifyAccountExist(String username);

    Long register(String username, String password);

    void saveUserPublicKey(Long userId, String userPublicX, String userPublicY);
}