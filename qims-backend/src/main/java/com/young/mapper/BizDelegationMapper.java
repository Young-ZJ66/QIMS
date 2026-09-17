package com.young.mapper;

import com.young.pojo.BizDelegation;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BizDelegationMapper {
    int insert(BizDelegation record);
    int update(BizDelegation record);
    int deleteById(Long id);
    BizDelegation selectById(Long id);
    List<BizDelegation> selectAll();
    List<BizDelegation> selectByClientId(Long clientId);
    List<BizDelegation> selectByStatus(@org.apache.ibatis.annotations.Param("status") Integer status);
    /** 按提交日期统计委托数量（近7天） */
    List<java.util.Map<String, Object>> countByRecentDays(@org.apache.ibatis.annotations.Param("days") int days);
    /** 按客户ID列表批量查询 */
    List<BizDelegation> selectByClientIds(@org.apache.ibatis.annotations.Param("clientIds") List<Long> clientIds);
    /** 统计委托总数 */
    int countTotal();
}
