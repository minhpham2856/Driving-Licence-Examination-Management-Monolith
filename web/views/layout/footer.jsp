<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
        <%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

            <jsp:useBean id="now" class="java.util.Date" scope="page" />

            <c:set var="ctx" value="${pageContext.request.contextPath}" />
            <c:set var="footerLogoUrl" value="${ctx}/assets/imgs/LOGO.jpeg" />

            <footer class="site-footer" role="contentinfo" data-node-id="1:2457">
                <div class="site-footer__container">
                    <!-- Top content area -->
                    <div class="site-footer__top">
                        <!-- Left block: Logo + Brand statement -->
                        <div class="site-footer__brand-group">
                            <div class="site-footer__logo-wrap">
                                <img src="${footerLogoUrl}" alt="Lái Vui" class="site-footer__logo-img">
                                <span class="site-footer__brand-title">Trung tâm sát hạch GPLX Lái Vui</span>
                            </div>
                            <p class="site-footer__brand-desc">
                                Nền tảng đăng ký thi sát hạch GPLX hiện đại, đơn giản hoá quy trình, theo dõi kết quả
                                thuận tiện.
                            </p>
                        </div>

                        <!-- Right block: Links grouped in columns -->
                        <div class="site-footer__links-group">
                            <!-- Column 1: Dịch vụ -->
                            <div class="site-footer__col">
                                <h4 class="site-footer__col-title">Dịch vụ</h4>
                                <nav class="site-footer__col-nav" aria-label="Liên kết Dịch vụ">
                                    <a href="license-grades.jsp" class="site-footer__link">Đăng ký thi</a>
                                    <a href="license-grades.jsp" class="site-footer__link">Các hạng bằng</a>
                                    <a href="#" class="site-footer__link">Liên hệ đăng ký học</a>
                                </nav>
                            </div>

                            <!-- Column 2: Hỗ trợ -->
                            <div class="site-footer__col">
                                <h4 class="site-footer__col-title">Hỗ trợ</h4>
                                <nav class="site-footer__col-nav" aria-label="Liên kết Hỗ trợ">
                                    <a href="#" class="site-footer__link">Câu hỏi thường gặp</a>
                                    <a href="#" class="site-footer__link">Liên hệ</a>
                                    <a href="#" class="site-footer__link">Trung tâm trợ giúp</a>
                                </nav>
                            </div>
                        </div>
                    </div>

                    <!-- Bottom copyright area -->
                    <div class="site-footer__bottom">
                        <p class="site-footer__copyright">
                            &copy;
                            <fmt:formatDate value="${now}" pattern="yyyy" /> Lái Vui - Trung tâm đào tạo và thi sát hạch
                            GPLX hiện đại.
                        </p>

                        <div class="site-footer__bottom-links">
                            <a href="#" class="site-footer__bottom-link">Điều khoản</a>
                            <a href="#" class="site-footer__bottom-link">Chính sách bảo mật</a>
                        </div>
                    </div>
                </div>
            </footer>

            <c:if test="${empty param.standalone or param.standalone ne 'false'}">
                </body>

                </html>
            </c:if>
