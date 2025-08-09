package io.github.jasonlat.trigger.model;

import lombok.Data;

/**
 * @author li--jiaqiang 2024−12−23
 */
@Data
public class SignInRequest {

    private String username;

    private String password;
}