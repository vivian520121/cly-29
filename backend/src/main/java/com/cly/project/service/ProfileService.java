package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cly.project.entity.User;
import com.cly.project.exception.BusinessException;
import com.cly.project.mapper.UserMapper;
import com.cly.project.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public User getProfile() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Transactional
    public void updateProfile(User user) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            throw new BusinessException("用户不存在");
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId);

        if (StringUtils.hasText(user.getNickname())) {
            updateWrapper.set(User::getNickname, user.getNickname());
        }
        if (StringUtils.hasText(user.getAvatar())) {
            updateWrapper.set(User::getAvatar, user.getAvatar());
        }
        if (StringUtils.hasText(user.getEmail())) {
            updateWrapper.set(User::getEmail, user.getEmail());
        }
        if (StringUtils.hasText(user.getPhone())) {
            updateWrapper.set(User::getPhone, user.getPhone());
        }
        if (user.getGender() != null) {
            updateWrapper.set(User::getGender, user.getGender());
        }
        if (user.getBirthday() != null) {
            updateWrapper.set(User::getBirthday, user.getBirthday());
        }

        userMapper.update(null, updateWrapper);
    }

    @Transactional
    public void updatePassword(String oldPassword, String newPassword) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        if (!StringUtils.hasText(newPassword) || newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                .set(User::getPassword, passwordEncoder.encode(newPassword));
        userMapper.update(null, updateWrapper);
    }
}
