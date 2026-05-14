package com.young.service.impl;

import com.young.pojo.SysClient;
import com.young.mapper.SysClientMapper;
import com.young.service.SysClientService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class SysClientServiceImpl implements SysClientService {

    @Autowired
    private SysClientMapper mapper;

    @Override
    public int add(SysClient record) {
        return mapper.insert(record);
    }

    @Override
    public int update(SysClient record) {
        return mapper.update(record);
    }

    @Override
    public int delete(Long id) {
        return mapper.deleteById(id);
    }

    @Override
    public SysClient getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<SysClient> getAll() {
        return mapper.selectAll();
    }
}
