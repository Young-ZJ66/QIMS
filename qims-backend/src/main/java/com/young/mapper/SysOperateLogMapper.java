package com.young.mapper;

import com.young.pojo.SysOperateLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SysOperateLogMapper {
    int insert(SysOperateLog log);
    List<SysOperateLog> selectRecentLogs(@Param("limit") int limit, @Param("delegationIds") List<Long> delegationIds);
}
