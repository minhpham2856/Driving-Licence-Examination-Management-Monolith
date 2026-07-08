package util;

import java.nio.charset.StandardCharsets;

/**
 * Nội dung CSV mẫu cho import DSTS (10 cột, SBD từ file).
 */
public final class CandidateDstsCsvSamples {

    public static final String HEADER =
            "Số báo danh,Họ và tên,Số căn cước,Ngày sinh,Giới tính,Nơi cư trú,Hạng GPLX,Nội dung SH,Số điện thoại,Email";

    public static final String TEMPLATE_FILENAME = "danh_sach_mau_dsts.csv";

    private static final String TEMPLATE_ROWS =
            "001,Nguyễn Văn An,012345678901,15/06/2000,Nam,\"Phường Bách Khoa, Hà Nội\",A1,SH lần đầu L+H,0987654321,nguyenvanan@example.com";

    private CandidateDstsCsvSamples() {
    }

    public static byte[] templateCsvBytes() {
        return withBom(HEADER + "\r\n" + TEMPLATE_ROWS + "\r\n");
    }

    private static byte[] withBom(String csv) {
        byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
        byte[] body = csv.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[bom.length + body.length];
        System.arraycopy(bom, 0, out, 0, bom.length);
        System.arraycopy(body, 0, out, bom.length, body.length);
        return out;
    }
}
