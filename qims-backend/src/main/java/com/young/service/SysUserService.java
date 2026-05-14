package com.young.service;

import com.young.pojo.SysUser;
import java.util.List;

public interface SysUserService {
    int add(SysUser record);
    int update(SysUser record);
    int delete(Long id);
    SysUser getById(Long id);
    List<SysUser> getAll();
}
