package dao;

import model.Document;

public interface DocumentDAO {

    boolean upsertByProfileAndType(Document document);
}
