package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cly.project.dto.DeptSaveDTO;
import com.cly.project.entity.Department;
import com.cly.project.entity.User;
import com.cly.project.entity.UserDept;
import com.cly.project.exception.BusinessException;
import com.cly.project.mapper.DepartmentMapper;
import com.cly.project.mapper.UserDeptMapper;
import com.cly.project.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService extends ServiceImpl<DepartmentMapper, Department> {

    private final UserDeptMapper userDeptMapper;
    private final UserMapper userMapper;

    public List<Department> getTree() {
        List<Department> allDepts = baseMapper.selectList(new LambdaQueryWrapper<Department>()
                .orderByAsc(Department::getSortOrder));
        return buildTree(allDepts, 0L);
    }

    private List<Department> buildTree(List<Department> depts, Long parentId) {
        List<Department> result = new ArrayList<>();
        for (Department dept : depts) {
            Long currentParentId = dept.getParentId() == null ? 0L : dept.getParentId();
            if (currentParentId.equals(parentId)) {
                dept.setChildren(buildTree(depts, dept.getId()));
                result.add(dept);
            }
        }
        return result;
    }

    public List<Department> getByParentId(Long parentId) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        if (parentId == null) {
            wrapper.isNull(Department::getParentId).or().eq(Department::getParentId, 0);
        } else {
            wrapper.eq(Department::getParentId, parentId);
        }
        wrapper.orderByAsc(Department::getSortOrder);
        return baseMapper.selectList(wrapper);
    }

    public List<Department> listAll() {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Department::getSortOrder);
        return baseMapper.selectList(wrapper);
    }

    public Department getDeptById(Long id) {
        return baseMapper.selectById(id);
    }

    public void saveDept(DeptSaveDTO dto) {
        Department dept = new Department();
        BeanUtils.copyProperties(dto, dept);
        dept.setTreePath(generateTreePath(dto.getParentId()));
        baseMapper.insert(dept);
    }

    public void updateDept(DeptSaveDTO dto) {
        Department dept = new Department();
        BeanUtils.copyProperties(dto, dept);
        if (dto.getParentId() != null) {
            dept.setTreePath(generateTreePath(dto.getParentId()));
        }
        baseMapper.updateById(dept);
    }

    private String generateTreePath(Long parentId) {
        if (parentId == null || parentId.equals(0L)) {
            return "";
        }
        Department parent = baseMapper.selectById(parentId);
        if (parent == null) {
            return "";
        }
        String parentPath = parent.getTreePath();
        if (StringUtils.hasText(parentPath)) {
            return parentPath + "," + parentId;
        } else {
            return String.valueOf(parentId);
        }
    }

    public void removeDept(Long id) {
        LambdaQueryWrapper<Department> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(Department::getParentId, id);
        Long childCount = baseMapper.selectCount(childWrapper);
        if (childCount > 0) {
            throw new BusinessException("该部门下存在子部门，无法删除");
        }

        LambdaQueryWrapper<UserDept> userDeptWrapper = new LambdaQueryWrapper<>();
        userDeptWrapper.eq(UserDept::getDeptId, id);
        Long userCount = userDeptMapper.selectCount(userDeptWrapper);
        if (userCount > 0) {
            throw new BusinessException("该部门下存在用户，无法删除");
        }

        baseMapper.deleteById(id);
    }

    public List<User> getDeptUsers(Long deptId) {
        LambdaQueryWrapper<UserDept> userDeptWrapper = new LambdaQueryWrapper<>();
        userDeptWrapper.eq(UserDept::getDeptId, deptId);
        List<UserDept> userDepts = userDeptMapper.selectList(userDeptWrapper);
        if (userDepts.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> userIds = userDepts.stream()
                .map(UserDept::getUserId)
                .collect(Collectors.toList());
        return userMapper.selectBatchIds(userIds);
    }
}
