package com.cly.project.vo;

import com.cly.project.entity.Department;
import com.cly.project.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserVO extends User {

    private List<Department> depts;

    private Department mainDept;

    private String position;
}
