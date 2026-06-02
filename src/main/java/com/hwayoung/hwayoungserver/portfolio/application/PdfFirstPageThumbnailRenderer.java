package com.hwayoung.hwayoungserver.portfolio.application;

import com.hwayoung.hwayoungserver.portfolio.exception.InvalidPdfException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class PdfFirstPageThumbnailRenderer {
    public static final String CONTENT_TYPE = "image/png";

    private static final float RENDER_DPI = 144F;
    private static final String IMAGE_FORMAT = "png";

    public byte[] render(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new InvalidPdfException("PDF 파일은 필수입니다.");
        }

        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(bytes));
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (document.isEncrypted()) {
                throw new InvalidPdfException("암호화된 PDF는 업로드할 수 없습니다.");
            }
            if (document.getNumberOfPages() < 1) {
                throw new InvalidPdfException("PDF 페이지 수가 올바르지 않습니다.");
            }

            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, RENDER_DPI, ImageType.RGB);
            if (!ImageIO.write(image, IMAGE_FORMAT, outputStream)) {
                throw new InvalidPdfException("PDF 첫 페이지 이미지를 생성할 수 없습니다.");
            }
            return outputStream.toByteArray();
        } catch (InvalidPdfException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new InvalidPdfException("PDF 첫 페이지 이미지를 생성할 수 없습니다.");
        }
    }
}
