package com.young.service.impl;

import com.young.pojo.BizSampleTask;
import com.young.mapper.BizSampleTaskMapper;
import com.young.service.BizSampleTaskService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class BizSampleTaskServiceImpl implements BizSampleTaskService {

    @Autowired
    private BizSampleTaskMapper mapper;

    @Override
    public int add(BizSampleTask record) {
        return mapper.insert(record);
    }

    @Override
    public int update(BizSampleTask record) {
        return mapper.update(record);
    }

    @Override
    public int delete(Long id) {
        return mapper.deleteById(id);
    }

    @Override
    public BizSampleTask getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<BizSampleTask> getAll() {
        return mapper.selectAll();
    }
}
