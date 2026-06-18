package Services;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface DocxExportService {

    /**
     * Determines the correct template filename based on the document type and license class.
     * @param documentType Type of the document (e.g., "VB1", "VB2", "VB3")
     * @param licenseClass The license class (e.g., "A1", "B1", "B2")
     * @return The filename of the template, or null if not found.
     */
    String determineTemplateName(String documentType, String licenseClass);

    /**
     * Retrieves an InputStream for the specified template from the resources directory.
     * @param templateName The name of the template file (e.g., "VB1(B1).docx").
     * @return InputStream of the template, or null if not found.
     */
    InputStream getTemplateStream(String templateName);

    /**
     * Clones the template and prepares a modifiable copy.
     * In an actual POI implementation, this might return an XWPFDocument or a byte array.
     * @param templateName The name of the template.
     * @return A byte array representing the cloned document.
     */
    byte[] cloneTemplate(String templateName);

    /**
     * Generates a single candidate document (like a result form or signature form).
     * @param sbd The candidate's SBD (So Bao Danh)
     * @param sessionId The exam session ID
     * @param documentType The document type (e.g., "VB1")
     * @param outputStream The output stream to write the resulting DOCX to.
     * @return true if successfully generated and written.
     */
    boolean generateCandidateDocument(String sbd, int sessionId, String documentType, OutputStream outputStream);

    /**
     * Batch generates documents for multiple candidates, possibly merging them into a single DOCX
     * or packaging them into a ZIP stream depending on implementation.
     * @param sbds The list of candidate SBDs
     * @param sessionId The exam session ID
     * @param documentType The document type (e.g., "VB1")
     * @param outputStream The output stream to write the resulting package to.
     * @return true if successfully generated and written.
     */
    boolean batchGenerateDocuments(List<String> sbds, int sessionId, String documentType, OutputStream outputStream);

}
