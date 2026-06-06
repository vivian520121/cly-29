package com.cly.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserSaveDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    private String nickname;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private String avatar;

    private String email;

    private String phone;

    private Integer gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private Integer userType;

    private Integer status;

    private List<Long> deptIds;

    private Long mainDeptId;

    private String position;
}
