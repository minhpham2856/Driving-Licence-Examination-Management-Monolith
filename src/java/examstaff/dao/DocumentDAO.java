package examstaff.dao;

import examstaff.model.Document;

public interface DocumentDAO {

    boolean upsertByProfileAndType(Document document);
}
