package com.young.service;

import com.young.pojo.BizDelegation;
import java.util.List;

public interface BizDelegationService {
    int add(BizDelegation record);
    int update(BizDelegation record);
    int delete(Long id);
    BizDelegation getById(Long id);
    List<BizDelegation> getAll();
    List<BizDelegation> getByClientId(Long clientId);

    /**
     * 客户提交委托单，系统自动生成单号
     */
    String submitDelegation(BizDelegation delegation);

    /**
     * 管理员收样并分配给检测员，自动生成盲样任务
     */
    String receiveSampleAndAssign(Long delegationId, Long inspectorId, Long receiverId);
}
