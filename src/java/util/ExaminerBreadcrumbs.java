package util;


import jakarta.servlet.http.HttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

 // Utility for building breadcrumb navigation items in the examiner portal.
public final class ExaminerBreadcrumbs {

    // Private constructor prevents instantiation — all methods are static
    private ExaminerBreadcrumbs() {
    }

         // A single breadcrumb item with a display label, hyperlink href, and a flag
    public static final class Item {
        // Vietnamese display text shown in the breadcrumb link
        private String label;
        // Full URL path (including context path) for the breadcrumb hyperlink
        private String href;
        // True if this is a top-level (section) breadcrumb; false for sub-page crumbs
        private boolean primary;

        // Returns the display label text
        public String getLabel() { return label; }
        // Returns the hyperlink URL
        public String getHref() { return href; }
        // Returns whether this is a primary (section-level) breadcrumb
        public boolean isPrimary() { return primary; }
    }

         // Builds a breadcrumb trail for the current request.
    public static List<Item> buildItems(HttpServletRequest request) {
        // Initialise the breadcrumb list — will always have at least one item
        List<Item> items = new ArrayList<>();
        // Fallback: if request is null, return a default examiner root breadcrumb
        if (request == null) {
            items.add(primary("", "Sat hach vien", "/views/examiner/dashboard"));
            return items;
        }

        // Extract the servlet context path (e.g. "/app") for building absolute URLs
        String ctx = request.getContextPath() != null ? request.getContextPath() : "";
        // Strip the context path from the URI to get the application-relative path
        String path = stripContextPath(request);
        // Read the SBD (candidate number) parameter for detail page breadcrumbs
        String sbd = request.getParameter("sbd");
        // Build the query string suffix (e.g. "?sbd=12345") for detail page links
        String sbdQuery = sbdQuery(sbd);

        // --- Match the URI path against known examiner views (most specific first) ---

        // Candidate paper review page — deepest in the candidate-details hierarchy
        if (path.contains("/candidate-paper")) {
            items.add(primary(ctx, "Sua thong tin", "/views/examiner/candidate-details"));
            items.add(child(ctx, "chi tiet", "/views/examiner/candidate-details-edit" + sbdQuery));
            items.add(child(ctx, "de thi", "/views/examiner/candidate-paper" + sbdQuery));
            return items;
        }
        // Candidate details edit page — child of candidate details
        if (path.contains("/candidate-details-edit")) {
            items.add(primary(ctx, "Sua thong tin", "/views/examiner/candidate-details"));
            items.add(child(ctx, "chi tiet", "/views/examiner/candidate-details-edit" + sbdQuery));
            return items;
        }
        // Candidate details listing page — primary section
        if (path.contains("/candidate-details")) {
            items.add(primary(ctx, "Sua thong tin", "/views/examiner/candidate-details"));
            return items;
        }
        // Result details edit page — child of result details
        if (path.contains("/result-details-edit")) {
            items.add(primary(ctx, "Sua ket qua", "/views/examiner/result-details"));
            items.add(child(ctx, "chi tiet", "/views/examiner/result-details-edit" + sbdQuery));
            return items;
        }
        // Result details listing page — primary section
        if (path.contains("/result-details")) {
            items.add(primary(ctx, "Sua ket qua", "/views/examiner/result-details"));
            return items;
        }
        // Violation confirmation page — child of violations
        if (path.contains("/violation-confirm")) {
            items.add(primary(ctx, "Vi pham", "/views/examiner/violations"));
            items.add(child(ctx, "dinh chi", "/views/examiner/violation-confirm" + sbdQuery));
            return items;
        }
        // Violation undo page — child of violations
        if (path.contains("/violation-undo")) {
            items.add(primary(ctx, "Vi pham", "/views/examiner/violations"));
            items.add(child(ctx, "hoan tac dinh chi", "/views/examiner/violation-undo" + sbdQuery));
            return items;
        }
        // Violations listing page — primary section
        if (path.contains("/violations")) {
            items.add(primary(ctx, "Vi pham", "/views/examiner/violations"));
            return items;
        }
        // Candidate call confirmation page — child of candidate call
        if (path.contains("/confirmation")) {
            items.add(primary(ctx, "Goi thi sinh", "/views/examiner/candidate-call"));
            items.add(child(ctx, "xac nhan", "/views/examiner/confirmation" + sbdQuery));
            return items;
        }
        // Candidate call listing page — primary section
        if (path.contains("/candidate-call")) {
            items.add(primary(ctx, "Goi thi sinh", "/views/examiner/candidate-call"));
            return items;
        }
        // Score entry page — primary section
        if (path.contains("/score-entry")) {
            items.add(primary(ctx, "Nhap diem", "/views/examiner/score-entry"));
            return items;
        }
        // Device management page — primary section
        if (path.contains("/devices")) {
            items.add(primary(ctx, "Thiet bi", "/views/examiner/devices"));
            return items;
        }
        // Export page — primary section
        if (path.contains("/export")) {
            items.add(primary(ctx, "Xuat file", "/views/examiner/export"));
            return items;
        }
        // Print documents page — primary section with optional SBD child
        if (path.contains("/print-documents")) {
            items.add(primary(ctx, "In van ban", "/views/examiner/print-documents"));
            // If an SBD is specified, add a child breadcrumb for the specific document
            if (sbd != null && !sbd.isBlank()) {
                items.add(child(ctx, "bien ban ket qua thi", "/views/examiner/print-documents" + sbdQuery));
            }
            return items;
        }
        // Audit log page — primary section
        if (path.contains("/audit")) {
            items.add(primary(ctx, "Nhat ky", "/views/examiner/audit"));
            return items;
        }
        // Dashboard page — primary section (examiner home)
        if (path.contains("/dashboard")) {
            items.add(primary(ctx, "Bang dieu khien", "/views/examiner/dashboard"));
            return items;
        }

        // --- Fallback: use the headerTitle attribute if set by the controller ---
        Object headerTitle = request.getAttribute("headerTitle");
        if (headerTitle != null && !String.valueOf(headerTitle).isBlank()) {
            items.add(primary(ctx, String.valueOf(headerTitle).trim(), path));
            return items;
        }
        // Ultimate fallback: generic examiner root breadcrumb
        items.add(primary(ctx, "Sat hach vien", "/views/examiner/dashboard"));
        return items;
    }

