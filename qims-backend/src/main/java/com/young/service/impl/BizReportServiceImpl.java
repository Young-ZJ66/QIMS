package com.young.service.impl;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import com.young.pojo.BizReport;
import com.young.pojo.BizDelegation;
import com.young.pojo.BizSampleTask;
import com.young.pojo.BizInspectionRecord;
import com.young.pojo.StdInspectionItem;
import com.young.pojo.SysUser;
import com.young.pojo.SysClient;
import com.young.pojo.StdStandard;
import com.young.mapper.BizReportMapper;
import com.young.mapper.BizDelegationMapper;
import com.young.mapper.BizSampleTaskMapper;
import com.young.mapper.BizInspectionRecordMapper;
import com.young.mapper.StdInspectionItemMapper;
import com.young.mapper.SysUserMapper;
import com.young.mapper.SysClientMapper;
import com.young.mapper.StdStandardMapper;
import com.young.mapper.SysOperateLogMapper;
import com.young.pojo.SysOperateLog;
import com.young.service.BizReportService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCellEvent;
import org.springframework.core.io.ClassPathResource;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

@Service
public class BizReportServiceImpl implements BizReportService {

    @Autowired
    private BizReportMapper mapper;

    @Autowired
    private BizDelegationMapper delegationMapper;

    @Autowired
    private BizSampleTaskMapper taskMapper;

    @Autowired
    private BizInspectionRecordMapper recordMapper;

    @Autowired
    private StdInspectionItemMapper itemMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysClientMapper clientMapper;

    @Autowired
    private StdStandardMapper standardMapper;

    @Autowired
    private SysOperateLogMapper logMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(BizReport record) {
        // 校验：确保该委托单的所有检测项目均已录入
        BizDelegation delegation = delegationMapper.selectById(record.getDelegationId());
        if (delegation == null) {
            throw new RuntimeException("关联的委托单不存在");
        }

        // 获取该国标下规定的所有检测项目数量
        List<StdInspectionItem> allItems = itemMapper.selectAll().stream()
                .filter(item -> delegation.getStandardId().equals(item.getStandardId()))
                .collect(Collectors.toList());

        // 获取该委托单对应的所有盲样任务
        List<BizSampleTask> tasks = taskMapper.selectAll().stream()
                .filter(t -> record.getDelegationId().equals(t.getDelegationId()))
                .collect(Collectors.toList());

        if (tasks.isEmpty()) {
            throw new RuntimeException("该委托单尚未生成任何检测任务，无法签发报告");
        }

        // 获取所有已录入的检测记录
        List<Long> taskIds = tasks.stream().map(BizSampleTask::getId).collect(Collectors.toList());
        List<BizInspectionRecord> inspectionRecords = recordMapper.selectAll().stream()
                .filter(r -> taskIds.contains(r.getTaskId()))
                .collect(Collectors.toList());

        Set<Long> requiredItemIds = allItems.stream().map(StdInspectionItem::getId).collect(Collectors.toSet());
        Map<Long, Set<Long>> taskToItemIds = new HashMap<>();
        for (BizInspectionRecord r : inspectionRecords) {
            taskToItemIds.computeIfAbsent(r.getTaskId(), k -> new HashSet<>()).add(r.getItemId());
        }
        for (Long taskId : taskIds) {
            Set<Long> recordedItemIds = taskToItemIds.getOrDefault(taskId, Collections.emptySet());
            if (!recordedItemIds.containsAll(requiredItemIds)) {
                throw new RuntimeException("该委托单存在未完成检测的盲样任务，拒绝签发！");
            }
        }

        // 校验总结论
        boolean hasFail = inspectionRecords.stream().anyMatch(r -> r.getResult() != null && r.getResult() == 0);
        // 若存在不合格项，则强制将总结论置为不合格
        if (hasFail) {
            record.setFinalConclusion(0);
        }

        if (record.getIssueTime() == null) {
            record.setIssueTime(LocalDateTime.now());
        }

        // 自动生成 PDF 报告文件
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/reports/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String pdfFileName = "Report_" + record.getReportNo() + ".pdf";
            String pdfPath = uploadDir + pdfFileName;

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
            document.open();

            // 准备数据
            String reviewerName = "/";
            if (record.getReviewerId() != null) {
                SysUser user = userMapper.selectById(record.getReviewerId());
                if (user != null && user.getRealName() != null) {
                    reviewerName = user.getRealName();
                }
            }

            // 获取委托单位（送检客户）信息
            String clientName = "/";
            if (delegation.getClientId() != null) {
                SysClient client = clientMapper.selectById(delegation.getClientId());
                if (client != null && client.getCompanyName() != null) {
                    clientName = client.getCompanyName();
                }
            }

            // 获取检验依据标准信息
            String standardName = "/";
            if (delegation.getStandardId() != null) {
                StdStandard standard = standardMapper.selectById(delegation.getStandardId());
                if (standard != null && standard.getStandardCode() != null) {
                    standardName = standard.getStandardCode() + " " + standard.getStandardName();
                }
            }

            // 获取主检人（质检员）信息，取第一个任务的检测员
            String inspectorName = "/";
            if (!tasks.isEmpty() && tasks.get(0).getInspectorId() != null) {
                SysUser inspectorUser = userMapper.selectById(tasks.get(0).getInspectorId());
                if (inspectorUser != null && inspectorUser.getRealName() != null) {
                    inspectorName = inspectorUser.getRealName();
                }
            }
            
            // 设置中文字体 (STSong-Light)
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UTF16-H", BaseFont.NOT_EMBEDDED);
            Font fontTitle = new Font(bfChinese, 24, Font.BOLD);
            Font fontSubTitle = new Font(bfChinese, 16, Font.BOLD);
            Font fontNormal = new Font(bfChinese, 12, Font.NORMAL);
            Font fontNormalBold = new Font(bfChinese, 12, Font.BOLD);

            Paragraph title = new Paragraph("产品质量检验报告", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 基本信息表
            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1.5f, 3.5f, 1.5f, 3.5f});
            infoTable.setSpacingAfter(20);
            
