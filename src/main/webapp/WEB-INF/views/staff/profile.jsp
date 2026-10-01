<c:set var="pageTitle" value="Hồ sơ nhân viên"/>
<c:set var="adminNav" value="profile"/>
<%@ include file="/WEB-INF/views/admin/top.jspf" %>

<h1 class="h3 text-black mb-4">Hồ sơ nhân viên</h1>
<div class="row">
  <div class="col-lg-6 mb-4">
    <form method="post" action="${ctx}/staff/profile" class="card"><div class="card-body">
      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
      <p class="mb-1">Tên đăng nhập: <strong>@<c:out value="${employee.username}"/></strong></p>
      <p class="mb-1">Vai trò: <span class="badge badge-primary">${employee.role.label}</span></p>
      <p class="mb-3">Ngày vào làm: ${f:date(employee.hireDate)} · Email: <c:out value="${employee.email}"/></p>
      <div class="form-group">
        <label class="form-required" for="fullName">Họ và tên</label>
        <input class="form-control" id="fullName" name="fullName" required maxlength="100" value="${fn:escapeXml(employee.fullName)}">
      </div>
      <div class="form-group">
        <label class="form-required" for="phone">Số điện thoại</label>
        <input class="form-control" id="phone" name="phone" required pattern="(0|\+84)[0-9]{9,10}" value="${fn:escapeXml(employee.phone)}">
      </div>
      <button class="btn btn-primary">Lưu</button>
    </div></form>
  </div>
  <div class="col-lg-6 mb-4">
    <form method="post" action="${ctx}/staff/password" class="card"><div class="card-body">
      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
      <h2 class="h6 text-uppercase text-muted">Đổi mật khẩu</h2>
      <div class="form-group"><label for="oldPassword">Mật khẩu hiện tại</label>
        <input type="password" class="form-control" id="oldPassword" name="oldPassword" required></div>
      <div class="form-group"><label for="newPassword">Mật khẩu mới</label>
        <input type="password" class="form-control" id="newPassword" name="newPassword" required minlength="6"></div>
      <div class="form-group"><label for="confirmPassword">Nhập lại mật khẩu mới</label>
        <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required minlength="6"></div>
      <button class="btn btn-outline-primary">Đổi mật khẩu</button>
    </div></form>
  </div>
</div>

<%@ include file="/WEB-INF/views/admin/bottom.jspf" %>
</body>
</html>
