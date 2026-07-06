package util;

import jakarta.servlet.ServletContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ExamSessionState {

    private static final String ATTR_PREFIX = "examSessionState.";

    private ExamSessionState() {
    }

    public static void clearPresent(ServletContext ctx, int sessionId, int sbd) {
        presentSet(ctx, sessionId).remove(sbd);
    }

    public static void markPresent(ServletContext ctx, int sessionId, int sbd) {
        presentSet(ctx, sessionId).add(sbd);
        procedureSet(ctx, sessionId).remove(sbd);
    }

    public static boolean isPresent(ServletContext ctx, int sessionId, int sbd) {
        return presentSet(ctx, sessionId).contains(sbd);
    }

    public static void sendToProcedure(ServletContext ctx, int sessionId, int sbd) {
        procedureSet(ctx, sessionId).add(sbd);
        presentSet(ctx, sessionId).remove(sbd);
        for (ExamQueue.Lane lane : ExamQueue.Lane.values()) {
            ExamQueue.remove(lane, sbd);
        }
    }

    public static boolean isInProcedureQueue(ServletContext ctx, int sessionId, int sbd) {
        return procedureSet(ctx, sessionId).contains(sbd);
    }

    public static Set<Integer> getProcedureQueue(ServletContext ctx, int sessionId) {
        return Collections.unmodifiableSet(new HashSet<>(procedureSet(ctx, sessionId)));
    }

    public static void setFaceMatchRate(ServletContext ctx, int sessionId, int sbd, double rate) {
        faceRates(ctx, sessionId).put(sbd, rate);
    }

    public static Double getFaceMatchRate(ServletContext ctx, int sessionId, int sbd) {
        return faceRates(ctx, sessionId).get(sbd);
    }

    public static void saveDraftAnswers(ServletContext ctx, int sessionId, int sbd,
            Map<Integer, String> answers) {
        draftAnswers(ctx).put(draftKey(sessionId, sbd), new LinkedHashMap<>(answers));
    }

    public static Map<Integer, String> getDraftAnswers(ServletContext ctx, int sessionId, int sbd) {
        Map<Integer, String> saved = draftAnswers(ctx).get(draftKey(sessionId, sbd));
        if (saved == null) {
            return new HashMap<>();
        }
        return new LinkedHashMap<>(saved);
    }

    public static void setSectionPassed(ServletContext ctx, int sessionId, int sbd, boolean passed) {
        sectionPass(ctx).put(draftKey(sessionId, sbd), passed);
    }

    public static Boolean getSectionPassed(ServletContext ctx, int sessionId, int sbd) {
        return sectionPass(ctx).get(draftKey(sessionId, sbd));
    }

    public static void removeCandidate(ServletContext ctx, int sessionId, int sbd) {
        presentSet(ctx, sessionId).remove(sbd);
        procedureSet(ctx, sessionId).remove(sbd);
        faceRates(ctx, sessionId).remove(sbd);
        draftAnswers(ctx).remove(draftKey(sessionId, sbd));
        sectionPass(ctx).remove(draftKey(sessionId, sbd));
    }

    private static String draftKey(int sessionId, int sbd) {
        return sessionId + "-" + sbd;
    }

    @SuppressWarnings("unchecked")
    private static Set<Integer> presentSet(ServletContext ctx, int sessionId) {
        return (Set<Integer>) bucket(ctx, sessionId, "present");
    }

    @SuppressWarnings("unchecked")
    private static Set<Integer> procedureSet(ServletContext ctx, int sessionId) {
        return (Set<Integer>) bucket(ctx, sessionId, "procedure");
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, Double> faceRates(ServletContext ctx, int sessionId) {
        return (Map<Integer, Double>) bucket(ctx, sessionId, "face");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Map<Integer, String>> draftAnswers(ServletContext ctx) {
        String key = ATTR_PREFIX + "draftAnswers";
        Map<String, Map<Integer, String>> map = (Map<String, Map<Integer, String>>) ctx.getAttribute(key);
        if (map == null) {
            map = new HashMap<>();
            ctx.setAttribute(key, map);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Boolean> sectionPass(ServletContext ctx) {
        String key = ATTR_PREFIX + "sectionPass";
        Map<String, Boolean> map = (Map<String, Boolean>) ctx.getAttribute(key);
        if (map == null) {
            map = new HashMap<>();
            ctx.setAttribute(key, map);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private static Object bucket(ServletContext ctx, int sessionId, String name) {
        String key = ATTR_PREFIX + sessionId + "." + name;
        Object value = ctx.getAttribute(key);
        if (value == null) {
            if ("face".equals(name)) {
                value = new HashMap<Integer, Double>();
            } else {
                value = new HashSet<Integer>();
            }
            ctx.setAttribute(key, value);
        }
        return value;
    }
}
