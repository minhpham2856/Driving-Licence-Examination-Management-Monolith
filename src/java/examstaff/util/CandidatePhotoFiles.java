package examstaff.util;

import examstaff.dto.ExamRegistrationDTO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper thuần (không Servlet API) quản lý đường dẫn và lưu trữ ảnh thí sinh trên đĩa.
 * <p>
 * Thứ tự ưu tiên thư mục dữ liệu (ghi/đọc chính):
 * <ol>
 *   <li>{@code -Ddlem.photos.dir} — cấu hình tường minh</li>
 *   <li>{@code $catalina.base/dlem-data/candidate-photos} — khi chạy Tomcat</li>
 *   <li>{@code $user.home/.dlem/candidate-photos} — fallback desktop/dev</li>
 * </ol>
 * Khi tìm file để phục vụ (xem {@link #findPhotoFile}), còn quét thêm thư mục dự án
 * {@code candidate-photos} và {@code webRoot/assets/imgs/candidates}.
 */
public final class CandidatePhotoFiles {

    /** Không cho khởi tạo — chỉ dùng static. */
    private CandidatePhotoFiles() {
    }

    /**
     * Xác định thư mục gốc lưu ảnh thí sinh theo cấu hình / môi trường runtime.
     * <p>
     * Luồng chọn đường dẫn:
     * <ol>
     *   <li>Nếu có system property {@code dlem.photos.dir} (không blank) → dùng đúng path đó</li>
     *   <li>Ngược lại, nếu có {@code catalina.base} → {@code catalina.base/dlem-data/candidate-photos}</li>
     *   <li>Cuối cùng → {@code user.home/.dlem/candidate-photos} (hoặc {@code .} nếu thiếu home)</li>
     * </ol>
     *
     * @return thư mục gốc ảnh (chưa đảm bảo đã tồn tại trên đĩa)
     */
    public static File photoDir() {
        // Bước 1: ưu tiên cấu hình tường minh từ JVM
        String configured = System.getProperty("dlem.photos.dir");
        if (configured != null && !configured.isBlank()) {
            return new File(configured.trim());
        }
        // Bước 2: thư mục dữ liệu cạnh Tomcat (catalina.base)
        String catalina = System.getProperty("catalina.base");
        if (catalina != null && !catalina.isBlank()) {
            return new File(catalina, "dlem-data" + File.separator + "candidate-photos");
        }
        // Bước 3: fallback về home người dùng khi không có Tomcat
        return new File(System.getProperty("user.home", "."), ".dlem" + File.separator + "candidate-photos");
    }

    /**
     * Chuẩn hóa {@code photoUrl} cho từng phần tử trong hàng đợi thí sinh (mutate tại chỗ).
     * <p>
     * Gọi {@link #normalizePhotoUrl(String, String)} với {@code webRootPath} chung;
     * chỉ ghi lại URL khi kết quả normalize khác null.
     *
     * @param webRootPath đường dẫn tuyệt đối tới web root (có thể null) — dùng khi resolve file tương đối
     * @param queue       danh sách đăng ký (null/rỗng → no-op)
     */
    public static void normalizeQueue(String webRootPath, List<ExamRegistrationDTO> queue) {
        // Bước 1: bỏ qua nếu không có dữ liệu
        if (queue == null || queue.isEmpty()) {
            return;
        }
        // Bước 2: duyệt từng thí sinh và chuẩn hóa photoUrl
        for (ExamRegistrationDTO c : queue) {
            if (c == null) {
                continue;
            }
            String normalized = normalizePhotoUrl(webRootPath, c.getPhotoUrl());
            // Bước 3: chỉ gán lại khi normalize trả về giá trị (có thể vẫn là chuỗi gốc)
            if (normalized != null) {
                c.setPhotoUrl(normalized);
            }
        }
    }

    /**
     * Chuẩn hóa một URL/đường dẫn ảnh thành dạng sẵn dùng (absolute FS hoặc web path).
     * <p>
     * Luồng resolve:
     * <ol>
     *   <li>Blank → trả nguyên (null/blank)</li>
     *   <li>{@code http(s)://} → giữ nguyên (URL tuyệt đối)</li>
     *   <li>Bắt đầu bằng {@code /} → giữ nguyên (context-relative web path)</li>
     *   <li>Absolute path trên đĩa và file tồn tại → giữ nguyên</li>
     *   <li>Tìm theo tên file trong {@link #photoDir()} → trả absolute path nếu tồn tại</li>
     *   <li>Ghép dưới {@code webRootPath} nếu có → trả absolute path nếu tồn tại</li>
     *   <li>Không tìm thấy → trả chuỗi đã trim (không đổi nghĩa)</li>
     * </ol>
     *
     * @param webRootPath đường dẫn web root (có thể null/blank)
     * @param photoUrl    URL hoặc path ảnh gốc
     * @return URL/path đã chuẩn hóa, hoặc giá trị gốc đã trim / null
     */
    public static String normalizePhotoUrl(String webRootPath, String photoUrl) {
        // Bước 1: thiếu dữ liệu → không xử lý
        if (photoUrl == null || photoUrl.isBlank()) {
            return photoUrl;
        }
        String trimmed = photoUrl.trim();
        // Bước 2: URL mạng tuyệt đối — không cần resolve FS
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        // Bước 3: path web absolute (context) — giữ nguyên
        if (trimmed.startsWith("/")) {
            return trimmed;
        }
        // Bước 4: absolute file path trên đĩa và đã tồn tại
        File file = new File(trimmed);
        if (file.isAbsolute() && file.exists()) {
            return trimmed;
        }
        // Bước 5: tìm theo basename trong thư mục dữ liệu chuẩn (photoDir)
        File inData = new File(photoDir(), new File(trimmed).getName());
        if (inData.exists()) {
            return inData.getAbsolutePath();
        }
        // Bước 6: thử dưới web root (path tương đối → absolute FS)
        if (webRootPath != null && !webRootPath.isBlank()) {
            File underWeb = new File(webRootPath, trimmed.replace('/', File.separatorChar));
            if (underWeb.exists()) {
                return underWeb.getAbsolutePath();
            }
        }
        // Bước 7: không resolve được — trả chuỗi đã trim
        return trimmed;
    }

    /**
     * Lấy tên file (basename) từ URL/path ảnh.
     * <p>
     * Chuẩn hóa separator về {@code /}, bỏ prefix {@code /}, rồi lấy phần sau dấu {@code /} cuối.
     *
     * @param photoUrl URL hoặc đường dẫn ảnh
     * @return tên file, hoặc {@code null} nếu blank
     */
    public static String extractFileName(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return null;
        }
        // Bước 1: thống nhất separator Windows/Unix
        String normalized = photoUrl.trim().replace("\\", "/");
        // Bước 2: bỏ leading slash để không còn path root rỗng
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        // Bước 3: basename = phần sau '/' cuối cùng
        return normalized.contains("/")
                ? normalized.substring(normalized.lastIndexOf('/') + 1)
                : normalized;
    }

    /**
     * Ghi bytes ảnh ra đĩa dưới tên {@code fileName} trong thư mục ảnh có thể ghi.
     * <p>
     * Luồng ghi file:
     * <ol>
     *   <li>Validate {@code fileName} và {@code imageBytes}</li>
     *   <li>Nếu có {@code dlem.photos.dir} → {@link #ensureDir} thư mục đó</li>
     *   <li>Ngược lại → {@link #ensureDir}({@link #photoDir()})</li>
     *   <li>Ghi đè/tạo file {@code dir/fileName} bằng {@link FileOutputStream}</li>
     * </ol>
     * Tham số {@code webRoot} hiện không dùng khi ghi (giữ chữ ký API tương thích servlet).
     *
     * @param webRoot    web root (reserved — không ảnh hưởng đường ghi hiện tại)
     * @param fileName   tên file đích (không path)
     * @param imageBytes nội dung ảnh
     * @throws IOException khi dữ liệu không hợp lệ hoặc không tạo/ghi được thư mục/file
     */
    public static void writePhotoFile(String webRoot, String fileName, byte[] imageBytes) throws IOException {
        // Bước 1: validate đầu vào
        if (fileName == null || fileName.isBlank() || imageBytes == null || imageBytes.length == 0) {
            throw new IOException("Dữ liệu ảnh không hợp lệ");
        }
        // Bước 2: chọn thư mục ghi — ưu tiên dlem.photos.dir, rồi photoDir()
        String configured = System.getProperty("dlem.photos.dir");
        File dir;
        if (configured != null && !configured.isBlank()) {
            dir = ensureDir(new File(configured.trim()));
        } else {
            dir = ensureDir(photoDir());
        }
        // Bước 3: ghi toàn bộ bytes vào file đích
        File file = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(imageBytes);
        }
    }

    /**
     * Đổi tên file ảnh thành path web tương đối dùng trong JSP/static.
     *
     * @param fileName tên file (basename)
     * @return {@code assets/imgs/candidates/} + fileName
     */
    public static String toWebPhotoPath(String fileName) {
        return "assets/imgs/candidates/" + fileName;
    }

    /**
     * Tìm file ảnh vật lý trên đĩa từ {@code photoUrl} (theo basename + thứ tự thư mục tìm kiếm).
     * <p>
     * Luồng tìm kiếm:
     * <ol>
     *   <li>Tách {@code fileName} bằng {@link #extractFileName}</li>
     *   <li>Duyệt từng thư mục từ {@link #collectPhotoSearchDirs} — lấy file tồn tại, length &gt; 0</li>
     *   <li>Fallback: {@code webRoot/assets/imgs/candidates/fileName}</li>
     *   <li>Fallback: path tương đối theo URL gốc dưới {@code webRoot}</li>
     *   <li>Không thấy → {@code null}</li>
     * </ol>
     *
     * @param webRoot  đường dẫn web root (có thể null)
     * @param photoUrl URL/path ảnh lưu trên hồ sơ
     * @return {@link File} hợp lệ hoặc {@code null}
     */
    public static File findPhotoFile(String webRoot, String photoUrl) {
        // Bước 1: lấy basename; không có tên → dừng
        String fileName = extractFileName(photoUrl);
        if (fileName == null) {
            return null;
        }
        // Bước 2: quét các thư mục dữ liệu / dự án theo thứ tự ưu tiên
        for (File dir : collectPhotoSearchDirs(webRoot)) {
            if (dir == null) {
                continue;
            }
            File candidate = new File(dir, fileName);
            if (candidate.isFile() && candidate.length() > 0) {
                return candidate;
            }
        }
        // Bước 3: fallback dưới tài nguyên web tĩnh
        if (webRoot != null && !webRoot.isBlank()) {
            File inWebAssets = new File(webRoot, "assets" + File.separator + "imgs"
                    + File.separator + "candidates" + File.separator + fileName);
            if (inWebAssets.isFile() && inWebAssets.length() > 0) {
                return inWebAssets;
            }
            // Bước 4: resolve path tương đối đúng như photoUrl ghi trên hồ sơ
            if (photoUrl != null && photoUrl.contains("/")) {
                String relative = photoUrl.trim().replace("\\", "/").replaceFirst("^/+", "");
                File viaUrl = new File(webRoot, relative.replace("/", File.separator));
                if (viaUrl.isFile() && viaUrl.length() > 0) {
                    return viaUrl;
                }
            }
        }
        return null;
    }

    /**
     * Chọn thư mục ghi được (có quyền tạo) cho ảnh — dùng nội bộ khi thu thập search dirs.
     * <p>
     * Thứ tự: {@code dlem.photos.dir} → {@link #projectPhotoDir} → {@link #photoDir()}.
     *
     * @param webRoot web root để suy project root
     * @return thư mục đã đảm bảo tồn tại
     * @throws IOException nếu không tạo được thư mục
     */
    private static File resolveWritablePhotoDir(String webRoot) throws IOException {
        // Bước 1: cấu hình JVM tường minh
        String configured = System.getProperty("dlem.photos.dir");
        if (configured != null && !configured.isBlank()) {
            return ensureDir(new File(configured.trim()));
        }
        // Bước 2: thư mục candidate-photos cạnh project (dev)
        File inProject = projectPhotoDir(webRoot);
        if (inProject != null) {
            return ensureDir(inProject);
        }
        // Bước 3: thư mục runtime chuẩn
        return ensureDir(photoDir());
    }

    /**
     * Trả path {@code <projectRoot>/candidate-photos} nếu suy được project root từ web root.
     *
     * @param webRoot đường dẫn web root deploy/build
     * @return thư mục project photos hoặc {@code null}
     */
    private static File projectPhotoDir(String webRoot) {
        File projectRoot = resolveProjectRootFromWebRoot(webRoot);
        if (projectRoot != null) {
            return new File(projectRoot, "candidate-photos");
        }
        return null;
    }

    /**
     * Suy thư mục gốc dự án từ đường dẫn web root (hỗ trợ layout Ant: {@code build/web}, {@code web}).
     * <p>
     * Luồng suy luận:
     * <ul>
     *   <li>{@code .../build/web} → parent của {@code build} (= project root)</li>
     *   <li>{@code .../web} (không qua build) → parent của {@code web}</li>
     *   <li>parent tên {@code build} → parent của {@code build}</li>
     *   <li>còn lại → {@code parent} của webRoot</li>
     * </ul>
     *
     * @param webRoot đường dẫn web root
     * @return project root hoặc {@code null} nếu thiếu input / không có parent
     */
    private static File resolveProjectRootFromWebRoot(String webRoot) {
        if (webRoot == null || webRoot.isBlank()) {
            return null;
        }
        File webRootDir = new File(webRoot);
        File parent = webRootDir.getParentFile();
        if (parent == null) {
            return null;
        }
        // Layout: <project>/build/web hoặc <project>/web
        if ("web".equalsIgnoreCase(webRootDir.getName())) {
            if ("build".equalsIgnoreCase(parent.getName())) {
                return parent.getParentFile();
            }
            return parent;
        }
        // Layout: webRoot nằm trực tiếp dưới build
        if ("build".equalsIgnoreCase(parent.getName())) {
            return parent.getParentFile();
        }
        return parent;
    }

    /**
     * Thu thập (không trùng) các thư mục có thể chứa ảnh để {@link #findPhotoFile} quét.
     * <p>
     * Thứ tự thêm (LinkedHashSet giữ thứ tự lần đầu):
     * <ol>
     *   <li>{@code dlem.photos.dir} nếu có</li>
     *   <li>{@link #projectPhotoDir}</li>
     *   <li>parent của {@code web}/… → {@code candidate-photos} cạnh sibling web</li>
     *   <li>{@link #resolveWritablePhotoDir} (bỏ qua nếu IOException)</li>
     *   <li>{@link #photoDir()} luôn là ứng viên cuối</li>
     * </ol>
     *
     * @param webRoot web root
     * @return danh sách thư mục tìm kiếm (có thứ tự)
     */
    private static List<File> collectPhotoSearchDirs(String webRoot) {
        Set<File> dirs = new LinkedHashSet<>();
        // Bước 1: cấu hình tường minh
        String configured = System.getProperty("dlem.photos.dir");
        if (configured != null && !configured.isBlank()) {
            dirs.add(new File(configured.trim()));
        }
        // Bước 2: thư mục project
        File projectDir = projectPhotoDir(webRoot);
        if (projectDir != null) {
            dirs.add(projectDir);
        }
        // Bước 3: sibling candidate-photos khi webRoot tên là "web"
        if (webRoot != null && !webRoot.isBlank()) {
            File webRootDir = new File(webRoot);
            File parent = webRootDir.getParentFile();
            if (parent != null && "web".equalsIgnoreCase(webRootDir.getName())) {
                dirs.add(new File(parent, "candidate-photos"));
            }
        }
        // Bước 4: thư mục ghi được (có thể trùng các mục trên — Set lọc)
        try {
            dirs.add(resolveWritablePhotoDir(webRoot));
        } catch (IOException ignored) {
            // Không chặn tìm kiếm nếu chưa tạo được dir ghi
        }
        // Bước 5: luôn thêm photoDir runtime
        dirs.add(photoDir());
        return new ArrayList<>(dirs);
    }

    /**
     * Đảm bảo thư mục tồn tại; tạo bằng {@link File#mkdirs()} nếu chưa có.
     *
     * @param dir thư mục đích
     * @return cùng {@code dir} sau khi xác nhận tồn tại
     * @throws IOException nếu tạo thư mục thất bại
     */
    private static File ensureDir(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Không tạo được thư mục lưu ảnh: " + dir.getAbsolutePath());
        }
        return dir;
    }
}
