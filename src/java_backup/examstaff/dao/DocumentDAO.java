package examstaff.dao;

import shared.model.Document;

public interface DocumentDAO {

    boolean upsertByProfileAndType(Document document);
}

