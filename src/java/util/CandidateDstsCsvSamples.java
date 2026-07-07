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
    public static final String BULK_TEST_FILENAME = "danh_sach_thi_sinh_55_test_dsts.csv";

    private static final String TEMPLATE_ROWS =
            "001,Nguyễn Văn An,012345678901,15/06/2000,Nam,\"Phường Bách Khoa, Hà Nội\",A1,SH lần đầu L+H,0987654321,nguyenvanan@example.com";

    private static final String TEST_ROWS =
            "001,Lê Hoàng Long,038201999991,12/10/1997,Nam,\"Xã Diễn Lộc, Thanh Hóa\",A1,SH lần đầu L+H,0912345678,hoanglong@example.com\r\n"
            + "002,Phạm Minh Anh,038202888882,25/08/2002,Nữ,\"Phường Lam Sơn, Thanh Hóa\",A1,SH lại L+H,0987654322,minhanh@example.com\r\n"
            + "003,Nguyễn Trung Kiên,038203777773,04/05/1995,Nam,\"Xã Văn Giang, Hưng Yên\",A1,SH lại L,0901234567,trungkien@example.com\r\n"
            + "004,Trần Thị Hoa,038204666664,18/02/1998,Nữ,\"Xã Như Quỳnh, Hưng Yên\",A1,SH lại H,0934567890,thihoa@example.com\r\n"
            + "005,Hoàng Văn Nam,038205555555,30/11/1996,Nam,\"Phường Thái Bình, Hưng Yên\",A1,Sát hạch H,0945678901,hoangnam@example.com\r\n"
            + "145,Nguyễn Đức Bình,038206444444,07/03/1999,Nam,\"Quận Cầu Giấy, Hà Nội\",B1,SH lần đầu L+H,0963789012,binhnd@example.com\r\n"
            + "146,Phan Thị Lan,038207333333,22/09/2001,Nữ,\"Quận Đống Đa, Hà Nội\",B1,SH lại L+H,0974890123,lanpt@example.com";

    private static final String[] SH_CONTENTS = {
            "SH lần đầu L+H", "SH lại L+H", "SH lại L", "SH lại H", "Sát hạch H"
    };
    private static final String[] HO = {
            "Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Vũ", "Đặng", "Bùi", "Đỗ"
    };
    private static final String[] DEM = {
            "Văn", "Thị", "Minh", "Quốc", "Đức", "Hữu", "Thanh", "Kim", "Xuân", "Thu"
    };
    private static final String[] TEN = {
            "An", "Bình", "Chi", "Dũng", "Em", "Giang", "Hà", "Khánh", "Long", "Mai",
            "Nam", "Oanh", "Phúc", "Quân", "Sơn", "Tâm", "Uyên", "Vinh", "Yến", "Bảo"
    };
    private static final String[] ADDRESSES = {
            "Phường Bách Khoa, Hà Nội",
            "Quận Cầu Giấy, Hà Nội",
            "Quận Đống Đa, Hà Nội",
            "Phường Lam Sơn, Thanh Hóa",
            "Xã Diễn Lộc, Thanh Hóa",
            "Xã Văn Giang, Hưng Yên",
            "Phường Thái Bình, Hưng Yên",
            "Quận Hải Châu, Đà Nẵng",
            "Quận Ngũ Hành Sơn, Đà Nẵng",
            "Phường Lê Chân, Hải Phòng"
    };

    private CandidateDstsCsvSamples() {
    }

    public static byte[] templateCsvBytes() {
        return withBom(HEADER + "\r\n" + TEMPLATE_ROWS + "\r\n");
    }

    public static byte[] testCsvBytes() {
        return withBom(HEADER + "\r\n" + TEST_ROWS + "\r\n");
    }

    /** 55 thí sinh hạng B1 (SBD 051–105) — test import + phân trang allocation (mặc định 50/trang). */
    public static byte[] bulkTestCsvBytes() {
        return withBom(HEADER + "\r\n" + buildBulkRows(55) + "\r\n");
    }

    public static String bulkTestCsvText() {
        return HEADER + "\n" + buildBulkRows(55).replace("\r\n", "\n") + "\n";
    }

    private static String buildBulkRows(int count) {
        StringBuilder sb = new StringBuilder(count * 120);
        for (int i = 1; i <= count; i++) {
            if (i > 1) {
                sb.append("\r\n");
            }
            int sbd = 50 + i;
            String sbdStr = String.format("%03d", sbd);
            String ho = HO[i % HO.length];
            String dem = DEM[(i / HO.length) % DEM.length];
            String ten = TEN[i % TEN.length];
            String fullName = ho + " " + dem + " " + ten;
            String cccd = String.format("0384%08d", 100000 + i);
            int day = (i % 27) + 1;
            int month = (i % 12) + 1;
            int year = 1995 + (i % 10);
            String dob = String.format("%02d/%02d/%04d", day, month, year);
            String sex = i % 3 == 0 ? "Nữ" : "Nam";
            String address = ADDRESSES[i % ADDRESSES.length];
            String sh = SH_CONTENTS[i % SH_CONTENTS.length];
            String phone = String.format("09%02d%07d", (i % 90) + 10, 1000000 + i);
            String email = "thisinh" + sbdStr + "@dststest.local";
            sb.append(sbdStr).append(',')
                    .append(fullName).append(',')
                    .append(cccd).append(',')
                    .append(dob).append(',')
                    .append(sex).append(',')
                    .append('"').append(address).append('"').append(',')
                    .append("B1").append(',')
                    .append(sh).append(',')
                    .append(phone).append(',')
                    .append(email);
        }
        return sb.toString();
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
