package controller.examiner;


import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

 // Manages the in-memory score-entry queue stored in the HTTP session.
public final class ExaminerScoreEntryQueue {

    // Private constructor prevents instantiation — all methods are static
    private ExaminerScoreEntryQueue() {
    }

    // Builds the session attribute key for the ordered SBD queue list
    private static String queueKey(int sessionId) {
        return "examinerScoreQueue_" + sessionId;
    }

    // Builds the session attribute key for the currently active (displayed) SBD
    private static String activeKey(int sessionId) {
        return "examinerScoreActiveSbd_" + sessionId;
    }

    // Builds the session attribute key for the most recently called SBD
    private static String calledKey(int sessionId) {
        return "examinerScoreCalledSbd_" + sessionId;
    }

         // Returns a copy of the current queue for the given session.
    @SuppressWarnings("unchecked")
    public static List<String> getQueue(HttpSession session, int sessionId) {
        // Return empty immutable list if session is null (e.g. during unit testing)
        if (session == null) {
            return List.of();
        }
        // Look up the queue attribute using the session-namespaced key
        Object value = session.getAttribute(queueKey(sessionId));
        // If a list exists in session, create a defensive copy with null filtering
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            // Build a new mutable list, converting each element to String and skipping nulls
            List<String> copy = new ArrayList<>();
            for (Object item : list) {
                if (item != null) {
                    copy.add(String.valueOf(item));
                }
            }
            return copy;
        }
        // No queue exists yet — return a fresh empty mutable list
        return new ArrayList<>();
    }

         // Merges the database-eligible SBD list into the existing session queue.
    public static void syncQueue(HttpSession session, int sessionId, List<String> eligibleSbds) {
        // Guard: do nothing if session or eligible list is null
        if (session == null || eligibleSbds == null) {
            return;
        }
        // Use a LinkedHashSet for O(1) lookup while preserving insertion order
        Set<String> eligible = new LinkedHashSet<>(eligibleSbds);
        // Get the current queue snapshot from the session
        List<String> current = getQueue(session, sessionId);
        // Build the merged queue: existing order first, then new SBDs appended
        List<String> merged = new ArrayList<>();
        for (String sbd : current) {
            // Keep only SBDs that are still eligible according to the DB
            if (eligible.contains(sbd)) {
                merged.add(sbd);
                // Remove from eligible set so remaining entries are "new" additions
                eligible.remove(sbd);
            }
        }
        // Append any newly-eligible SBDs that were not in the existing queue
        merged.addAll(eligible);
        // Store the reconciled queue back into the session
        session.setAttribute(queueKey(sessionId), merged);

        // Clear the active SBD if it was removed from the queue (no longer eligible)
        String active = getActiveSbd(session, sessionId);
        if (active != null && !merged.contains(active)) {
            session.removeAttribute(activeKey(sessionId));
        }
        // Clear the called SBD if it was removed from the queue (no longer eligible)
        String called = getCalledSbd(session, sessionId);
        if (called != null && !merged.contains(called)) {
            session.removeAttribute(calledKey(sessionId));
        }
    }

    // Returns the currently active SBD in the score-entry panel, or null.
    public static String getActiveSbd(HttpSession session, int sessionId) {
        // Return null if session is not available
        if (session == null) {
            return null;
        }
        // Read the attribute and convert to String (returns null if not set)
        Object value = session.getAttribute(activeKey(sessionId));
        return value != null ? String.valueOf(value) : null;
    }

    // Sets the active SBD, or removes the attribute if sbd is null/blank.
    public static void setActiveSbd(HttpSession session, int sessionId, String sbd) {
        // Guard: do nothing if session is null
        if (session == null) {
            return;
        }
        // Remove the attribute if sbd is null or blank (no active candidate)
        if (sbd == null || sbd.isBlank()) {
            session.removeAttribute(activeKey(sessionId));
            return;
        }
        // Store the trimmed SBD as the active candidate
        session.setAttribute(activeKey(sessionId), sbd.trim());
    }

    // Returns the most recently called SBD, or null.
    public static String getCalledSbd(HttpSession session, int sessionId) {
        // Return null if session is not available
        if (session == null) {
            return null;
        }
        // Read the attribute and convert to String (returns null if not set)
        Object value = session.getAttribute(calledKey(sessionId));
        return value != null ? String.valueOf(value) : null;
    }

    // Sets the called SBD, or removes the attribute if sbd is null/blank.
    public static void setCalledSbd(HttpSession session, int sessionId, String sbd) {
        // Guard: do nothing if session is null
        if (session == null) {
            return;
        }
        // Remove the attribute if sbd is null or blank (no pending call)
        if (sbd == null || sbd.isBlank()) {
            session.removeAttribute(calledKey(sessionId));
            return;
        }
        // Store the trimmed SBD as the called candidate
        session.setAttribute(calledKey(sessionId), sbd.trim());
    }

         // Returns the first SBD in the queue, or null if the queue is empty.
    public static String firstInQueue(HttpSession session, int sessionId) {
        List<String> queue = getQueue(session, sessionId);
        // Return null if the queue is empty, otherwise return the first element
        return queue.isEmpty() ? null : queue.get(0);
    }

         // Returns the next SBD in the queue that comes after the given SBD.
    public static String nextInQueueAfter(HttpSession session, int sessionId, String sbd) {
        // Get the current queue snapshot
        List<String> queue = getQueue(session, sessionId);
        // Return null if the queue is empty (no candidates at all)
        if (queue.isEmpty()) {
            return null;
        }
        // If no SBD is specified, return the queue head as the "next" candidate
        if (sbd == null || sbd.isBlank()) {
            return queue.get(0);
        }
        // Normalize the SBD for consistent lookup
        String normalized = sbd.trim();
        // Find the position of the current SBD in the queue
        int idx = queue.indexOf(normalized);
        // If the SBD is not in the queue, fall back to the first element
        if (idx < 0) {
            return queue.get(0);
        }
        // If there is a next element after the current position, return it
        if (idx + 1 < queue.size()) {
            return queue.get(idx + 1);
        }
        // Current SBD is the last in the queue — no next candidate (no wrapping)
        return null;
    }

         // Moves the given SBD to the bottom of the queue.
    public static String moveToBottom(HttpSession session, int sessionId, String sbd) {
        // Guard: if session or SBD is invalid, just return the current head
        if (session == null || sbd == null || sbd.isBlank()) {
            return firstInQueue(session, sessionId);
        }
        // Get a mutable copy of the current queue
        List<String> queue = new ArrayList<>(getQueue(session, sessionId));
        // Normalize the SBD for consistent lookup
        String normalized = sbd.trim();
        // Find the SBD's current position in the queue
        int idx = queue.indexOf(normalized);
        if (idx >= 0) {
            // Remove from current position and append at the end
            queue.remove(idx);
            queue.add(normalized);
            // Persist the reordered queue back to the session
            session.setAttribute(queueKey(sessionId), queue);
        }
        // Clear the called SBD since the queue order has changed
        session.removeAttribute(calledKey(sessionId));
        // Clear the active SBD if the moved candidate was the active one
        if (normalized.equals(getActiveSbd(session, sessionId))) {
            session.removeAttribute(activeKey(sessionId));
        }
        // Return the new head of the queue, or null if empty
        return queue.isEmpty() ? null : queue.get(0);
    }
}
