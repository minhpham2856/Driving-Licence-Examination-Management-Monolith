<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    </main>
</div>
<c:if test="${not empty param.extraScript}">
<script src="${pageContext.request.contextPath}${param.extraScript}" charset="UTF-8"></script>
</c:if>
</body>
</html>
