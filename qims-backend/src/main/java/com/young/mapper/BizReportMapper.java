package com.young.mapper;

import com.young.pojo.BizReport;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BizReportMapper {
    int insert(BizReport record);
    int update(BizReport record);
    int deleteById(Long id);
    BizReport selectById(Long id);
    List<BizReport> selectAll();
    /** 按委托单ID批量查询报告 */
    List<BizReport> selectByDelegationIds(@org.apache.ibatis.annotations.Param("delegationIds") List<Long> delegationIds);
    /** 统计当月签发报告数 */
    int countThisMonth();
    /** 统计合格报告数 */
    int countQualified();
    /** 统计报告总数 */
    int countTotal();
}