            infoTable.addCell(createCell(new Paragraph("报告编号", fontNormalBold), true));
            infoTable.addCell(createCell(new Paragraph(record.getReportNo(), fontNormal), false));
            infoTable.addCell(createCell(new Paragraph("送检日期", fontNormalBold), true));
            infoTable.addCell(createCell(new Paragraph(delegation.getSubmitTime() != null ? delegation.getSubmitTime().toLocalDate().toString() : "/", fontNormal), false));
            
            infoTable.addCell(createCell(new Paragraph("委托单位", fontNormalBold), true));
            infoTable.addCell(createCell(new Paragraph(clientName, fontNormal), false));
            infoTable.addCell(createCell(new Paragraph("委托单号", fontNormalBold), true));
            infoTable.addCell(createCell(new Paragraph(delegation.getDelegationNo(), fontNormal), false));
            
            infoTable.addCell(createCell(new Paragraph("样品名称", fontNormalBold), true));
            infoTable.addCell(createCell(new Paragraph(delegation.getSampleName(), fontNormal), false));
            infoTable.addCell(createCell(new Paragraph("规格型号", fontNormalBold), true));
            infoTable.addCell(createCell(new Paragraph(delegation.getSampleSpecs() != null ? delegation.getSampleSpecs() : "/", fontNormal), false));
            
            // 检验依据占整行
            infoTable.addCell(createCell(new Paragraph("检验依据", fontNormalBold), true));
            PdfPCell stdCell = createCell(new Paragraph(standardName, fontNormal), false);
            stdCell.setColspan(3);
            infoTable.addCell(stdCell);
            
            // 主检人与签发人
            infoTable.addCell(createCell(new Paragraph("主 检 人", fontNormalBold), true));
            infoTable.addCell(createCell(new Paragraph(inspectorName, fontNormal), false));
            infoTable.addCell(createCell(new Paragraph("签 发 人", fontNormalBold), true));
            infoTable.addCell(createCell(new Paragraph(reviewerName, fontNormal), false));
            
            document.add(infoTable);

            // 检测项目明细表
            Paragraph detailTitle = new Paragraph("检测项目明细", fontSubTitle);
            detailTitle.setSpacingAfter(10);
            document.add(detailTitle);