         // Resolves the complete breadcrumb trail as a plain-text string.
    public static String resolve(HttpServletRequest request) {
        // Build the breadcrumb items for the current request
        List<Item> items = buildItems(request);
        // Return default label if no items were generated
        if (items.isEmpty()) {
            return "Sat hach vien";
        }
        // Join all item labels with " > " separator
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                text.append(" > ");
            }
            text.append(items.get(i).getLabel());
        }
        return text.toString();
    }

    // Creates a primary (section-level) breadcrumb item.
    private static Item primary(String ctx, String label, String path) {
        Item item = new Item();
        item.label = label;
        // Prepend the context path to make the href absolute
        item.href = ctx + path;
        // Mark as primary so the JSP can render it with bolder styling
        item.primary = true;
        return item;
    }

    // Creates a child (sub-page) breadcrumb item.
    private static Item child(String ctx, String label, String path) {
        Item item = new Item();
        item.label = label;
        // Prepend the context path to make the href absolute
        item.href = ctx + path;
        // Mark as non-primary so the JSP renders it with lighter styling
        item.primary = false;
        return item;
    }

    // Builds the query-string suffix for SBD filtering, or empty string if no SBD.
    private static String sbdQuery(String sbd) {
        // Return empty string if no SBD parameter was provided
        if (sbd == null || sbd.isBlank()) {
            return "";
        }
        // URL-encode the SBD value to handle special characters safely
        return "?sbd=" + URLEncoder.encode(sbd.trim(), StandardCharsets.UTF_8);
    }

    // Strips the context path from the request URI to get the application-relative path.
    private static String stripContextPath(HttpServletRequest request) {
        // Get the full request URI including context path
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        // If the URI starts with the context path, strip it off
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        // No context path to strip — return the URI as-is
        return uri;
    }
}
