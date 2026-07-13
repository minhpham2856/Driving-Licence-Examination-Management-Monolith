package examstaff.dao;

import shared.model.Fee;
import java.util.List;

public interface FeeDAO {

    List<Fee> getProcedureFees(String licenseCode, boolean requiresRoadTest);

    List<Fee> getFeesByPaymentId(int paymentId);
}
