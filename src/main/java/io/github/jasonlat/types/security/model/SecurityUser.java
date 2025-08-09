package io.github.jasonlat.types.security.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author li--jiaqiang 2025−02−27
 */
@Data
@NoArgsConstructor
public class SecurityUser implements UserDetails {

    private String username;

    private String password;

    private List<String> permissions;

    @JSONField(serialize = false)
    @JsonIgnore
    private Collection<? extends GrantedAuthority> authorities;

    public SecurityUser(String username, String password, List<String> permissions) {
        this.username = username;
        this.password = password;
        this.permissions = permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.authorities != null) {
            return this.authorities;
        }
        Collection<GrantedAuthority> authorities = new LinkedList<>();
        for (String authority : this.permissions) {
            GrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(authority);
            authorities.add(simpleGrantedAuthority);
        }
        this.authorities = authorities;
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}