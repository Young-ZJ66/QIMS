package com.young.mapper;

import com.young.pojo.SysClient;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SysClientMapper {
    int insert(SysClient record);
    int update(SysClient record);
    int deleteById(Long id);
    SysClient selectById(Long id);
    List<SysClient> selectAll();
    /** 按登录账号精确查询 */
    SysClient selectByLoginAccount(@org.apache.ibatis.annotations.Param("loginAccount") String loginAccount);
}
