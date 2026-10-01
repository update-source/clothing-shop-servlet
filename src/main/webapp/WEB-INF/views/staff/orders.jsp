<c:set var="pageTitle" value="Đơn hàng"/>
<c:set var="adminNav" value="orders"/>
<%@ include file="/WEB-INF/views/admin/top.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
  <h1 class="h3 text-black mb-2">Đơn hàng <small class="text-muted">(${result.total})</small></h1>
  <form class="form-inline" method="get" action="${ctx}/staff/orders">
    <c:if test="${not empty selectedStatus}"><input type="hidden" name="status" value="${selectedStatus}"></c:if>
    <input type="text" name="q" value="${fn:escapeXml(param.q)}" class="form-control form-control-sm mr-2"
           placeholder="Mã đơn, tên hoặc SĐT người nhận">
    <button class="btn btn-sm btn-primary">Tìm</button>
  </form>
</div>

<ul class="nav nav-pills mb-3 small">
  <li class="nav-item"><a class="nav-link ${empty selectedStatus ? 'active' : ''}" href="${ctx}/staff/orders">Tất cả</a></li>
  <c:forEach items="${statuses}" var="s">
    <li class="nav-item">
      <a class="nav-link ${selectedStatus == s ? 'active' : ''}" href="${ctx}/staff/orders?status=${s}">${s.label}
        <span class="badge badge-light">${counts[s]}</span></a>
    </li>
  </c:forEach>
</ul>

<div class="card">
  <div class="card-body p-0">
    <%@ include file="/WEB-INF/views/staff/order-table.jspf" %>
  </div>
</div>

<c:if test="${result.totalPages > 1}">
  <nav class="mt-3">
    <ul class="pagination pagination-sm">
      <c:forEach begin="1" end="${result.totalPages}" var="i">
        <li class="page-item ${i == result.page ? 'active' : ''}">
          <a class="page-link" href="${ctx}/staff/orders?${pageQuery}page=${i}">${i}</a>
        </li>
      </c:forEach>
    </ul>
  </nav>
</c:if>

<%@ include file="/WEB-INF/views/admin/bottom.jspf" %>
</body>
</html>
