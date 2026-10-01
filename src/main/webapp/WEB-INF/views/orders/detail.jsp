<c:set var="pageTitle" value="Đơn hàng #${order.id}"/>
<c:set var="linkProducts" value="${true}"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="bg-light py-3">
  <div class="container">
    <div class="row">
      <div class="col-md-12 mb-0">
        <a href="${ctx}/">Trang chủ</a> <span class="mx-2 mb-0">/</span>
        <a href="${ctx}/orders">Đơn hàng của tôi</a> <span class="mx-2 mb-0">/</span>
        <strong class="text-black">#${order.id}</strong>
      </div>
    </div>
  </div>
</div>

<div class="site-section">
  <div class="container">
    <div class="d-flex flex-wrap justify-content-between align-items-start mb-4">
      <div class="mb-3">
        <h2 class="h3 text-black mb-1">Đơn hàng #${order.id}</h2>
        <p class="text-muted mb-2">Đặt lúc ${f:dateTime(order.orderDate)} · ${order.itemCount} sản phẩm</p>
        <%@ include file="/WEB-INF/views/orders/status-steps.jspf" %>
      </div>
      <div class="d-flex flex-wrap" style="gap: 8px;">
        <%@ include file="/WEB-INF/views/orders/customer-actions.jspf" %>
        <c:if test="${order.canCancel()}">
          <form action="${ctx}/orders/cancel" method="post"
                onsubmit="return confirm('Bạn chắc chắn muốn huỷ đơn #${order.id}?');">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
            <input type="hidden" name="id" value="${order.id}">
            <button type="submit" class="btn btn-outline-danger btn-sm">Huỷ đơn</button>
          </form>
        </c:if>
      </div>
    </div>

    <c:if test="${order.status == 'COMPLETED'}">
      <div class="alert alert-success">Đơn đã giao thành công. Bấm vào tên sản phẩm bên dưới để viết đánh giá.</div>
    </c:if>

    <%@ include file="/WEB-INF/views/orders/order-summary.jspf" %>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
