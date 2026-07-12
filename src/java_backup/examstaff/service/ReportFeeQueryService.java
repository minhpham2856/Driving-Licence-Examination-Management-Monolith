package examstaff.service;

import examstaff.dto.ReportPaymentSummaryDTO;

public interface ReportFeeQueryService {

    ReportPaymentSummaryDTO findPaymentSummary(int candidateId);
}
