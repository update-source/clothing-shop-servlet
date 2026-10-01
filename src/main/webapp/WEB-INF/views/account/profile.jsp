<c:set var="pageTitle" value="Tài khoản của tôi"/>
<c:set var="accountNav" value="profile"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="bg-light py-3">
  <div class="container">
    <div class="row">
      <div class="col-md-12 mb-0"><a href="${ctx}/">Trang chủ</a> <span class="mx-2 mb-0">/</span> <strong class="text-black">Tài khoản</strong></div>
    </div>
  </div>
</div>

<div class="site-section">
  <div class="container">
    <div class="row">
      <div class="col-md-3 mb-5 mb-md-0">
        <%@ include file="/WEB-INF/views/common/account-nav.jspf" %>
      </div>
      <div class="col-md-9">
        <div class="p-4 border rounded mb-4">
          <div class="d-flex flex-wrap align-items-center justify-content-between">
            <div>
              <h2 class="h4 text-black mb-1"><c:out value="${customer.fullName}"/></h2>
              <p class="mb-0 text-muted">@<c:out value="${customer.username}"/> · <c:out value="${customer.email}"/></p>
            </div>
            <div class="text-md-right mt-3 mt-md-0">
              <span class="badge badge-primary p-2">Hạng: ${level.label}</span>
              <p class="mb-0 mt-2">Đã chi tiêu: <strong class="text-black">${f:vnd(totalSpent)}</strong></p>
            </div>
          </div>
          <c:choose>
            <c:when test="${not empty nextLevel}">
              <div class="progress mt-3" style="height: 8px;">
                <div class="progress-bar" role="progressbar" style="width: ${levelProgress}%"></div>
              </div>
              <small class="text-muted">Mua thêm ${f:vnd(toNextLevel)} (đơn hoàn tất) để lên hạng ${nextLevel.label}.</small>
            </c:when>
            <c:otherwise><small class="text-muted d-block mt-3">Bạn đang ở hạng cao nhất. Cảm ơn bạn đã đồng hành!</small></c:otherwise>
          </c:choose>
        </div>

        <div class="row">
          <div class="col-lg-6 mb-4">
            <form action="${ctx}/account/profile" method="post" class="p-4 border rounded h-100">
              <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
              <h3 class="h5 text-black mb-3">Thông tin cá nhân</h3>
              <div class="form-group">
                <label class="text-black form-required" for="fullName">Họ và tên</label>
                <input type="text" class="form-control" id="fullName" name="fullName" required maxlength="100"
                       value="${fn:escapeXml(customer.fullName)}">
              </div>
              <div class="form-group">
                <label class="text-black form-required" for="phone">Số điện thoại</label>
                <input type="tel" class="form-control" id="phone" name="phone" required pattern="(0|\+84)[0-9]{9,10}"
                       value="${fn:escapeXml(customer.phone)}">
              </div>
              <div class="form-group row">
                <div class="col-6">
                  <label class="text-black">Giới tính</label>
                  <input type="text" class="form-control" value="${empty customer.gender ? '—' : customer.gender.label}" disabled>
                </div>
                <div class="col-6">
                  <label class="text-black">Ngày sinh</label>
                  <input type="text" class="form-control" value="${empty customer.dob ? '—' : f:date(customer.dob)}" disabled>
                </div>
              </div>
              <button type="submit" class="btn btn-primary">Lưu thay đổi</button>
            </form>
          </div>
          <div class="col-lg-6 mb-4">
            <form action="${ctx}/account/password" method="post" class="p-4 border rounded h-100">
              <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
              <h3 class="h5 text-black mb-3">Đổi mật khẩu</h3>
              <div class="form-group">
                <label class="text-black form-required" for="oldPassword">Mật khẩu hiện tại</label>
                <input type="password" class="form-control" id="oldPassword" name="oldPassword" required autocomplete="current-password">
              </div>
              <div class="form-group">
                <label class="text-black form-required" for="newPassword">Mật khẩu mới</label>
                <input type="password" class="form-control" id="newPassword" name="newPassword" required minlength="6" autocomplete="new-password">
              </div>
              <div class="form-group">
                <label class="text-black form-required" for="confirmPassword">Nhập lại mật khẩu mới</label>
                <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required minlength="6" autocomplete="new-password">
              </div>
              <button type="submit" class="btn btn-outline-primary">Đổi mật khẩu</button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
