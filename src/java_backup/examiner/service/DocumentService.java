package examiner.service;

import examiner.dto.ExportContextDTO;
import examiner.enums.DocumentFormat;

import java.io.IOException;
import java.io.OutputStream;

public interface DocumentService {

    void export(ExportContextDTO ctx, String documentType, DocumentFormat format,
            String searchQuery, OutputStream out) throws IOException;

    void print(ExportContextDTO ctx, String documentType, int sbd, OutputStream out) throws IOException;
}
