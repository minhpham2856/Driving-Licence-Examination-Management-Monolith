package util;

import java.nio.charset.StandardCharsets;

/**
 * Nội dung CSV mẫu / test cho import DSTS (10 cột, SBD từ file).
 */
public final class CandidateDstsCsvSamples {

    public static final String HEADER =
            "Số báo danh,Họ và tên,Số căn cước,Ngày sinh,Giới tính,Nơi cư trú,Hạng GPLX,Nội dung SH,Số điện thoại,Email";

    public static final String TEMPLATE_FILENAME = "danh_sach_mau_dsts.csv";
    public static final String TEST_FILENAME = "danh_sach_thi_sinh_test_dsts.csv";

    private static final String TEMPLATE_ROWS =
            "001,Nguyễn Văn An,012345678901,15/06/2000,Nam,\"Phường Bách Khoa, Hà Nội\",A1,SH lần đầu L+H,0987654321,nguyenvanan@example.com";

    private static final String TEST_ROWS =
            "001,Lê Hoàng Long,038201999991,12/10/1997,Nam,\"Xã Diễn Lộc, Thanh Hóa\",A1,SH lần đầu L+H,0912345678,hoanglong@example.com\r\n"
            + "002,Phạm Minh Anh,038202888882,25/08/2002,Nữ,\"Phường Lam Sơn, Thanh Hóa\",A1,SH lại L+H,0987654322,minhanh@example.com\r\n"
            + "003,Nguyễn Trung Kiên,038203777773,04/05/1995,Nam,\"Xã Văn Giang, Hưng Yên\",A1,SH lại L,0901234567,trungkien@example.com\r\n"
            + "004,Trần Thị Hoa,038204666664,18/02/1998,Nữ,\"Xã Như Quỳnh, Hưng Yên\",A1,SH lại H,0934567890,thihoa@example.com\r\n"
            + "005,Hoàng Văn Nam,038205555555,30/11/1996,Nam,\"Phường Thái Bình, Hưng Yên\",A1,Sát hạch H,0945678901,hoangnam@example.com\r\n"
            + "145,Nguyễn Đức Bình,038206444444,07/03/1999,Nam,\"Quận Cầu Giấy, Hà Nội\",B2,SH lần đầu L+H,0963789012,binhnd@example.com\r\n"
            + "146,Phan Thị Lan,038207333333,22/09/2001,Nữ,\"Quận Đống Đa, Hà Nội\",B2,SH lại L+H,0974890123,lanpt@example.com";

    private CandidateDstsCsvSamples() {
    }

    public static byte[] templateCsvBytes() {
        return withBom(HEADER + "\r\n" + TEMPLATE_ROWS + "\r\n");
    }

    public static byte[] testCsvBytes() {
        return withBom(HEADER + "\r\n" + TEST_ROWS + "\r\n");
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
