package examstaff.service.impl;

import examstaff.util.AllocationPassRules;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffQueueRefreshInput;
import examstaff.service.CandidateQueueQueryService;
import examstaff.service.CandidateQueueService;
import examstaff.service.ExamStaffSessionQueryService;
import examstaff.util.CallQueueRules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CandidateQueueServiceImpl implements CandidateQueueService {

    private final CandidateQueueQueryService queueQuery;
    private final ExamStaffSessionQueryService sessionQuery;

    public CandidateQueueServiceImpl() {
        this(new CandidateQueueQueryServiceImpl(), new ExamStaffSessionQueryServiceImpl());
    }

    public CandidateQueueServiceImpl(CandidateQueueQueryService queueQuery,
            ExamStaffSessionQueryService sessionQuery) {
        this.queueQuery = queueQuery;
        this.sessionQuery = sessionQuery;
    }

    @Override
    public CandidateQueueSnapshotDTO refreshQueue(ExamStaffQueueRefreshInput input) {
        CandidateQueueSnapshotDTO snapshot = new CandidateQueueSnapshotDTO();
        snapshot.setFullQueue(new ArrayList<>());
        if (input == null) {
            return snapshot;
        }

        int examId = input.getExamId();
        List<ExamSummaryDTO> allSessions = input.getAllSessions();
        if (allSessions == null || allSessions.isEmpty()) {
            allSessions = sessionQuery.listAllSessions();
        }

        if (examId <= 0 && input.getSelectedExamId() != null && input.getSelectedExamId() > 0) {
            examId = input.getSelectedExamId();
        }
        if (examId <= 0 && allSessions != null && !allSessions.isEmpty()) {
            examId = examstaff.util.ExamStaffSessionRules.resolveDefaultExamId(allSessions);
        }
        if (examId <= 0) {
            return snapshot;
        }

        Integer selected = input.getSelectedExamId();
        if (selected != null && selected > 0) {
            ExamSummaryDTO pickedSession = resolveSession(selected, allSessions);
            if (pickedSession != null && (pickedSession.getExamId() == examId || pickedSession.getId() == examId)) {
                examId = selected;
            }
        }

        List<ExamRegistrationDTO> qList = loadCandidates(examId, examId, allSessions);
        if (input.getWebRoot() != null) {
            queueQuery.normalizePhotoPaths(input.getWebRoot(), qList);
        }
        for (ExamRegistrationDTO c : qList) {
            AllocationPassRules.applyToCandidate(c);
        }
        qList = applyCallQueueOrder(input.getCallQueueOrder(), input.getCallQueueOrderExamId(), examId, qList);

        snapshot.setResolvedExamId(examId);
        return buildSnapshot(qList, examId, examId);
    }

    @Override
    public CandidateQueueSnapshotDTO buildSnapshot(List<ExamRegistrationDTO> queue, int examId, int sessionId) {
        CandidateQueueSnapshotDTO snapshot = new CandidateQueueSnapshotDTO();
        List<ExamRegistrationDTO> qList = queue != null ? queue : List.of();
        snapshot.setFullQueue(qList);
        snapshot.setActiveQueue(filterPendingForCall(qList));
        snapshot.setProcedureDone(listProcedureDoneNewestFirst(qList));
        snapshot.setResolvedExamId(examId > 0 ? examId : sessionId);
        return snapshot;
    }

    @Override
    public List<ExamRegistrationDTO> filterPendingForCall(List<ExamRegistrationDTO> queue) {
        if (queue == null || queue.isEmpty()) {
            return new ArrayList<>();
        }
        List<ExamRegistrationDTO> active = new ArrayList<>();
        for (ExamRegistrationDTO c : queue) {
            if (isCallablePending(c)) {
                active.add(c);
            }
        }
        return active;
    }

    @Override
    public boolean isCallablePending(ExamRegistrationDTO candidate) {
        return CallQueueRules.isCallablePending(candidate);
    }

    @Override
    public ExamRegistrationDTO findBySbd(List<ExamRegistrationDTO> queue, String sbd) {
        return CallQueueRules.findBySbd(queue, sbd);
    }

    @Override
    public String findNextPendingSbd(List<ExamRegistrationDTO> queue, String afterSbd) {
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        if (afterSbd == null || afterSbd.isBlank()) {
            for (ExamRegistrationDTO c : queue) {
                if (isCallablePending(c)) {
                    return c.getSbd();
                }
            }
            return null;
        }
        boolean passed = false;
        for (ExamRegistrationDTO c : queue) {
            if (!passed) {
                if (afterSbd.equals(c.getSbd())) {
                    passed = true;
                }
                continue;
            }
            if (isCallablePending(c)) {
                return c.getSbd();
            }
        }
        for (ExamRegistrationDTO c : queue) {
            if (afterSbd.equals(c.getSbd())) {
                continue;
            }
            if (isCallablePending(c)) {
                return c.getSbd();
            }
        }
        return null;
    }

    @Override
    public String resolveNextCallingSbd(List<ExamRegistrationDTO> fullQueue, String afterSbd) {
        List<ExamRegistrationDTO> active = filterPendingForCall(fullQueue);
        if (active.isEmpty()) {
            return null;
        }
        if (afterSbd == null || afterSbd.isBlank()) {
            return active.get(0).getSbd();
        }
        String next = findNextPendingSbd(active, afterSbd);
        return next != null ? next : active.get(0).getSbd();
    }

    @Override
    public boolean moveCallableCandidateToFront(List<ExamRegistrationDTO> queue, String sbd) {
        if (queue == null || sbd == null || sbd.isBlank()) {
            return false;
        }
        for (int i = 0; i < queue.size(); i++) {
            ExamRegistrationDTO c = queue.get(i);
            if (!sbd.equals(c.getSbd()) || !isCallablePending(c)) {
                continue;
            }
            if (i == 0) {
                return true;
            }
            queue.remove(i);
            queue.add(0, c);
            return true;
        }
        return false;
    }

    @Override
    public boolean moveCallableCandidateToBottom(List<ExamRegistrationDTO> queue, String sbd) {
        if (queue == null || sbd == null || sbd.isBlank()) {
            return false;
        }
        for (int i = 0; i < queue.size(); i++) {
            ExamRegistrationDTO c = queue.get(i);
            if (!sbd.equals(c.getSbd()) || !isCallablePending(c)) {
                continue;
            }
            queue.remove(i);
            queue.add(c);
            return true;
        }
        return false;
    }

    @Override
    public List<ExamRegistrationDTO> listSuspendedInSession(List<ExamRegistrationDTO> queue) {
        return CallQueueRules.listSuspendedInSession(queue);
    }

    @Override
    public List<ExamRegistrationDTO> listProcedureDoneNewestFirst(List<ExamRegistrationDTO> queue) {
        if (queue == null || queue.isEmpty()) {
            return List.of();
        }
        List<ExamRegistrationDTO> done = new ArrayList<>();
        for (ExamRegistrationDTO c : queue) {
            if (c.isProcedureComplete()) {
                done.add(c);
            }
        }
        done.sort(Comparator
                .comparing(ExamRegistrationDTO::getPresentMarkedAt,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ExamRegistrationDTO::getSbd));
        return done;
    }

    @Override
    public ExamRegistrationDTO findByExamOrSession(int examId, int sessionId, String sbd) {
        if (sbd == null || sbd.isBlank()) {
            return null;
        }
        String trimmed = sbd.trim();
        try {
            if (examId > 0) {
                ExamRegistrationDTO byExam = queueQuery.findByExamIdAndSbd(examId, trimmed);
                if (byExam != null) {
                    return byExam;
                }
            }
            if (sessionId > 0) {
                for (ExamRegistrationDTO c : queueQuery.listByExamId(sessionId)) {
                    if (trimmed.equals(c.getSbd())) {
                        return c;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<ExamRegistrationDTO> loadCandidates(int examId, int sessionId, List<ExamSummaryDTO> allSessions) {
        try {
            if (examId <= 0 && sessionId > 0) {
                examId = sessionId;
            }
            if (examId > 0) {
                return new ArrayList<>(queueQuery.listByExamId(examId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private ExamSummaryDTO resolveSession(int sessionId, List<ExamSummaryDTO> allSessions) {
        ExamSummaryDTO found = examstaff.util.ExamStaffSessionRules.findExamById(allSessions, sessionId);
        if (found != null) {
            return found;
        }
        try {
            return sessionQuery.findByExamId(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<ExamRegistrationDTO> applyCallQueueOrder(List<String> order, Integer orderSessionId,
            int sessionId, List<ExamRegistrationDTO> qList) {
        if (qList == null || qList.isEmpty() || order == null || order.isEmpty()
                || orderSessionId == null || orderSessionId != sessionId) {
            return qList;
        }
        Map<String, ExamRegistrationDTO> bySbd = new LinkedHashMap<>();
        for (ExamRegistrationDTO c : qList) {
            if (c != null && c.getSbd() != null) {
                bySbd.put(c.getSbd(), c);
            }
        }
        List<ExamRegistrationDTO> reordered = new ArrayList<>();
        for (String sbd : order) {
            ExamRegistrationDTO c = bySbd.remove(sbd);
            if (c != null) {
                reordered.add(c);
            }
        }
        reordered.addAll(bySbd.values());
        return reordered;
    }
}
