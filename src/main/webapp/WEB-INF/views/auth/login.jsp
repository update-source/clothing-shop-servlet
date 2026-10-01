<c:set var="pageTitle" value="Đăng nhập"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="bg-light py-3">
  <div class="container">
    <div class="row">
      <div class="col-md-12 mb-0"><a href="${ctx}/">Trang chủ</a> <span class="mx-2 mb-0">/</span> <strong class="text-black">Đăng nhập</strong></div>
    </div>
  </div>
</div>

<div class="site-section">
  <div class="container">
    <div class="row justify-content-center">
      <div class="col-md-6 col-lg-5">
        <h2 class="h3 mb-3 text-black">Đăng nhập</h2>
        <form action="${ctx}/login" method="post" class="p-3 p-lg-5 border">
          <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
          <input type="hidden" name="next" value="${fn:escapeXml(param.next)}">
          <c:if test="${not empty error}">
            <div class="alert alert-danger"><c:out value="${error}"/></div>
          </c:if>
          <div class="form-group">
            <label for="username" class="text-black form-required">Tên đăng nhập</label>
            <input type="text" class="form-control" id="username" name="username" required autofocus
                   value="${fn:escapeXml(param.username)}" autocomplete="username">
          </div>
          <div class="form-group">
            <label for="password" class="text-black form-required">Mật khẩu</label>
            <input type="password" class="form-control" id="password" name="password" required
                   autocomplete="current-password">
          </div>
          <div class="form-group mb-0">
            <button type="submit" class="btn btn-primary btn-lg btn-block">Đăng nhập</button>
          </div>
          <p class="mt-4 mb-0 text-center">
            Chưa có tài khoản?
            <a href="${ctx}/register${empty param.next ? '' : '?next='.concat(f:url(param.next))}">Đăng ký ngay</a>
          </p>
        </form>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
