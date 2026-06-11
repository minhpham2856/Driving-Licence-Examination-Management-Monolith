package DAO;

import Models.FeeBreakdownItem;
import java.math.BigDecimal;
import java.util.List;

public interface ExamSectionDAO {

    BigDecimal sumActiveFeesByLicenseTypeId(int licenseTypeId);

    List<FeeBreakdownItem> findFeeLinesByLicenseTypeId(int licenseTypeId);
}
