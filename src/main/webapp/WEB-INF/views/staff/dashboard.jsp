<c:set var="pageTitle" value="Tổng quan"/>
<c:set var="adminNav" value="dashboard"/>
<%@ include file="/WEB-INF/views/admin/top.jspf" %>

<h1 class="h3 text-black mb-4">Tổng quan</h1>
<div class="row">
  <div class="col-md-6 col-xl-3 mb-4">
    <div class="card stat-card"><div class="card-body">
      <div class="label">Doanh thu (đơn hoàn tất)</div><div class="value">${f:vnd(revenue)}</div>
    </div></div>
  </div>
  <div class="col-md-6 col-xl-3 mb-4">
    <a href="${ctx}/staff/orders?status=PENDING" class="card stat-card text-decoration-none"><div class="card-body">
      <div class="label">Chờ xác nhận</div><div class="value">${pendingCount}</div>
    </div></a>
  </div>
  <div class="col-md-6 col-xl-3 mb-4">
    <div class="card stat-card"><div class="card-body">
      <div class="label">Khách hàng</div><div class="value">${customerCount}</div>
    </div></div>
  </div>
  <div class="col-md-6 col-xl-3 mb-4">
    <div class="card stat-card"><div class="card-body">
      <div class="label">Sản phẩm đang bán</div><div class="value">${productCount}</div>
    </div></div>
  </div>
</div>

<div class="card mb-4">
  <div class="card-body">
    <h2 class="h6 text-uppercase text-muted mb-3">Đơn theo trạng thái</h2>
    <div class="d-flex flex-wrap" style="gap: 10px;">
      <c:forEach items="${counts}" var="e">
        <a href="${ctx}/staff/orders?status=${e.key}" class="btn btn-light border">
          <span class="badge badge-${e.key.badge}">${e.key.label}</span> <strong class="ml-1">${e.value}</strong>
        </a>
      </c:forEach>
    </div>
  </div>
</div>

<div class="card">
  <div class="card-body">
    <div class="d-flex justify-content-between mb-3">
      <h2 class="h6 text-uppercase text-muted mb-0">Đơn mới nhất</h2>
      <a href="${ctx}/staff/orders" class="small">Xem tất cả</a>
    </div>
    <%@ include file="/WEB-INF/views/staff/order-table.jspf" %>
  </div>
</div>

<%@ include file="/WEB-INF/views/admin/bottom.jspf" %>
</body>
</html>
