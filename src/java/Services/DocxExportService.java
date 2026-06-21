package Services;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface DocxExportService {

    /**
     * Determines the correct template filename based on the document type and license class.
     * @param documentType Type of the document ("BB1"=theory, "BB2"=practical, "BB3"=road)
     * @param licenseClass The license class (e.g., "A1", "B1", "B2")
     * @return The filename of the template, or null if not found.
     */
    String determineTemplateName(String documentType, String licenseClass);

    /**
     * Retrieves an InputStream for the specified template from the resources directory.
     * @param templateName The name of the template file (e.g., "BB1(A1-A-B1).docx").
     * @return InputStream of the template, or null if not found.
     */
    InputStream getTemplateStream(String templateName);

    /**
     * Clones the template and prepares a modifiable copy.
     * @param templateName The name of the template.
     * @return A byte array representing the cloned document.
     */
    byte[] cloneTemplate(String templateName);

    /**
     * Generates a single candidate document (e.g. result form).
     * @param sbd The candidate's SBD
     * @param sessionId The exam session ID
     * @param documentType The document type ("BB1", "BB2", or "BB3")
     * @param outputStream The output stream to write the resulting DOCX to.
     * @return true if successfully generated and written.
     */
    boolean generateCandidateDocument(String sbd, int sessionId, String documentType, OutputStream outputStream);

    /**
     * Batch generates documents for multiple candidates into a ZIP stream.
     * @param sbds The list of candidate SBDs
     * @param sessionId The exam session ID
     * @param documentType The document type ("BB1", "BB2", or "BB3")
     * @param outputStream The output stream to write the resulting package to.
     * @return true if successfully generated and written.
     */
    boolean batchGenerateDocuments(List<String> sbds, int sessionId, String documentType, OutputStream outputStream);

}
