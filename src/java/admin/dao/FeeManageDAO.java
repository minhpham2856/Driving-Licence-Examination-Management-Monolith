package admin.dao;

import admin.dto.FeeView;
import java.util.List;

public interface FeeManageDAO {
    List<FeeView> search(String keyword, Integer licenceId, String feeType, Boolean active);
    FeeView findById(int feeId);
    int insert(FeeView fee, Integer actorId);
    boolean update(FeeView fee, Integer actorId);
    boolean delete(int feeId);
    int countAll();
    int countByType(String feeType);
}
