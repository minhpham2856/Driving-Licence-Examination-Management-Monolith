package dto.payload;

public class SaveEntityData {

    private final int entityId;

    public SaveEntityData(int entityId) {
        this.entityId = entityId;
    }

    public int getEntityId() {
        return entityId;
    }
}
