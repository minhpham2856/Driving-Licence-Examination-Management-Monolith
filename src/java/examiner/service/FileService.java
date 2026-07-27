package examiner.service;

import examiner.dto.ExportContextDTO;
import examiner.dto.PrintPreviewDTO;
import shared.enums.FileType;

import java.io.IOException;
import java.io.OutputStream;

// Service contract for exporting examiner reports and building print preview models.
public interface FileService {

    // Exports examiner data in the requested file format to the output stream.
    void export(ExportContextDTO ctx,
            String documentType,
            FileType format,
            String searchQuery,
            int sbd, OutputStream out) throws IOException;

    // Builds print preview model for JSP forward (session table or per-candidate form).
    PrintPreviewDTO print(ExportContextDTO ctx,
            String documentType,
            int sbd,
            String searchQuery) throws IOException;
}
