<%@ page isErrorPage="true" %>
<c:set var="code" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<c:set var="ex" value="${requestScope['jakarta.servlet.error.exception']}"/>
<c:set var="isDomain" value="${not empty ex and ex['class'].simpleName == 'DomainException'}"/>
<c:choose>
  <c:when test="${isDomain}">
    <c:set var="title" value="Không thực hiện được"/>
    <c:set var="message" value="${ex.message}"/>
  </c:when>
  <c:when test="${code == 403}">
    <c:set var="title" value="Không có quyền truy cập"/>
    <c:set var="message" value="${not empty errorMessage ? errorMessage : requestScope['jakarta.servlet.error.message']}"/>
  </c:when>
  <c:when test="${code == 404}">
    <c:set var="title" value="Không tìm thấy trang"/>
    <c:set var="message" value="Trang hoặc sản phẩm bạn tìm không tồn tại hoặc đã bị gỡ."/>
  </c:when>
  <c:otherwise>
    <c:set var="title" value="Đã có lỗi xảy ra"/>
    <c:set var="message" value="Hệ thống gặp sự cố khi xử lý yêu cầu. Vui lòng thử lại sau."/>
  </c:otherwise>
</c:choose>
<c:set var="pageTitle" value="${title}"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="site-section">
  <div class="container min-vh-50">
    <div class="row">
      <div class="col-md-12 text-center">
        <span class="icon-warning display-3 text-warning"></span>
        <h2 class="display-4 text-black"><c:out value="${title}"/></h2>
        <p class="lead mb-5"><c:out value="${message}"/></p>
        <p>
          <a href="javascript:history.back()" class="btn btn-sm btn-outline-primary">Quay lại</a>
          <a href="${ctx}/" class="btn btn-sm btn-primary">Về trang chủ</a>
        </p>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
