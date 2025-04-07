package com.igrowker.feature.parkify.features.auth.dto.response;

import com.igrowker.feature.parkify.features.auth.entities.Role;
import lombok.Getter;
import lombok.Setter;
import java.util.Objects;

@Setter
@Getter
public class AuthResponse {
    String token;
    Role role;

    public AuthResponse(String token, Role role) {
        this.token = token;
        this.role = role;
    }
    private AuthResponse(Builder builder) {
        this.token = builder.token;
        this.role = builder.role;
    }

    public static class Builder {
        private String token;
        private Role role;

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public AuthResponse build() {
            return new AuthResponse(this);
        }
    }
    @Override
    public String toString() {
        return "AuthResponse{" +
                "token='" + token + '\'' +
                ", role=" + role +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthResponse that = (AuthResponse) o;
        return Objects.equals(token, that.token) && role == that.role;
    }
    public int hashCode() {
        return Objects.hash(token, role);
    }
}
