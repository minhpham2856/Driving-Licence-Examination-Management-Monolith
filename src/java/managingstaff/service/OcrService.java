package managingstaff.service;

import managingstaff.dto.OcrResultDTO;
import java.io.IOException;
import java.nio.file.Path;

public interface OcrService {

    boolean isConfigured();

    OcrResultDTO recognize(Path file) throws IOException, InterruptedException;
}
