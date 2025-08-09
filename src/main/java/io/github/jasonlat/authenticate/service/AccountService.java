package io.github.jasonlat.authenticate.service;

import io.github.jasonlat.authenticate.model.entity.UserEntity;
import io.github.jasonlat.authenticate.repository.IAuthenticateRepository;
import io.github.jasonlat.types.exception.AccountExistException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static io.github.jasonlat.types.enums.ResponseCode.ACCOUNT_EXIST_ERROR;


/**
 * @author li--jiaqiang 2025−06−17
 */
@Service
@AllArgsConstructor
public class AccountService implements IAccountService {

    private final IAuthenticateRepository authenticateRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void verifyAccountExist(String username) {
        UserEntity userEntity = authenticateRepository.queryUserInfoWithEmail(username);

        if (null != userEntity) {
            throw new AccountExistException(ACCOUNT_EXIST_ERROR.getCode(), ACCOUNT_EXIST_ERROR.getInfo());
        }
    }

    @Override
    public Long register(String username, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        return authenticateRepository.registerWithEmail(username, encodedPassword);
    }

    @Override
    public void saveUserPublicKey(Long userId, String userPublicX, String userPublicY) {
        authenticateRepository.saveUserPublicKey(userId, userPublicX, userPublicY);
    }
}