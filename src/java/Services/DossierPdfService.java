package Services;

import DTOs.DossierDTO;
import java.io.IOException;
import java.nio.file.Path;

public interface DossierPdfService {

    byte[] generate(DossierDTO dossier, Path webRoot) throws IOException;
}
