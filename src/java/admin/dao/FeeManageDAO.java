package admin.dao;

import admin.model.FeeView;
import admin.model.LicenceFeeView;
import java.math.BigDecimal;
import java.util.List;

public interface FeeManageDAO {
    // Danh mục phí (Fee)
    List<FeeView> listFees(String keyword, Boolean active);
    List<FeeView> listActiveFees();
    FeeView findFee(int feeId);
    int insertFee(FeeView f);
    boolean updateFee(FeeView f);
    boolean setFeeActive(int feeId, boolean active);
    boolean deleteFee(int feeId);
    boolean feeNameExists(String feeName, int excludeId);
    int countFees();

    // Biểu phí theo hạng (Licence_Fee)
    List<LicenceFeeView> listLicenceFees(Integer licenceId, Integer feeId);
    LicenceFeeView findLicenceFee(int licenceFeeId);
    int insertLicenceFee(Integer licenceId, int feeId, BigDecimal amount);
    boolean updateLicenceFee(int licenceFeeId, Integer licenceId, int feeId, BigDecimal amount);
    boolean deleteLicenceFee(int licenceFeeId);
    boolean pairExists(Integer licenceId, int feeId, int excludeId);
    int countLicenceFees();
}
