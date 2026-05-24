<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>

<jsp:useBean id="now" class="java.util.Date" scope="page" />

<footer class="site-footer" role="contentinfo" data-node-id="1:1206">
    <div class="site-footer__container" data-node-id="1:1207">
        <div class="site-footer__brand" data-node-id="1:1208">
            <p class="site-footer__brand-text" data-node-id="1:1209">Lái Vui</p>
        </div>

        <nav class="site-footer__nav" aria-label="Liên kết chân trang" data-node-id="1:1210">
            <a href="#" class="site-footer__link" data-node-id="1:1211">Liên hệ</a>
            <a href="#" class="site-footer__link" data-node-id="1:1213">Điều khoản</a>
            <a href="#" class="site-footer__link" data-node-id="1:1215">Chính sách bảo mật</a>
            <a href="#" class="site-footer__link" data-node-id="1:1217">Câu hỏi thường gặp</a>
        </nav>

        <hr class="site-footer__divider" aria-hidden="true" data-node-id="1:1219">

        <p class="site-footer__copyright" data-node-id="1:1220">
            &copy; <fmt:formatDate value="${now}" pattern="yyyy" /> Lái Vui
        </p>
    </div>
</footer>

<c:if test="${empty param.standalone or param.standalone ne 'false'}">
</body>
</html>
</c:if>
