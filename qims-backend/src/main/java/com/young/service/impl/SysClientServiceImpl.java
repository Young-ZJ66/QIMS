package com.young.service.impl;

import com.young.pojo.SysClient;
import com.young.mapper.SysClientMapper;
import com.young.service.SysClientService;
import com.young.utils.PasswordUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class SysClientServiceImpl implements SysClientService {

    @Autowired
    private SysClientMapper mapper;

    @Override
    public int add(SysClient record) {
        // 新增客户时使用 BCrypt 加密密码
        if (record.getLoginPassword() != null && !record.getLoginPassword().isEmpty()) {
            record.setLoginPassword(PasswordUtils.hash(record.getLoginPassword()));
        }
        // 默认状态为正常
        if (record.getStatus() == null) {
            record.setStatus(1);
        }
        return mapper.insert(record);
    }

    @Override
    public int update(SysClient record) {
        // 如果传了密码且不是 BCrypt 格式，需要加密
        if (record.getLoginPassword() != null && !record.getLoginPassword().isEmpty()) {
            if (!record.getLoginPassword().startsWith("$2")) {
                record.setLoginPassword(PasswordUtils.hash(record.getLoginPassword()));
            }
        } else {
            record.setLoginPassword(null); // 为空则不更新密码
        }
        return mapper.update(record);
    }

    @Override
    public int delete(Long id) {
        return mapper.deleteById(id);
    }

    @Override
    public SysClient getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<SysClient> getAll() {
        return mapper.selectAll();
    }
}
