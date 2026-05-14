package com.young.pojo;

import java.time.LocalDateTime;

public class SysOperateLog {
    private Long id;
    private Long delegationId; // 关联的委托单ID
    private String operator;
    private String action;
    private String actionType; // 动作类型(primary,warning,info,success)
    private String description;
    private LocalDateTime createTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDelegationId() { return delegationId; }
    public void setDelegationId(Long delegationId) { this.delegationId = delegationId; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
