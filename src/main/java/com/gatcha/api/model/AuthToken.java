package com.gatcha.api.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@Document(collection = "auth_tokens")
public class AuthToken {
    @Id
    private String id;
    private String userId;
    private String username;
    private String token;
    private Date expirationDate;
    private Date createdAt = new Date();

    public boolean isExpired() {
        return expirationDate != null && expirationDate.before(new Date());
    }
}