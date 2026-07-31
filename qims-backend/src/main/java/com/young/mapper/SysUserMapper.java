package com.young.mapper;

import com.young.pojo.SysUser;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SysUserMapper {
    int insert(SysUser record);
    int update(SysUser record);
    int deleteById(Long id);
    SysUser selectById(Long id);
    List<SysUser> selectAll();
    /** 按用户名精确查询 */
    SysUser selectByUsername(@org.apache.ibatis.annotations.Param("username") String username);
}
