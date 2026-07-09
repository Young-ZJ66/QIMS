package com.young.service.impl;

import com.young.pojo.BizInspectionRecord;
import com.young.pojo.StdInspectionItem;
import com.young.pojo.enums.JudgeType;
import com.young.pojo.enums.InspectionResult;
import com.young.mapper.BizInspectionRecordMapper;
import com.young.mapper.StdInspectionItemMapper;
import com.young.service.BizInspectionRecordService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BizInspectionRecordServiceImpl implements BizInspectionRecordService {

    @Autowired
    private BizInspectionRecordMapper mapper;

    @Autowired
    private StdInspectionItemMapper itemMapper;

    @Override
    public int add(BizInspectionRecord record) {
        return mapper.insert(record);
    }

    @Override
    public int update(BizInspectionRecord record) {
        return mapper.update(record);
    }

    @Override
    public int delete(Long id) {
        return mapper.deleteById(id);
    }

    @Override
    public BizInspectionRecord getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<BizInspectionRecord> getAll() {
        return mapper.selectAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitInspectionData(BizInspectionRecord record) {
        if (record.getMeasuredText() != null && record.getMeasuredText().trim().isEmpty()) {
            record.setMeasuredText(null);
        }

        // 1. 获取对应的国家标准检测项配置
        StdInspectionItem item = itemMapper.selectById(record.getItemId());
        if (item == null) {
            throw new RuntimeException("对应的检测标准项目不存在");
        }

        // 2. 依据国家标准限值自动判定结果
        boolean isQualified = false;
        JudgeType type = JudgeType.of(item.getJudgeType());

        if (type == JudgeType.RANGE) { 
            // 1-数值范围 [min, max]
            if (record.getMeasuredValue() != null &&
                record.getMeasuredValue().compareTo(item.getMinValue()) >= 0 &&
                record.getMeasuredValue().compareTo(item.getMaxValue()) <= 0) {
                isQualified = true;
            }
        } else if (type == JudgeType.UPPER_LIMIT) { 
            // 2-上限值 (<= max)
            if (record.getMeasuredValue() != null &&
                record.getMeasuredValue().compareTo(item.getMaxValue()) <= 0) {
                isQualified = true;
            }
        } else if (type == JudgeType.LOWER_LIMIT) { 
            // 3-下限值 (>= min)
            if (record.getMeasuredValue() != null &&
                record.getMeasuredValue().compareTo(item.getMinValue()) >= 0) {
                isQualified = true;
            }
        } else if (type == JudgeType.QUALITATIVE) { 
            // 4-文本定性 (精确匹配)
            if (record.getMeasuredText() != null && 
                record.getMeasuredText().equals(item.getTextStandard())) {
                isQualified = true;
            }
        }

        // 3. 记录判定结果 (1-合格，0-不合格) 和检测时间
        record.setResult(isQualified ? InspectionResult.QUALIFIED.getCode() : InspectionResult.UNQUALIFIED.getCode());
        record.setInspectTime(LocalDateTime.now());

        // 保存检测数据
        mapper.insert(record);
    }
}
