package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cly.project.dto.UserQueryDTO;
import com.cly.project.dto.UserSaveDTO;
import com.cly.project.entity.Department;
import com.cly.project.entity.User;
import com.cly.project.entity.UserDept;
import com.cly.project.exception.BusinessException;
import com.cly.project.mapper.CompanyMapper;
import com.cly.project.mapper.DepartmentMapper;
import com.cly.project.mapper.UserDeptMapper;
import com.cly.project.mapper.UserMapper;
import com.cly.project.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final CompanyMapper companyMapper;
    private final DepartmentMapper departmentMapper;
    private final UserDeptMapper userDeptMapper;
    private final PasswordEncoder passwordEncoder;

    public IPage<UserVO> page(UserQueryDTO query) {
        Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(User::getUsername, query.getKeyword())
                    .or()
                    .like(User::getRealName, query.getKeyword())
                    .or()
                    .like(User::getPhone, query.getKeyword());
        }
        if (query.getStatus() != null) {
            wrapper.eq(User::getStatus, query.getStatus());
        }
        if (query.getUserType() != null) {
            wrapper.eq(User::getUserType, query.getUserType());
        }
        wrapper.orderByDesc(User::getCreateTime);

        IPage<User> userPage = baseMapper.selectPage(page, wrapper);
        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> voList = userPage.getRecords().stream()
                .map(this::convertToUserVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    public UserVO getUserById(Long id) {
        User user = baseMapper.selectById(id);
        if (user == null) {
            return null;
        }
        return convertToUserVO(user);
    }

    private UserVO convertToUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);

        LambdaQueryWrapper<UserDept> userDeptWrapper = new LambdaQueryWrapper<>();
        userDeptWrapper.eq(UserDept::getUserId, user.getId());
        List<UserDept> userDepts = userDeptMapper.selectList(userDeptWrapper);

        if (!userDepts.isEmpty()) {
            List<Long> deptIds = userDepts.stream()
                    .map(UserDept::getDeptId)
                    .collect(Collectors.toList());
            List<Department> depts = departmentMapper.selectBatchIds(deptIds);
            vo.setDepts(depts);

            UserDept mainDept = userDepts.stream()
                    .filter(ud -> ud.getIsMain() != null && ud.getIsMain() == 1)
                    .findFirst()
                    .orElse(userDepts.get(0));
            vo.setMainDept(depts.stream()
                    .filter(d -> d.getId().equals(mainDept.getDeptId()))
                    .findFirst()
                    .orElse(null));
            vo.setPosition(mainDept.getPosition());
        }

        return vo;
    }

    @Transactional
    public void saveUser(UserSaveDTO dto) {
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, dto.getUsername());
        Long count = baseMapper.selectCount(usernameWrapper);
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(dto, user);
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else {
            user.setPassword(passwordEncoder.encode("123456"));
        }
        baseMapper.insert(user);

        saveUserDepts(user.getId(), dto.getDeptIds(), dto.getMainDeptId(), dto.getPosition());
    }

    @Transactional
    public void updateUser(UserSaveDTO dto) {
        User existingUser = baseMapper.selectById(dto.getId());
        if (existingUser == null) {
            throw new BusinessException("用户不存在");
        }

        if (!existingUser.getUsername().equals(dto.getUsername())) {
            LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
            usernameWrapper.eq(User::getUsername, dto.getUsername());
            Long count = baseMapper.selectCount(usernameWrapper);
            if (count > 0) {
                throw new BusinessException("用户名已存在");
            }
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, dto.getId());
        updateWrapper.set(User::getUsername, dto.getUsername());
        updateWrapper.set(User::getNickname, dto.getNickname());
        updateWrapper.set(User::getRealName, dto.getRealName());
        updateWrapper.set(User::getAvatar, dto.getAvatar());
        updateWrapper.set(User::getEmail, dto.getEmail());
        updateWrapper.set(User::getPhone, dto.getPhone());
        updateWrapper.set(User::getGender, dto.getGender());
        updateWrapper.set(User::getBirthday, dto.getBirthday());
        updateWrapper.set(User::getUserType, dto.getUserType());
        updateWrapper.set(User::getStatus, dto.getStatus());
        if (StringUtils.hasText(dto.getPassword())) {
            updateWrapper.set(User::getPassword, passwordEncoder.encode(dto.getPassword()));
        }
        baseMapper.update(null, updateWrapper);

        LambdaQueryWrapper<UserDept> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(UserDept::getUserId, dto.getId());
        userDeptMapper.delete(deleteWrapper);

        saveUserDepts(dto.getId(), dto.getDeptIds(), dto.getMainDeptId(), dto.getPosition());
    }

    private void saveUserDepts(Long userId, List<Long> deptIds, Long mainDeptId, String position) {
        if (deptIds == null || deptIds.isEmpty()) {
            return;
        }

        for (Long deptId : deptIds) {
            UserDept userDept = new UserDept();
            userDept.setUserId(userId);
            userDept.setDeptId(deptId);
            userDept.setIsMain(deptId.equals(mainDeptId) ? 1 : 0);
            userDept.setPosition(position);
            userDept.setCreateTime(LocalDateTime.now());
            userDeptMapper.insert(userDept);
        }
    }

    @Transactional
    public void removeUser(Long id) {
        LambdaQueryWrapper<UserDept> userDeptWrapper = new LambdaQueryWrapper<>();
        userDeptWrapper.eq(UserDept::getUserId, id);
        userDeptMapper.delete(userDeptWrapper);

        baseMapper.deleteById(id);
    }

    public void resetPassword(Long id, String newPassword) {
        User user = baseMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        baseMapper.updateById(user);
    }

    public List<User> listAll() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(User::getRealName);
        return baseMapper.selectList(wrapper);
    }

    public List<User> getUsersByDeptId(Long deptId) {
        LambdaQueryWrapper<UserDept> userDeptWrapper = new LambdaQueryWrapper<>();
        userDeptWrapper.eq(UserDept::getDeptId, deptId);
        List<UserDept> userDepts = userDeptMapper.selectList(userDeptWrapper);
        if (userDepts.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> userIds = userDepts.stream()
                .map(UserDept::getUserId)
                .collect(Collectors.toList());
        return baseMapper.selectBatchIds(userIds);
    }
}
