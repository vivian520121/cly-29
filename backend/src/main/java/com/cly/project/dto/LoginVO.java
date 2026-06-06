package com.cly.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String token;

    private Long userId;

    private String username;

    private String realName;

    private String avatar;

    private Integer userType;
}
