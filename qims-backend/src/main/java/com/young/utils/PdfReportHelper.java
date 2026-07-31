package com.young.utils;

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
import com.young.pojo.SysClient;
import com.young.pojo.StdStandard;
import com.young.pojo.enums.InspectionResult;
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
import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

public class PdfReportHelper {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PdfReportHelper.class);

    /**
     * 渲染生成检验报告 PDF 文件
     */
    public static void generatePdf(
            String pdfPath, 
            BizReport report, 
            BizDelegation delegation, 
            StdStandard standard, 
            SysClient client, 
            String inspectorName, 
            String reviewerName, 
            List<BizSampleTask> tasks, 
            List<BizInspectionRecord> inspectionRecords, 
            List<StdInspectionItem> allItems) throws Exception {

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
        document.open();

        // 准备基本信息
        String clientName = client != null && client.getCompanyName() != null ? client.getCompanyName() : "/";
        String standardName = standard != null && standard.getStandardCode() != null ? standard.getStandardCode() + " " + standard.getStandardName() : "/";

        // 设置中文字体 (STSong-Light)
        BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UTF16-H", BaseFont.NOT_EMBEDDED);
        Font fontTitle = new Font(bfChinese, 24, Font.BOLD);
        Font fontSubTitle = new Font(bfChinese, 16, Font.BOLD);
        Font fontNormal = new Font(bfChinese, 12, Font.NORMAL);
        Font fontNormalBold = new Font(bfChinese, 12, Font.BOLD);

        // 标题
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
        infoTable.addCell(createCell(new Paragraph(report.getReportNo(), fontNormal), false));
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
            if (rec.getResult() != null && rec.getResult() == InspectionResult.QUALIFIED.getCode()) {
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
        long failItemsCount = inspectionRecords.stream().filter(r -> r.getResult() != null && r.getResult() == InspectionResult.UNQUALIFIED.getCode()).count();
        
        Paragraph finalJudgePara = new Paragraph();
        if (report.getFinalConclusion() == InspectionResult.QUALIFIED.getCode()) {
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
        
        // 电子签章与落款
        try {
            PdfPTable signTable = new PdfPTable(1);
            signTable.setWidthPercentage(30);
            signTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            signTable.setSpacingBefore(30f);
            
            Paragraph textPara = new Paragraph();
            textPara.setLeading(30f);
            textPara.add(new Chunk("检验机构 (盖章):\n", fontNormalBold));
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy 年 MM 月 dd 日");
            textPara.add(new Chunk("日 期: " + report.getIssueTime().format(dtf), fontNormal));
            
            PdfPCell textCell = new PdfPCell(textPara);
            textCell.setBorder(PdfPCell.NO_BORDER);
            textCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            
            ClassPathResource imgResource = new ClassPathResource("images/Signature.png");
            if (imgResource.exists()) {
                final Image signature = Image.getInstance(imgResource.getURL());
                signature.scaleToFit(120, 120);
                textCell.setCellEvent(new PdfPCellEvent() {
                    @Override
                    public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
                        try {
                            PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
                            float x = position.getLeft() + (position.getWidth() - signature.getScaledWidth()) / 2;
                            float y = position.getBottom() + (position.getHeight() - signature.getScaledHeight()) / 2;
                            signature.setAbsolutePosition(x, y);
                            canvas.addImage(signature);
                        } catch (Exception e) {
                            log.warn("渲染电子签章失败", e);
                        }
                    }
                });
            }
            
            signTable.addCell(textCell);
            document.add(signTable);
        } catch (Exception e) {
            log.warn("加载电子签章失败: {}", e.getMessage());
        }

        Paragraph disclaimer = new Paragraph("声明：本报告为演示文件，不具备法律效力。", new Font(bfChinese, 10, Font.NORMAL));
        disclaimer.setAlignment(Element.ALIGN_LEFT);
        disclaimer.setSpacingBefore(40);
        document.add(disclaimer);

        document.close();
    }

    private static PdfPCell createCell(Paragraph paragraph, boolean isHeader) {
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

    private static PdfPCell createCenterCell(Paragraph paragraph) {
        PdfPCell cell = createCell(paragraph, false);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private static Image getCheckboxImage(boolean checked) throws Exception {
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
