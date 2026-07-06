package service;

import dto.ExaminerExportContext;
import enums.DocumentFormat;

import java.io.IOException;
import java.io.OutputStream;

public interface ExaminerDocumentService {

    void export(ExaminerExportContext ctx, String documentType, DocumentFormat format,
            String searchQuery, OutputStream out) throws IOException;

    void print(ExaminerExportContext ctx, String documentType, int sbd, OutputStream out) throws IOException;
}
