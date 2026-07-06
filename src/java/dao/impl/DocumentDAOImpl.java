package dao.impl;

import dao.DocumentDAO;
import dbconnection.DBContext;
import model.Document;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DocumentDAOImpl extends DBContext implements DocumentDAO {

    @Override
    public boolean upsertByProfileAndType(Document document) {
        String sql = """
                IF EXISTS (SELECT 1 FROM Document WHERE ProfileId = ? AND DocumentType = ?)
                    UPDATE Document SET DocumentUrl = ?, Notes = ?
                    WHERE ProfileId = ? AND DocumentType = ?
                ELSE
                    INSERT INTO Document (DocumentType, DocumentUrl, Notes, ProfileId)
                    VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, document.getProfileId());
            ps.setString(2, document.getDocumentType());
            ps.setString(3, document.getDocumentUrl());
            ps.setString(4, document.getNotes());
            ps.setInt(5, document.getProfileId());
            ps.setString(6, document.getDocumentType());
            ps.setString(7, document.getDocumentType());
            ps.setString(8, document.getDocumentUrl());
            ps.setString(9, document.getNotes());
            ps.setInt(10, document.getProfileId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
