package examstaff.dao;

import examstaff.model.Fee;
import java.util.List;

public interface FeeDAO {
    List<Fee> getActiveFees();

    List<Fee> getProcedureFees(String licenseCode, boolean requiresRoadTest);

    double sumProcedureFees(String licenseCode, boolean requiresRoadTest);

    List<Fee> getFeesByPaymentId(int paymentId);
}
