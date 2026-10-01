<c:set var="pageTitle" value="Đăng ký"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="bg-light py-3">
  <div class="container">
    <div class="row">
      <div class="col-md-12 mb-0"><a href="${ctx}/">Trang chủ</a> <span class="mx-2 mb-0">/</span> <strong class="text-black">Đăng ký</strong></div>
    </div>
  </div>
</div>

<div class="site-section">
  <div class="container">
    <div class="row justify-content-center">
      <div class="col-md-9 col-lg-7">
        <h2 class="h3 mb-3 text-black">Tạo tài khoản khách hàng</h2>
        <form action="${ctx}/register" method="post" class="p-3 p-lg-5 border">
          <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
          <input type="hidden" name="next" value="${fn:escapeXml(param.next)}">
          <c:if test="${not empty error}">
            <div class="alert alert-danger"><c:out value="${error}"/></div>
          </c:if>
          <div class="form-group row">
            <div class="col-md-6">
              <label for="username" class="text-black form-required">Tên đăng nhập</label>
              <input type="text" class="form-control" id="username" name="username" required minlength="4" maxlength="30"
                     pattern="[A-Za-z0-9_.]{4,30}" value="${fn:escapeXml(param.username)}" autocomplete="username">
              <small class="text-muted">4–30 ký tự chữ, số, dấu chấm, gạch dưới</small>
            </div>
            <div class="col-md-6">
              <label for="fullName" class="text-black form-required">Họ và tên</label>
              <input type="text" class="form-control" id="fullName" name="fullName" required maxlength="100"
                     value="${fn:escapeXml(param.fullName)}">
            </div>
          </div>
          <div class="form-group row">
            <div class="col-md-6">
              <label for="email" class="text-black form-required">Email</label>
              <input type="email" class="form-control" id="email" name="email" required maxlength="100"
                     value="${fn:escapeXml(param.email)}" autocomplete="email">
            </div>
            <div class="col-md-6">
              <label for="phone" class="text-black form-required">Số điện thoại</label>
              <input type="tel" class="form-control" id="phone" name="phone" required pattern="(0|\+84)[0-9]{9,10}"
                     value="${fn:escapeXml(param.phone)}" placeholder="09xxxxxxxx">
            </div>
          </div>
          <div class="form-group row">
            <div class="col-md-6">
              <label for="gender" class="text-black">Giới tính</label>
              <select id="gender" name="gender" class="form-control">
                <option value="">-- Chọn --</option>
                <c:forEach items="${genders}" var="g">
                  <option value="${g}" ${param.gender == g.name() ? 'selected' : ''}>${g.label}</option>
                </c:forEach>
              </select>
            </div>
            <div class="col-md-6">
              <label for="dob" class="text-black">Ngày sinh</label>
              <input type="date" class="form-control" id="dob" name="dob" value="${fn:escapeXml(param.dob)}">
            </div>
          </div>
          <div class="form-group row">
            <div class="col-md-6">
              <label for="password" class="text-black form-required">Mật khẩu</label>
              <input type="password" class="form-control" id="password" name="password" required minlength="6"
                     autocomplete="new-password">
            </div>
            <div class="col-md-6">
              <label for="confirmPassword" class="text-black form-required">Nhập lại mật khẩu</label>
              <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required
                     minlength="6" autocomplete="new-password">
            </div>
          </div>
          <div class="form-group mb-0">
            <button type="submit" class="btn btn-primary btn-lg btn-block">Đăng ký</button>
          </div>
          <p class="mt-4 mb-0 text-center">Đã có tài khoản? <a href="${ctx}/login">Đăng nhập</a></p>
        </form>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
