package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cly.project.common.PageQuery;
import com.cly.project.entity.Company;
import com.cly.project.entity.Department;
import com.cly.project.exception.BusinessException;
import com.cly.project.mapper.CompanyMapper;
import com.cly.project.mapper.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService extends ServiceImpl<CompanyMapper, Company> {

    private final DepartmentMapper departmentMapper;

    public IPage<Company> pageList(PageQuery query, String keyword) {
        Page<Company> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Company> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Company::getCompanyName, keyword)
                    .or()
                    .like(Company::getCompanyCode, keyword)
                    .or()
                    .like(Company::getLegalPerson, keyword);
        }
        wrapper.orderByAsc(Company::getSortOrder);
        return baseMapper.selectPage(page, wrapper);
    }

    public Company getCompanyById(Long id) {
        return baseMapper.selectById(id);
    }

    public void saveCompany(Company company) {
        baseMapper.insert(company);
    }

    public void updateCompany(Company company) {
        baseMapper.updateById(company);
    }

    public void removeCompany(Long id) {
        LambdaQueryWrapper<Department> deptWrapper = new LambdaQueryWrapper<>();
        deptWrapper.eq(Department::getCompanyId, id);
        Long deptCount = departmentMapper.selectCount(deptWrapper);
        if (deptCount > 0) {
            throw new BusinessException("该公司下存在部门，无法删除");
        }
        baseMapper.deleteById(id);
    }

    public List<Company> listAll() {
        LambdaQueryWrapper<Company> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Company::getSortOrder);
        return baseMapper.selectList(wrapper);
    }
}
