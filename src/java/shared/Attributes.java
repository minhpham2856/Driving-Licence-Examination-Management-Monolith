package shared;

public final class Attributes {

    private Attributes() {
    }

    // Session attributes shared across modules
    public static final class Session {

        public static final String USER = "user";
        public static final String USER_PROFILE = "userProfile";
        public static final String SUCCESS_MESSAGE = "successMessage";
        public static final String ERROR_MESSAGE = "errorMessage";
        public static final String REGISTRATION_USERNAME = "registrationUsername";
        public static final String REGISTRATION_PASSWORD = "registrationPassword";
    }

    // Request attributes for page data and one-hop flash
    public static final class Request {

        public static final String ERROR = "error";
        public static final String SUCCESS = "success";
        public static final String MESSAGE = "message";
        public static final String MESSAGE_TYPE = "messageType";
        public static final String LICENCES = "licences";
        public static final String SEARCH_QUERY = "searchQuery";
        public static final String SORT_BY = "sortBy";
        public static final String SORT_DIR = "sortDir";
        public static final String CANDIDATE = "candidate";
        public static final String CANDIDATES = "candidates";
        public static final String PAGE_URL = "pageUrl";
        public static final String BACK_URL = "backUrl";
        public static final String ERROR_MSG = "errorMsg";
        public static final String ALERT_MSG = "alertMsg";
        public static final String ACCOUNT_USER = "accountUser";
        public static final String ACCOUNT_PROFILE = "accountProfile";
        public static final String ACCOUNT_SHELL = "accountShell";
        public static final String ACCOUNT_PROFILE_PATH = "accountProfilePath";
        public static final String ACCOUNT_CHANGE_PASSWORD_PATH = "accountChangePasswordPath";
    }

    // Examiner session and request context
    public static final class Examiner {

        public static final String SCHEDULE = "examinerSchedule";
        public static final String ACTIVE_EXAM_ID = "activeExamId";
        public static final String EXAM_SECTION = "examSection";
        public static final String HAS_ACTIVE_EXAM = "examinerHasActiveExam";
        public static final String EXAM_MESSAGE = "examinerExamMessage";
        public static final String IS_THEORY = "isTheory";
        public static final String EXAM_SECTION_NAME = "examSectionName";
        public static final String SECTION_THEORY = "examinerSectionTheory";
    }

    // Exam staff session and request context
    public static final class ExamStaff {

        public static final String SELECTED_EXAM_ID = "selectedExamId";
        public static final String CURRENT_EXAM = "currentExam";
        public static final String CANDIDATE_QUEUE = "candidateQueue";
        public static final String ACTIVE_CALL_QUEUE = "activeCallQueue";
        public static final String PROCEDURE_DONE_CANDIDATES = "procedureDoneCandidates";
        public static final String ALL_EXAMS = "allExams";
        public static final String EXAM_OPTIONS = "examOptions";
        public static final String LOADED_EXAM_ID = "examStaffLoadedExamId";
        public static final String LAST_LOADED_EXAM_ID = "lastLoadedExamId";

        public static final String CALL_QUEUE_ORDER = "callQueueOrder";
        public static final String CALL_QUEUE_ORDER_EXAM_ID = "callQueueOrderExamId";
        public static final String CALLING_SBD = "callingSbd";
        public static final String LAST_SELECTED_SBD = "lastSelectedSbd";
        public static final String PERMANENT_ABSENTS = "permanentAbsents";

        public static final String PROCEDURE_STEP = "procedureStep";
        public static final String PROCEDURE_JUST_PAID = "procedureJustPaid";
        public static final String PROCEDURE_JUST_PAID_SBD = "procedureJustPaidSbd";

        public static final String SHIFT_PAUSED = "shiftPaused";
        public static final String SHIFT_ENDED = "shiftEnded";
        public static final String EXAM_CONTROL_MSG = "examControlMsg";
        public static final String EXAM_CONTROL_ERROR = "examControlError";
        public static final String FLAG_TRUE = "true";

        // Dropdown phân công sát hạch viên (list map areaId/areaName/...).
        public static final String AREA_ASSIGN_OPTIONS = "areaAssignOptions";
        public static final String EXAM_ASSIGNMENTS = "examAssignments";
        public static final String ALL_EXAMINERS = "allExaminers";
        public static final String AVAILABLE_EXAMINERS = "availableExaminers";
        public static final String BUSY_EXAMINERS = "busyExaminers";

        // Phòng LT / sân TH đã có sát hạch viên - trang phân bổ thí sinh.
        public static final String ACTIVE_THEORY_ROOMS = "activeTheoryRooms";
        public static final String ACTIVE_PRACTICAL_AREAS = "activePracticalAreas";
        public static final String ALLOCATION_AREA_FILTER = "allocationAreaFilter";
        public static final String ALLOCATION_ACTIVE_EXAM_ID = "allocationActiveExamId";
    }
}
