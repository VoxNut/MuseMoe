package com.javaweb.builder;

import lombok.Getter;

import java.util.Set;

@Getter
public class UserSearchBuilder {
    private String fullName;
    private Set<String> roles;
    private String status;

    private UserSearchBuilder(UserSearchBuilder.Builder builder) {
        this.fullName = builder.fullName;
        this.roles = builder.roles;
        this.status = builder.status;
    }


    public static class Builder {
        private String fullName;
        private Set<String> roles;
        private String status;

        public UserSearchBuilder.Builder setFullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public UserSearchBuilder.Builder setRole(Set<String> roles) {
            this.roles = roles;
            return this;
        }

        public UserSearchBuilder.Builder setStatus(String status) {
            this.status = status;
            return this;
        }


        public UserSearchBuilder build() {
            return new UserSearchBuilder(this);
        }


    }
}
