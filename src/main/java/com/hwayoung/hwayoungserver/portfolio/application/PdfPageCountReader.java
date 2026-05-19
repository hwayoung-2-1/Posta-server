package com.hwayoung.hwayoungserver.portfolio.application;

import com.hwayoung.hwayoungserver.portfolio.exception.InvalidPdfException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PdfPageCountReader {
    public int countPages(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new InvalidPdfException("PDF 파일은 필수입니다.");
        }

        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(bytes))) {
            if (document.isEncrypted()) {
                throw new InvalidPdfException("암호화된 PDF는 업로드할 수 없습니다.");
            }

            int pageCount = document.getNumberOfPages();
            if (pageCount < 1) {
                throw new InvalidPdfException("PDF 페이지 수가 올바르지 않습니다.");
            }
            return pageCount;
        } catch (InvalidPdfException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new InvalidPdfException("PDF 파일을 열 수 없습니다.");
        }
    }
}
