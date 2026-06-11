package DAO.Impl;

import DAO.ExamSectionDAO;
import DBConnection.DBContext;
import Models.FeeBreakdownItem;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExamSectionDAOImpl extends DBContext implements ExamSectionDAO {

    @Override
    public BigDecimal sumActiveFeesByLicenseTypeId(int licenseTypeId) {
        String sql = """
                select coalesce(sum(examFee), 0)
                from ExamSection
                where licenseTypeId = ? and isActive = 1
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, licenseTypeId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBigDecimal(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    @Override
    public List<FeeBreakdownItem> findFeeLinesByLicenseTypeId(int licenseTypeId) {
        String sql = """
                select et.typeName, sec.examFee
                from ExamSection sec
                join ExamType et on sec.examTypeId = et.id
                where sec.licenseTypeId = ? and sec.isActive = 1
                order by et.id
                """;

        List<FeeBreakdownItem> items = new ArrayList<>();

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, licenseTypeId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String typeName = rs.getString("typeName");
                        items.add(new FeeBreakdownItem(
                                translateExamType(typeName),
                                rs.getBigDecimal("examFee")));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    private String translateExamType(String typeName) {
        if (typeName == null) {
            return "Phí thi";
        }
        return switch (typeName) {
            case "Theory" -> "Phí thi lý thuyết";
            case "Practical" -> "Phí thi thực hành";
            case "RoadLayout" -> "Phí thi sa hình";
            case "OnRoad" -> "Phí thi đường trường";
            default -> "Phí thi " + typeName;
        };
    }
}
