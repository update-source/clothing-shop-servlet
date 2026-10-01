<c:set var="pageTitle" value="Tài khoản"/>
<c:set var="adminNav" value="users"/>
<%@ include file="/WEB-INF/views/admin/top.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
  <h1 class="h3 text-black mb-2">Tài khoản</h1>
  <form class="form-inline" method="get" action="${ctx}/admin/users">
    <input type="hidden" name="type" value="${type}">
    <input type="text" name="q" value="${fn:escapeXml(param.q)}" class="form-control form-control-sm mr-2" placeholder="Tên đăng nhập, họ tên, email">
    <button class="btn btn-sm btn-outline-primary">Tìm</button>
  </form>
</div>
<ul class="nav nav-tabs mb-3">
  <li class="nav-item"><a class="nav-link ${type == 'CUSTOMER' ? 'active' : ''}" href="${ctx}/admin/users?type=CUSTOMER">Khách hàng</a></li>
  <li class="nav-item"><a class="nav-link ${type == 'EMPLOYEE' ? 'active' : ''}" href="${ctx}/admin/users?type=EMPLOYEE">Nhân viên</a></li>
</ul>

<div class="row">
  <div class="${type == 'EMPLOYEE' ? 'col-xl-8' : 'col-12'} mb-4">
    <div class="card"><div class="card-body p-0">
      <div class="table-responsive">
        <table class="table mb-0">
          <thead class="thead-light">
            <tr><th>Tài khoản</th><th>Họ tên</th><th>Liên hệ</th>
              <th>${type == 'EMPLOYEE' ? 'Vai trò' : 'Hạng / Đã chi tiêu'}</th><th>Trạng thái</th><th></th></tr>
          </thead>
          <tbody>
            <c:if test="${empty users}"><tr><td colspan="6" class="text-center text-muted">Không có tài khoản nào.</td></tr></c:if>
            <c:forEach items="${users}" var="u">
              <tr class="${u.active ? '' : 'text-muted'}">
                <td class="font-weight-bold">@<c:out value="${u.username}"/></td>
                <td><c:out value="${u.fullName}"/></td>
                <td class="small"><c:out value="${u.email}"/><br><c:out value="${u.phone}"/></td>
                <td class="small">
                  <c:choose>
                    <c:when test="${type == 'EMPLOYEE'}">${u.role.label}<br>Vào làm ${f:date(u.hireDate)}</c:when>
                    <c:otherwise>${u.level.label}<br>${f:vnd(u.totalSpent)}</c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${u.active}"><span class="badge badge-success">Hoạt động</span></c:when>
                    <c:otherwise><span class="badge badge-secondary">Đã khoá</span></c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <c:if test="${u.id != auth.id}">
                    <form method="post" action="${ctx}/admin/users"
                          onsubmit="return confirm('${u.active ? 'Khoá' : 'Mở khoá'} tài khoản @${fn:escapeXml(u.username)}?');">
                      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                      <input type="hidden" name="type" value="${type}">
                      <input type="hidden" name="id" value="${u.id}">
                      <input type="hidden" name="action" value="${u.active ? 'lock' : 'unlock'}">
                      <button class="btn btn-sm ${u.active ? 'btn-outline-danger' : 'btn-outline-success'}">${u.active ? 'Khoá' : 'Mở khoá'}</button>
                    </form>
                  </c:if>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </div></div>
  </div>

  <c:if test="${type == 'EMPLOYEE'}">
    <div class="col-xl-4">
      <form method="post" action="${ctx}/admin/users" class="card"><div class="card-body">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="createEmployee">
        <h2 class="h6 text-uppercase text-muted">Tạo tài khoản nhân viên</h2>
        <div class="form-row">
          <div class="form-group col-6"><label class="form-required">Tên đăng nhập</label>
            <input class="form-control" name="username" required pattern="[A-Za-z0-9_.]{4,30}"></div>
          <div class="form-group col-6"><label class="form-required">Vai trò</label>
            <select class="form-control" name="role">
              <c:forEach items="${roles}" var="r"><option value="${r}">${r.label}</option></c:forEach>
            </select></div>
        </div>
        <div class="form-group"><label class="form-required">Họ và tên</label>
          <input class="form-control" name="fullName" required maxlength="100"></div>
        <div class="form-row">
          <div class="form-group col-6"><label class="form-required">Email</label>
            <input type="email" class="form-control" name="email" required></div>
          <div class="form-group col-6"><label class="form-required">Điện thoại</label>
            <input class="form-control" name="phone" required pattern="(0|\+84)[0-9]{9,10}"></div>
        </div>
        <div class="form-row">
          <div class="form-group col-4"><label>Giới tính</label>
            <select class="form-control" name="gender"><option value="">—</option>
              <c:forEach items="${genders}" var="g"><option value="${g}">${g.label}</option></c:forEach></select></div>
          <div class="form-group col-4"><label>Ngày sinh</label><input type="date" class="form-control" name="dob"></div>
          <div class="form-group col-4"><label>Ngày vào làm</label><input type="date" class="form-control" name="hireDate" value="${f:isoDate(now.toLocalDate())}"></div>
        </div>
        <div class="form-row">
          <div class="form-group col-6"><label class="form-required">Mật khẩu</label>
            <input type="password" class="form-control" name="password" required minlength="6" autocomplete="new-password"></div>
          <div class="form-group col-6"><label class="form-required">Nhập lại</label>
            <input type="password" class="form-control" name="confirmPassword" required minlength="6" autocomplete="new-password"></div>
        </div>
        <button class="btn btn-primary">Tạo tài khoản</button>
      </div></form>
    </div>
  </c:if>
</div>

<%@ include file="/WEB-INF/views/admin/bottom.jspf" %>
</body>
</html>
