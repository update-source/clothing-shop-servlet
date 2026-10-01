<c:set var="pageTitle" value="Đơn hàng của tôi"/>
<c:set var="accountNav" value="orders"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="bg-light py-3">
  <div class="container">
    <div class="row">
      <div class="col-md-12 mb-0">
        <a href="${ctx}/">Trang chủ</a> <span class="mx-2 mb-0">/</span>
        <a href="${ctx}/account/profile">Tài khoản</a> <span class="mx-2 mb-0">/</span>
        <strong class="text-black">Đơn hàng</strong>
      </div>
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
        <h2 class="h4 text-black mb-3">Đơn hàng của tôi</h2>
        <ul class="nav nav-pills mb-4 small">
          <li class="nav-item"><a class="nav-link ${empty selectedStatus ? 'active' : ''}" href="${ctx}/orders">Tất cả</a></li>
          <c:forEach items="${statuses}" var="s">
            <li class="nav-item">
              <a class="nav-link ${selectedStatus == s ? 'active' : ''}" href="${ctx}/orders?status=${s}">${s.label}</a>
            </li>
          </c:forEach>
        </ul>
        <c:choose>
          <c:when test="${empty orders}">
            <div class="alert alert-info">Không có đơn hàng nào. <a href="${ctx}/shop">Mua sắm ngay</a></div>
          </c:when>
          <c:otherwise>
            <div class="table-responsive">
              <table class="table table-bordered">
                <thead>
                  <tr><th>Mã đơn</th><th>Ngày đặt</th><th>Sản phẩm</th><th class="text-right">Tổng tiền</th>
                    <th>Thanh toán</th><th>Trạng thái</th><th></th></tr>
                </thead>
                <tbody>
                  <c:forEach items="${orders}" var="o">
                    <tr>
                      <td><a href="${ctx}/orders/detail?id=${o.id}">#${o.id}</a></td>
                      <td class="small">${f:dateTime(o.orderDate)}</td>
                      <td class="small">
                        <c:out value="${o.details[0].variant.product.name}"/>
                        <c:if test="${fn:length(o.details) > 1}"><span class="text-muted">và ${fn:length(o.details) - 1} sản phẩm khác</span></c:if>
                      </td>
                      <td class="text-right">${f:vnd(o.total)}</td>
                      <td class="small">${o.paymentMethod}
                        <c:if test="${o.paid}"><span class="badge badge-success">Đã trả</span></c:if></td>
                      <td><span class="badge badge-${o.status.badge}">${o.status.label}</span></td>
                      <td><a href="${ctx}/orders/detail?id=${o.id}" class="btn btn-sm btn-outline-primary">Xem</a></td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
