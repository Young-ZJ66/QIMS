package com.young.service;

import com.young.pojo.SysClient;
import java.util.List;

public interface SysClientService {
    int add(SysClient record);
    int update(SysClient record);
    int delete(Long id);
    SysClient getById(Long id);
    List<SysClient> getAll();
}
