package com.gatcha.api.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "authTokens")
public class AuthToken {
    @Id
    private String id;
    private String token;
    private String username;
    private Date expirationDate;

    public boolean isExpired() {
        return expirationDate.before(new Date());
    }

    public void updateExpiration(long expirationTimeInMillis) {
        this.expirationDate = new Date(System.currentTimeMillis() + expirationTimeInMillis);
    }
}