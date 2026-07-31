package com.young.service.impl;

import com.young.pojo.SysUser;
import com.young.mapper.SysUserMapper;
import com.young.service.SysUserService;
import com.young.utils.PasswordUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper mapper;

    @Override
    public int add(SysUser record) {
        // 新增用户时使用 BCrypt 加密密码
        if (record.getPassword() != null && !record.getPassword().isEmpty()) {
            record.setPassword(PasswordUtils.hash(record.getPassword()));
        }
        return mapper.insert(record);
    }

    @Override
    public int update(SysUser record) {
        if (record.getPassword() != null && !record.getPassword().isEmpty()) {
            // 如果不是 BCrypt 格式，说明是明文，需要加密
            if (!record.getPassword().startsWith("$2")) {
                record.setPassword(PasswordUtils.hash(record.getPassword()));
            }
        } else {
            record.setPassword(null); // 为空则不更新密码
        }
        return mapper.update(record);
    }

    @Override
    public int delete(Long id) {
        return mapper.deleteById(id);
    }

    @Override
    public SysUser getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<SysUser> getAll() {
        return mapper.selectAll();
    }
}
