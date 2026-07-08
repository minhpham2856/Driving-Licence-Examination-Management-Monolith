package service;

import dto.examstaff.ReportPaymentSummaryDTO;

public interface ReportFeeQueryService {

    ReportPaymentSummaryDTO findPaymentSummary(int candidateId);
}