            PdfPTable itemTable = new PdfPTable(5);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{2.5f, 3f, 1.5f, 2.5f, 3.5f});
            itemTable.setSpacingAfter(20);
            
            // 表头
            itemTable.addCell(createCell(new Paragraph("盲样编号", fontNormalBold), true));
            itemTable.addCell(createCell(new Paragraph("检验项目", fontNormalBold), true));
            itemTable.addCell(createCell(new Paragraph("单位", fontNormalBold), true));
            itemTable.addCell(createCell(new Paragraph("实测结果", fontNormalBold), true));
            itemTable.addCell(createCell(new Paragraph("单项判定", fontNormalBold), true));

            Map<Long, String> taskCodeMap = tasks.stream()
                    .collect(Collectors.toMap(BizSampleTask::getId, BizSampleTask::getBlindSampleCode));

            Image checkedImg = getCheckboxImage(true);
            Image uncheckedImg = getCheckboxImage(false);

            for (BizInspectionRecord rec : inspectionRecords) {
                StdInspectionItem item = allItems.stream().filter(i -> i.getId().equals(rec.getItemId())).findFirst().orElse(null);
                String itemName = item != null ? item.getItemName() : "未知项目";
                String unit = (item != null && item.getUnit() != null) ? item.getUnit() : "-";
                String measuredText = rec.getMeasuredText();
                String resultVal;
                if (measuredText != null && !measuredText.trim().isEmpty()) {
                    resultVal = measuredText;
                } else if (rec.getMeasuredValue() != null) {
                    resultVal = rec.getMeasuredValue().toString();
                } else {
                    resultVal = "-";
                }
                
                Paragraph judgePara = new Paragraph();
                if (rec.getResult() != null && rec.getResult() == 1) {
                    judgePara.add(new Chunk(checkedImg, 0, 0, true));
                    judgePara.add(new Chunk(" 合格   ", fontNormal));
                    judgePara.add(new Chunk(uncheckedImg, 0, 0, true));
                    judgePara.add(new Chunk(" 不合格", fontNormal));
                } else {
                    judgePara.add(new Chunk(uncheckedImg, 0, 0, true));
                    judgePara.add(new Chunk(" 合格   ", fontNormal));
                    judgePara.add(new Chunk(checkedImg, 0, 0, true));
                    judgePara.add(new Chunk(" 不合格", fontNormal));
                }

                String blindCode = taskCodeMap.getOrDefault(rec.getTaskId(), "-");

                itemTable.addCell(createCenterCell(new Paragraph(blindCode, fontNormal)));
                itemTable.addCell(createCenterCell(new Paragraph(itemName, fontNormal)));
                itemTable.addCell(createCenterCell(new Paragraph(unit, fontNormal)));
                itemTable.addCell(createCenterCell(new Paragraph(resultVal, fontNormal)));
                itemTable.addCell(createCell(judgePara, false));
            }
            document.add(itemTable);

            // 综合结论表格
            long totalItemsCount = inspectionRecords.size();
            long failItemsCount = inspectionRecords.stream().filter(r -> r.getResult() != null && r.getResult() == 0).count();
            
            Paragraph finalJudgePara = new Paragraph();
            if (record.getFinalConclusion() == 1) {
                finalJudgePara.add(new Chunk(checkedImg, 0, 0, true));
                finalJudgePara.add(new Chunk(" 合格   ", fontNormal));
                finalJudgePara.add(new Chunk(uncheckedImg, 0, 0, true));
                finalJudgePara.add(new Chunk(" 不合格", fontNormal));
            } else {
                finalJudgePara.add(new Chunk(uncheckedImg, 0, 0, true));
                finalJudgePara.add(new Chunk(" 合格   ", fontNormal));
                finalJudgePara.add(new Chunk(checkedImg, 0, 0, true));
                finalJudgePara.add(new Chunk(" 不合格", fontNormal));
            }

            PdfPTable conclusionTable = new PdfPTable(2);
            conclusionTable.setWidthPercentage(100);
            conclusionTable.setWidths(new float[]{3f, 7f});
            conclusionTable.setSpacingAfter(20);

            conclusionTable.addCell(createCell(new Paragraph("检测项总数", fontNormalBold), true));
            conclusionTable.addCell(createCell(new Paragraph(String.valueOf(totalItemsCount), fontNormal), false));
            
            conclusionTable.addCell(createCell(new Paragraph("不合格项数", fontNormalBold), true));
            conclusionTable.addCell(createCell(new Paragraph(String.valueOf(failItemsCount), fontNormal), false));

            conclusionTable.addCell(createCell(new Paragraph("综合结论", fontNormalBold), true));
            conclusionTable.addCell(createCell(finalJudgePara, false));

            document.add(conclusionTable);
            
            // 插入电子签章与落款
            try {
                PdfPTable signTable = new PdfPTable(1);
                signTable.setWidthPercentage(30);
                signTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
                signTable.setSpacingBefore(30f);
                
                // 将检验机构与日期放入同一个段落中
                Paragraph textPara = new Paragraph();
                textPara.setLeading(30f);
                textPara.add(new Chunk("检验机构 (盖章):\n", fontNormalBold));
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy 年 MM 月 dd 日");
                textPara.add(new Chunk("日 期: " + record.getIssueTime().format(dtf), fontNormal));
                
                PdfPCell textCell = new PdfPCell(textPara);
                textCell.setBorder(PdfPCell.NO_BORDER);
                textCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                
                ClassPathResource imgResource = new ClassPathResource("images/Signature.png");
                if (imgResource.exists()) {
                    final Image signature = Image.getInstance(imgResource.getURL());
                    signature.scaleToFit(120, 120);
                    // 使用 PdfPCellEvent 确保印章绝对居中绘制
                    textCell.setCellEvent(new PdfPCellEvent() {
                        @Override
                        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
                            try {
                                PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
                                float x = position.getLeft() + (position.getWidth() - signature.getScaledWidth()) / 2;
                                float y = position.getBottom() + (position.getHeight() - signature.getScaledHeight()) / 2;
                                signature.setAbsolutePosition(x, y);
                                canvas.addImage(signature);
                            } catch (Exception e) {}
                        }
                    });
                }
                
                signTable.addCell(textCell);
                document.add(signTable);
            } catch (Exception e) {
                System.out.println("加载电子签章失败: " + e.getMessage());
            }

            Paragraph disclaimer = new Paragraph("声明：本报告为演示文件，不具备法律效力。", new Font(bfChinese, 10, Font.NORMAL));
            disclaimer.setAlignment(Element.ALIGN_LEFT);
            disclaimer.setSpacingBefore(40);
            document.add(disclaimer);

            document.close();

            // 保存生成的 PDF 相对路径到数据库
            record.setReportFileUrl("/uploads/reports/" + pdfFileName);

        } catch (Exception e) {
            e.printStackTrace();
            // PDF生成失败时不阻塞流程，实际项目中可以根据需要抛出异常
        }

        // 保存报告记录
        mapper.insert(record);

        // 更新委托单状态为 3-已出报告
        delegation.setStatus(3);
        delegationMapper.update(delegation);

        String reviewerName = "/";
        if (record.getReviewerId() != null) {
            SysUser user = userMapper.selectById(record.getReviewerId());
            if (user != null && user.getRealName() != null) {
                reviewerName = user.getRealName();
            }
        }

        // 记录系统操作日志
        SysOperateLog operateLog = new SysOperateLog();
        operateLog.setDelegationId(record.getDelegationId());
        operateLog.setOperator(reviewerName);
        operateLog.setAction("报告签发");
        operateLog.setActionType("success");
        operateLog.setDescription("签发了报告编号 " + record.getReportNo());
        operateLog.setCreateTime(LocalDateTime.now());
        logMapper.insert(operateLog);

        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(BizReport record) {
        // 更新数据库
        int res = mapper.update(record);
        
        // 重新生成 PDF，覆盖旧文件
        try {
            BizReport existReport = mapper.selectById(record.getId());
            if (existReport == null) return res;
            
            // 补全生成 PDF 需要的数据
            BizDelegation delegation = delegationMapper.selectById(existReport.getDelegationId());
            List<StdInspectionItem> allItems = itemMapper.selectAll().stream()
                    .filter(item -> delegation.getStandardId().equals(item.getStandardId()))
                    .collect(Collectors.toList());
            List<BizSampleTask> tasks = taskMapper.selectAll().stream()
                    .filter(t -> existReport.getDelegationId().equals(t.getDelegationId()))
                    .collect(Collectors.toList());
            List<Long> taskIds = tasks.stream().map(BizSampleTask::getId).collect(Collectors.toList());
            List<BizInspectionRecord> inspectionRecords = recordMapper.selectAll().stream()
                    .filter(r -> taskIds.contains(r.getTaskId()))
                    .collect(Collectors.toList());

            // 提取 PDF 生成核心逻辑（与 add() 保持一致，直接写在这个方法里或提成 private 方法，这里直接重新渲染）
            String uploadDir = System.getProperty("user.dir") + "/uploads/reports/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String pdfFileName = "Report_" + existReport.getReportNo() + ".pdf";
            String pdfPath = uploadDir + pdfFileName;

            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
            document.open();

            // 数据准备
            String reviewerName = "/";
            if (existReport.getReviewerId() != null) {
                SysUser user = userMapper.selectById(existReport.getReviewerId());
                if (user != null && user.getRealName() != null) reviewerName = user.getRealName();
            }
            String clientName = "/";
            if (delegation.getClientId() != null) {
                SysClient client = clientMapper.selectById(delegation.getClientId());
                if (client != null && client.getCompanyName() != null) clientName = client.getCompanyName();
            }
            String standardName = "/";
            if (delegation.getStandardId() != null) {
                StdStandard standard = standardMapper.selectById(delegation.getStandardId());
                if (standard != null && standard.getStandardCode() != null) standardName = standard.getStandardCode() + " " + standard.getStandardName();
            }
            String inspectorName = "/";
            if (!tasks.isEmpty() && tasks.get(0).getInspectorId() != null) {
                SysUser inspectorUser = userMapper.selectById(tasks.get(0).getInspectorId());
                if (inspectorUser != null && inspectorUser.getRealName() != null) inspectorName = inspectorUser.getRealName();
            }

            // 字体准备
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UTF16-H", BaseFont.NOT_EMBEDDED);
            Font fontTitle = new Font(bfChinese, 24, Font.BOLD);
            Font fontSubTitle = new Font(bfChinese, 16, Font.BOLD);
            Font fontNormal = new Font(bfChinese, 12, Font.NORMAL);
            Font fontNormalBold = new Font(bfChinese, 12, Font.BOLD);

            Paragraph title = new Paragraph("产品质量检验报告", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1.5f, 3.5f, 1.5f, 3.5f});
            infoTable.setSpacingAfter(20);
            infoTable.addCell(createCell(new Paragraph("报告编号", fontNormalBold), true));
            infoTable.addCell(createCell(new Paragraph(existReport.getReportNo(), fontNormal), false));
            infoTable.addCell(createCell(new Paragraph("送检日期", fontNormalBold), true));
            infoTable.addCell(createCell(new Paragraph(delegation.getSubmitTime() != null ? delegation.getSubmitTime().toLocalDate().toString() : "/", fontNormal), false));
            infoTable.addCell(createCell(new Paragraph("委托单位", fontNormalBold), true));
            infoTable.addCell(createCell(new Paragraph(clientName, fontNormal), false));
            infoTable.addCell(createCell(new Paragraph("委托单号", fontNormalBold), true));
            infoTable.addCell(createCell(new Paragraph(delegation.getDelegationNo(), fontNormal), false));
            infoTable.addCell(createCell(new Paragraph("样品名称", fontNormalBold), true));
            infoTable.addCell(createCell(new Paragraph(delegation.getSampleName(), fontNormal), false));
            infoTable.addCell(createCell(new Paragraph("规格型号", fontNormalBold), true));
            infoTable.addCell(createCell(new Paragraph(delegation.getSampleSpecs() != null ? delegation.getSampleSpecs() : "/", fontNormal), false));
            infoTable.addCell(createCell(new Paragraph("检验依据", fontNormalBold), true));
            PdfPCell stdCell = createCell(new Paragraph(standardName, fontNormal), false);
            stdCell.setColspan(3);
            infoTable.addCell(stdCell);
            infoTable.addCell(createCell(new Paragraph("主 检 人", fontNormalBold), true));
            infoTable.addCell(createCell(new Paragraph(inspectorName, fontNormal), false));
            infoTable.addCell(createCell(new Paragraph("签 发 人", fontNormalBold), true));
            infoTable.addCell(createCell(new Paragraph(reviewerName, fontNormal), false));
            document.add(infoTable);

            Paragraph detailTitle = new Paragraph("检测项目明细", fontSubTitle);
            detailTitle.setSpacingAfter(10);
            document.add(detailTitle);

            PdfPTable itemTable = new PdfPTable(5);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{2.5f, 3f, 1.5f, 2.5f, 3.5f});
            itemTable.setSpacingAfter(20);
            itemTable.addCell(createCell(new Paragraph("盲样编号", fontNormalBold), true));
            itemTable.addCell(createCell(new Paragraph("检验项目", fontNormalBold), true));
            itemTable.addCell(createCell(new Paragraph("单位", fontNormalBold), true));
            itemTable.addCell(createCell(new Paragraph("实测结果", fontNormalBold), true));
            itemTable.addCell(createCell(new Paragraph("单项判定", fontNormalBold), true));

            Map<Long, String> taskCodeMap = tasks.stream().collect(Collectors.toMap(BizSampleTask::getId, BizSampleTask::getBlindSampleCode));
            Image checkedImg = getCheckboxImage(true);
            Image uncheckedImg = getCheckboxImage(false);

            for (BizInspectionRecord rec : inspectionRecords) {
                StdInspectionItem item = allItems.stream().filter(i -> i.getId().equals(rec.getItemId())).findFirst().orElse(null);
                String itemName = item != null ? item.getItemName() : "未知项目";
                String unit = (item != null && item.getUnit() != null) ? item.getUnit() : "-";
                String measuredText = rec.getMeasuredText();
                String resultVal = "-";
                if (measuredText != null && !measuredText.trim().isEmpty()) {
                    resultVal = measuredText;
                } else if (rec.getMeasuredValue() != null) {
                    resultVal = rec.getMeasuredValue().toString();
                }
                Paragraph judgePara = new Paragraph();
                if (rec.getResult() != null && rec.getResult() == 1) {
                    judgePara.add(new Chunk(checkedImg, 0, 0, true));
                    judgePara.add(new Chunk(" 合格   ", fontNormal));
                    judgePara.add(new Chunk(uncheckedImg, 0, 0, true));
                    judgePara.add(new Chunk(" 不合格", fontNormal));
                } else {
                    judgePara.add(new Chunk(uncheckedImg, 0, 0, true));
                    judgePara.add(new Chunk(" 合格   ", fontNormal));
                    judgePara.add(new Chunk(checkedImg, 0, 0, true));
                    judgePara.add(new Chunk(" 不合格", fontNormal));
                }
                itemTable.addCell(createCenterCell(new Paragraph(taskCodeMap.getOrDefault(rec.getTaskId(), "-"), fontNormal)));
                itemTable.addCell(createCenterCell(new Paragraph(itemName, fontNormal)));
                itemTable.addCell(createCenterCell(new Paragraph(unit, fontNormal)));
                itemTable.addCell(createCenterCell(new Paragraph(resultVal, fontNormal)));
                itemTable.addCell(createCell(judgePara, false));
            }
            document.add(itemTable);

            long totalItemsCount = inspectionRecords.size();
            long failItemsCount = inspectionRecords.stream().filter(r -> r.getResult() != null && r.getResult() == 0).count();
            Paragraph finalJudgePara = new Paragraph();
            if (existReport.getFinalConclusion() == 1) {
                finalJudgePara.add(new Chunk(checkedImg, 0, 0, true));
                finalJudgePara.add(new Chunk(" 合格   ", fontNormal));
                finalJudgePara.add(new Chunk(uncheckedImg, 0, 0, true));
                finalJudgePara.add(new Chunk(" 不合格", fontNormal));
            } else {
                finalJudgePara.add(new Chunk(uncheckedImg, 0, 0, true));
                finalJudgePara.add(new Chunk(" 合格   ", fontNormal));
                finalJudgePara.add(new Chunk(checkedImg, 0, 0, true));
                finalJudgePara.add(new Chunk(" 不合格", fontNormal));
            }

            PdfPTable conclusionTable = new PdfPTable(2);
            conclusionTable.setWidthPercentage(100);
            conclusionTable.setWidths(new float[]{3f, 7f});
            conclusionTable.setSpacingAfter(20);
            conclusionTable.addCell(createCell(new Paragraph("检测项总数", fontNormalBold), true));
            conclusionTable.addCell(createCell(new Paragraph(String.valueOf(totalItemsCount), fontNormal), false));
            conclusionTable.addCell(createCell(new Paragraph("不合格项数", fontNormalBold), true));
            conclusionTable.addCell(createCell(new Paragraph(String.valueOf(failItemsCount), fontNormal), false));
            conclusionTable.addCell(createCell(new Paragraph("综合结论", fontNormalBold), true));
            conclusionTable.addCell(createCell(finalJudgePara, false));
            document.add(conclusionTable);

            try {
                PdfPTable signTable = new PdfPTable(1);
                signTable.setWidthPercentage(30);
                signTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
                signTable.setSpacingBefore(30f);
                
                // 将检验机构与日期放入同一个段落中
                Paragraph textPara = new Paragraph();
                textPara.setLeading(30f);
                textPara.add(new Chunk("检验机构 (盖章):\n", fontNormalBold));
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy 年 MM 月 dd 日");
                textPara.add(new Chunk("日 期: " + existReport.getIssueTime().format(dtf), fontNormal));
                
                PdfPCell textCell = new PdfPCell(textPara);
                textCell.setBorder(PdfPCell.NO_BORDER);
                textCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                
                ClassPathResource imgResource = new ClassPathResource("images/Signature.png");
                if (imgResource.exists()) {
                    final Image signature = Image.getInstance(imgResource.getURL());
                    signature.scaleToFit(120, 120);
                    // 使用 PdfPCellEvent 确保印章绝对居中绘制
                    textCell.setCellEvent(new PdfPCellEvent() {
                        @Override
                        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
                            try {
                                PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
                                float x = position.getLeft() + (position.getWidth() - signature.getScaledWidth()) / 2;
                                float y = position.getBottom() + (position.getHeight() - signature.getScaledHeight()) / 2;
                                signature.setAbsolutePosition(x, y);
                                canvas.addImage(signature);
                            } catch (Exception e) {}
                        }
                    });
                }
                
                signTable.addCell(textCell);
                document.add(signTable);
            } catch (Exception e) {
                System.out.println("加载电子签章失败: " + e.getMessage());
            }

            Paragraph disclaimer = new Paragraph("声明：本报告为演示文件，不具备法律效力。", new Font(bfChinese, 10, Font.NORMAL));
            disclaimer.setAlignment(Element.ALIGN_LEFT);
            disclaimer.setSpacingBefore(40);
            document.add(disclaimer);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    @Override
    public int delete(Long id) {
        return mapper.deleteById(id);
    }

    @Override
    public BizReport getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<BizReport> getAll() {
        return mapper.selectAll();
    }

    private PdfPCell createCell(Paragraph paragraph, boolean isHeader) {
        PdfPCell cell = new PdfPCell(paragraph);
        cell.setUseAscender(true);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8f);
        cell.setBorderWidth(0.5f);
        if (isHeader) {
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(new com.itextpdf.text.BaseColor(240, 240, 240));
        } else {
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        return cell;
    }

    private PdfPCell createCenterCell(Paragraph paragraph) {
        PdfPCell cell = createCell(paragraph, false);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private Image getCheckboxImage(boolean checked) throws Exception {
        BufferedImage img = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 30, 30);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2.5f));
        g.drawRect(4, 4, 22, 22);
        if (checked) {
            g.setColor(new Color(0, 0, 0));
            g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(8, 15, 13, 22);
            g.drawLine(13, 22, 24, 8);
        }
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        Image pdfImg = Image.getInstance(baos.toByteArray());
        pdfImg.scaleToFit(10, 10);
        return pdfImg;
    }
}
