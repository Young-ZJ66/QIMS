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
}
