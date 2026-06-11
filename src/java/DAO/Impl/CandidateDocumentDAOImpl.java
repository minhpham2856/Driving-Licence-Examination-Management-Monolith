package DAO.Impl;

import DAO.CandidateDocumentDAO;
import DBConnection.DBContext;
import Models.CandidateDocument;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CandidateDocumentDAOImpl extends DBContext implements CandidateDocumentDAO {

    @Override
    public int countByPersonIdAndType(int personId, String documentType) {
        String sql = """
                select count(*)
                from CandidateDocument
                where personId = ? and documentType = ?
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, personId);
                ps.setString(2, documentType);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public Map<String, Integer> countGroupedByType(int personId) {
        String sql = """
                select documentType, count(*) as docCount
                from CandidateDocument
                where personId = ?
                group by documentType
                """;

        Map<String, Integer> counts = new HashMap<>();

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, personId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        counts.put(rs.getString("documentType"), rs.getInt("docCount"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return counts;
    }

    @Override
    public List<CandidateDocument> findIdCardsByPersonId(int personId) {
        String sql = """
                select id, personId, documentType, documentUrl, expiryDate, createdAt
                from CandidateDocument
                where personId = ? and documentType = 'ID_Card'
                order by id asc
                """;

        List<CandidateDocument> documents = new ArrayList<>();

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, personId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        documents.add(mapDocument(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return documents;
    }

    @Override
    public CandidateDocument findLatestByPersonIdAndType(int personId, String documentType) {
        String sql = """
                select top 1 id, personId, documentType, documentUrl, expiryDate, createdAt
                from CandidateDocument
                where personId = ? and documentType = ?
                order by id desc
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, personId);
                ps.setString(2, documentType);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapDocument(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean insert(CandidateDocument document) {
        String sql = """
                insert into CandidateDocument (personId, documentType, documentUrl, expiryDate)
                values (?, ?, ?, ?)
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, document.getPersonId());
                ps.setString(2, document.getDocumentType());
                ps.setString(3, document.getDocumentUrl());

                if (document.getExpiryDate() == null) {
                    ps.setNull(4, Types.DATE);
                } else {
                    ps.setDate(4, document.getExpiryDate());
                }

                int affectedRows = ps.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            document.setId(keys.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "delete from CandidateDocument where id = ?";

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteByPersonIdAndType(int personId, String documentType) {
        String sql = "delete from CandidateDocument where personId = ? and documentType = ?";

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, personId);
                ps.setString(2, documentType);
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private CandidateDocument mapDocument(ResultSet rs) throws SQLException {
        CandidateDocument document = new CandidateDocument();
        document.setId(rs.getInt("id"));
        document.setPersonId(rs.getInt("personId"));
        document.setDocumentType(rs.getString("documentType"));
        document.setDocumentUrl(rs.getString("documentUrl"));
        document.setExpiryDate(rs.getDate("expiryDate"));
        document.setCreatedAt(rs.getTimestamp("createdAt"));
        return document;
    }
}
