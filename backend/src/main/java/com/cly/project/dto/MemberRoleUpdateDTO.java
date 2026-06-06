package com.cly.project.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberRoleUpdateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "角色不能为空")
    private Integer role;
}
