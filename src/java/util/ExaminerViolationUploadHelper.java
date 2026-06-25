package util;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

 // Utility class that handles file uploads for violation evidence in the examiner portal.
public final class ExaminerViolationUploadHelper {

    // Relative subdirectory under the web root where violation uploads are stored
    private static final String UPLOAD_SUBDIR = "uploads/violations";

    // Private constructor prevents instantiation — all methods are static
    private ExaminerViolationUploadHelper() {
    }

         // Processes a file upload part from a multipart request and saves it to disk.
    public static String saveUpload(HttpServletRequest request, Part filePart, int sessionId) {
        // Guard: skip if no file was attached or the file is empty
        if (filePart == null || filePart.getSize() <= 0) {
            return null;
        }
        try {
            // Resolve the absolute path to the web application's root directory
            String webRoot = request.getServletContext().getRealPath("/");
            // Create a date prefix (yyyyMMdd) to group uploads by day
            String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            // Build the full directory path: <webRoot>/uploads/violations/<sessionId>
            String dirPath = webRoot + File.separator + UPLOAD_SUBDIR + File.separator + sessionId;
            // Create the target directory if it does not already exist
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Build a unique filename: <datePrefix>_<millis>_<originalFilename>
            String fileName = datePrefix + "_" + System.currentTimeMillis() + "_" + getSubmittedFileName(filePart);
            // Resolve the full file path as a Path object for NIO copy
            Path target = new File(dir, fileName).toPath();
            // Copy the uploaded input stream to the target file, replacing if exists
            try (InputStream in = filePart.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            // Return the relative path (from web root) for storage in the database
            return UPLOAD_SUBDIR + "/" + sessionId + "/" + fileName;
        } catch (Exception e) {
            // Log the error and return null to indicate upload failure
            e.printStackTrace();
            return null;
        }
    }

         // Extracts the original filename from a multipart {@link Part}'s {@code Content-Disposition} header.
    private static String getSubmittedFileName(Part part) {
        // Read the Content-Disposition header value
        String cd = part.getHeader("Content-Disposition");
        // Return fallback if header is missing
        if (cd == null) {
            return "unknown";
        }
        // Split the header into semicolon-delimited tokens to find the filename token
        for (String token : cd.split(";")) {
            String t = token.trim();
            // Check if this token contains the filename parameter
            if (t.startsWith("filename")) {
                // Extract the value after '=' and strip surrounding quotes
                String val = t.substring(t.indexOf('=') + 1).trim().replace("\"", "");
                // Strip any directory path separators (forward or back slash) from the filename
                int idx = Math.max(val.lastIndexOf('/'), val.lastIndexOf('\\'));
                // Return just the filename portion (after the last separator)
                return idx >= 0 ? val.substring(idx + 1) : val;
            }
        }
        // Return fallback if no filename token was found in the header
        return "unknown";
    }
}
