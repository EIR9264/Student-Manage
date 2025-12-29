package com.example.backend.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class DocumentExtractorService {

    private static final int MAX_CONTENT_LENGTH = 100000; // 最大提取10万字符

    /**
     * 根据文件类型提取文本内容
     */
    public String extractText(InputStream inputStream, String fileType) {
        try {
            String content = switch (fileType.toUpperCase()) {
                case "PDF" -> extractPdfText(inputStream);
                case "DOC" -> extractDocText(inputStream);
                case "DOCX" -> extractDocxText(inputStream);
                case "PPT" -> extractPptText(inputStream);
                case "PPTX" -> extractPptxText(inputStream);
                default -> "";
            };

            // 限制内容长度
            if (content != null && content.length() > MAX_CONTENT_LENGTH) {
                content = content.substring(0, MAX_CONTENT_LENGTH);
            }

            return content;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 提取PDF文本
     */
    public String extractPdfText(InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 提取Word文本（.docx）
     */
    public String extractDocxText(InputStream inputStream) {
        try (XWPFDocument doc = new XWPFDocument(inputStream)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            return extractor.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 提取Word文本（.doc）
     */
    public String extractDocText(InputStream inputStream) {
        try (HWPFDocument doc = new HWPFDocument(inputStream)) {
            WordExtractor extractor = new WordExtractor(doc);
            return extractor.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 提取PPT文本（.pptx）
     */
    public String extractPptxText(InputStream inputStream) {
        try (XMLSlideShow ppt = new XMLSlideShow(inputStream)) {
            StringBuilder sb = new StringBuilder();
            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        sb.append(((XSLFTextShape) shape).getText()).append("\n");
                    }
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 提取PPT文本（.ppt）
     */
    public String extractPptText(InputStream inputStream) {
        try (HSLFSlideShow ppt = new HSLFSlideShow(inputStream);
                SlideShowExtractor<?, ?> extractor = new SlideShowExtractor<>(ppt)) {
            return extractor.getText();
        } catch (Exception e) {
            return "";
        }
    }
}
