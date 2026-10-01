<c:set var="pageTitle" value="Đơn #${order.id}"/>
<c:set var="adminNav" value="orders"/>
<c:set var="linkProducts" value="${false}"/>
<%@ include file="/WEB-INF/views/admin/top.jspf" %>

<p class="mb-2"><a href="${ctx}/staff/orders">← Danh sách đơn</a></p>
<div class="d-flex flex-wrap justify-content-between align-items-start mb-4">
  <div class="mb-3">
    <h1 class="h3 text-black mb-1">Đơn hàng #${order.id}</h1>
    <p class="text-muted mb-2">Đặt lúc ${f:dateTime(order.orderDate)} bởi
      <strong><c:out value="${order.customer.fullName}"/></strong> (@<c:out value="${order.customer.username}"/>,
      <c:out value="${order.customer.email}"/>)
      <c:if test="${not empty order.handledBy}"> · Xử lý: <strong><c:out value="${order.handledBy.fullName}"/></strong></c:if>
    </p>
    <%@ include file="/WEB-INF/views/orders/status-steps.jspf" %>
  </div>
  <div class="card">
    <div class="card-body">
      <h2 class="h6 text-uppercase text-muted">Xử lý đơn</h2>
      <c:set var="hasAction" value="${order.canConfirm() or order.canShip() or order.canComplete() or order.canFailDelivery() or order.canReturn() or order.canCancel()}"/>
      <div class="d-flex flex-wrap" style="gap: 6px;">
        <c:if test="${order.canConfirm()}">
          <form method="post" action="${ctx}/staff/orders/action">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="id" value="${order.id}">
            <button name="action" value="CONFIRM" class="btn btn-sm btn-primary">Xác nhận đơn</button>
          </form>
        </c:if>
        <c:if test="${order.canShip()}">
          <form method="post" action="${ctx}/staff/orders/action">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="id" value="${order.id}">
            <button name="action" value="SHIP" class="btn btn-sm btn-primary">Giao hàng</button>
          </form>
        </c:if>
        <c:if test="${order.canComplete()}">
          <form method="post" action="${ctx}/staff/orders/action">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="id" value="${order.id}">
            <button name="action" value="COMPLETE" class="btn btn-sm btn-success">Đã giao thành công</button>
          </form>
        </c:if>
        <c:if test="${order.canFailDelivery()}">
          <form method="post" action="${ctx}/staff/orders/action" onsubmit="return confirm('Ghi nhận giao thất bại? Hàng sẽ nhập lại kho.');">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="id" value="${order.id}">
            <button name="action" value="FAIL_DELIVERY" class="btn btn-sm btn-outline-danger">Giao thất bại</button>
          </form>
        </c:if>
        <c:if test="${order.canReturn()}">
          <form method="post" action="${ctx}/staff/orders/action" onsubmit="return confirm('Nhận trả hàng và hoàn tiền cho khách?');">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="id" value="${order.id}">
            <button name="action" value="RETURN_GOODS" class="btn btn-sm btn-outline-dark">Nhận trả hàng</button>
          </form>
        </c:if>
        <c:if test="${order.canCancel()}">
          <form method="post" action="${ctx}/staff/orders/action" onsubmit="return confirm('Huỷ đơn #${order.id}?');">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="id" value="${order.id}">
            <button name="action" value="CANCEL" class="btn btn-sm btn-outline-danger">Huỷ đơn</button>
          </form>
        </c:if>
        <c:if test="${not hasAction}"><span class="text-muted small">Đơn đã kết thúc, không còn thao tác.</span></c:if>
      </div>
      <c:if test="${order.paymentMethod == 'VNPAY' and order.status == 'PENDING' and not order.paid}">
        <small class="text-warning d-block mt-2">Đơn VNPAY chưa thanh toán — chưa thể xác nhận.</small>
      </c:if>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/orders/order-summary.jspf" %>

<%@ include file="/WEB-INF/views/admin/bottom.jspf" %>
</body>
</html>
