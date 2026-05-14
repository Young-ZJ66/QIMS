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
}
