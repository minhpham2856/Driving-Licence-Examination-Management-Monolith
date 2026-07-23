package registrant.util;

import registrant.dto.RegistrantDocumentView;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resolver URL xem tài liệu hồ sơ — chuyển giá trị {@code Document.DocumentUrl} lưu trong DB thành link trình duyệt.
 * <p>
 * Hỗ trợ ref Cloudinary ({@code cloudinary:…}), file legacy {@code /uploads/registrant/*}
 * và seed demo Cloudinary; gọi từ JSP upload/profile và {@link registrant.controller.RegistrantDocumentViewServlet}.
 */
public final class DocumentUrlResolver {

    private static final Logger LOG = Logger.getLogger(DocumentUrlResolver.class.getName());

    /** Seed cũ dùng URL demo không tồn tại trên Cloudinary → thay placeholder xem được. */
    private static final String SEED_DEMO_HOST = "res.cloudinary.com/demo/";

    private DocumentUrlResolver() {
    }

    /** Resolve DocumentUrl của cả list sang URL xem được trên browser. */
    public static void resolveViewUrls(List<RegistrantDocumentView> docs, HttpServletRequest request) {
        if (docs == null) {
            return;
        }
        for (RegistrantDocumentView doc : docs) {
            if (doc != null) {
                doc.setDocumentUrl(resolveViewUrl(doc.getDocumentUrl(), request));
            }
        }
    }

    /** Đổi ref Cloudinary/local/seed-demo thành URL xem được trên browser. */
    public static String resolveViewUrl(String storedRef, HttpServletRequest request) {
        if (storedRef == null || storedRef.isBlank()) {
            return storedRef;
        }
        if (isDeadSeedDemoUrl(storedRef)) {
            return placeholderForSeedDemo(storedRef);
        }
        if (storedRef.startsWith("http://") || storedRef.startsWith("https://")) {
            return storedRef;
        }
        if (CloudinaryDocumentStorage.isCloudinaryRef(storedRef)) {
            CloudinaryDocumentStorage.CloudinaryRef ref = CloudinaryDocumentStorage.parseRef(storedRef);
            if (ref == null) {
                return storedRef;
            }
            String signed = CloudinaryDocumentStorage.signedDeliveryUrl(ref.resourceType, ref.publicId);
            return signed != null ? signed : storedRef;
        }
        if (request != null) {
            return RegistrantUploadStorage.normalizePublicUrl(request, storedRef);
        }
        return storedRef;
    }

    private static boolean isDeadSeedDemoUrl(String url) {
        String lower = url.toLowerCase();
        return lower.contains(SEED_DEMO_HOST)
                && (lower.contains("/registrant/") || lower.contains("_demo"));
    }

    private static String placeholderForSeedDemo(String url) {
        String lower = url.toLowerCase();
        if (lower.contains("portrait")) {
            return "https://placehold.co/300x400/png?text=Anh+chan+dung+3x4";
        }
        if (lower.contains("cccd_front") || lower.contains("idfront") || lower.contains("id_front")) {
            return "https://placehold.co/600x380/png?text=CCCD+mat+truoc";
        }
        if (lower.contains("cccd_back") || lower.contains("idback") || lower.contains("id_back")) {
            return "https://placehold.co/600x380/png?text=CCCD+mat+sau";
        }
        if (lower.contains("health")) {
            return "https://placehold.co/600x800/png?text=Giay+kham+SK";
        }
        return "https://placehold.co/600x400/png?text=Tai+lieu+demo";
    }

    /** Xóa tệp trên Cloudinary hoặc local theo tham chiếu đã lưu. */
    public static void deleteStoredRef(ServletContext ctx, String storedRef) {
        if (storedRef == null || storedRef.isBlank()) {
            return;
        }
        if (CloudinaryDocumentStorage.isCloudinaryRef(storedRef)) {
            try {
                CloudinaryDocumentStorage.destroy(storedRef);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Không xóa được tệp Cloudinary: " + storedRef, ex);
            }
            return;
        }
        if (ctx != null) {
            RegistrantUploadStorage.deleteStoredFile(ctx, storedRef);
        }
    }
}
