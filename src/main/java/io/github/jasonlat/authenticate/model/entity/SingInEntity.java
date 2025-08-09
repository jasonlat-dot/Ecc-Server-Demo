package io.github.jasonlat.authenticate.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author li--jiaqiang 2024−12−23
 */
@Data
@Builder
@AllArgsConstructor
public class SingInEntity {
    private String username;
    private String password;
}