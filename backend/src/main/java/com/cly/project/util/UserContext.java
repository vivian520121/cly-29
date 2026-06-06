package com.cly.project.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserContext {

    private static final ThreadLocal<CurrentUser> USER_HOLDER = new ThreadLocal<>();

    public static void setCurrentUser(Long userId, String username, Integer userType) {
        USER_HOLDER.set(new CurrentUser(userId, username, userType));
    }

    public static CurrentUser getCurrentUser() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        CurrentUser user = USER_HOLDER.get();
        return user != null ? user.getUserId() : null;
    }

    public static String getUsername() {
        CurrentUser user = USER_HOLDER.get();
        return user != null ? user.getUsername() : null;
    }

    public static Integer getUserType() {
        CurrentUser user = USER_HOLDER.get();
        return user != null ? user.getUserType() : null;
    }

    public static boolean isAdmin() {
        Integer userType = getUserType();
        return userType != null && userType == 1;
    }

    public static void clear() {
        USER_HOLDER.remove();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentUser {
        private Long userId;
        private String username;
        private Integer userType;
    }
}
