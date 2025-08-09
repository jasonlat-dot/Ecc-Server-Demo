package io.github.jasonlat.authenticate.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class UserPublicKey {

    private String userPublicX;
    private String userPublicY;
}
