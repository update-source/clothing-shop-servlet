<c:set var="pageTitle" value="Đặt hàng thành công"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="bg-light py-3">
  <div class="container">
    <div class="row">
      <div class="col-md-12 mb-0"><a href="${ctx}/">Trang chủ</a> <span class="mx-2 mb-0">/</span> <strong class="text-black">Đặt hàng thành công</strong></div>
    </div>
  </div>
</div>

<div class="site-section">
  <div class="container">
    <div class="row">
      <div class="col-md-12 text-center">
        <span class="icon-check_circle display-3 text-success"></span>
        <h2 class="display-4 text-black">Cảm ơn bạn!</h2>
        <p class="lead mb-2">Đơn hàng <strong>#${order.id}</strong> đã được ghi nhận và đang ở trạng thái
          <span class="badge badge-${order.status.badge}">${order.status.label}</span>.</p>
        <c:choose>
          <c:when test="${order.canPayOnline()}">
            <p class="mb-4">Hàng đã được giữ cho bạn. Vui lòng hoàn tất thanh toán VNPAY để cửa hàng xác nhận đơn.</p>
          </c:when>
          <c:otherwise>
            <p class="mb-4">Cửa hàng sẽ xác nhận và giao hàng sớm. Bạn thanh toán ${f:vnd(order.total)} khi nhận hàng.</p>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
    <div class="row justify-content-center">
      <div class="col-lg-7">
        <div class="border p-4 mb-4">
          <h3 class="h5 text-black">Giao đến</h3>
          <p class="mb-0"><strong><c:out value="${order.shippingAddress.recipientName}"/></strong> ·
            <c:out value="${order.shippingAddress.phone}"/><br><c:out value="${order.shippingAddress.fullAddress}"/></p>
        </div>
        <table class="table site-block-order-table">
          <tbody>
            <c:forEach items="${order.details}" var="d">
              <tr>
                <td><c:out value="${d.variant.product.name}"/> <small class="text-muted">(<c:out value="${d.variant.label}"/>)</small>
                  <strong class="mx-2">x</strong> ${d.quantity}</td>
                <td class="text-right">${f:vnd(d.subtotal)}</td>
              </tr>
            </c:forEach>
            <tr><td>Tạm tính</td><td class="text-right">${f:vnd(order.subtotal)}</td></tr>
            <c:if test="${not empty order.voucher}">
              <tr><td>Giảm giá (${order.voucher.code})</td><td class="text-right">− ${f:vnd(order.discount)}</td></tr>
            </c:if>
            <tr><td>Phí vận chuyển</td><td class="text-right">${f:vnd(order.shippingFee)}</td></tr>
            <tr><td class="font-weight-bold text-black">Tổng cộng</td>
              <td class="text-right font-weight-bold text-black">${f:vnd(order.total)}</td></tr>
            <tr><td>Thanh toán</td><td class="text-right">${order.paymentMethod.label}</td></tr>
          </tbody>
        </table>
        <p class="text-center mt-4">
          <a href="${ctx}/orders/detail?id=${order.id}" class="btn btn-sm btn-primary">Xem chi tiết đơn</a>
          <a href="${ctx}/shop" class="btn btn-sm btn-outline-primary">Tiếp tục mua sắm</a>
        </p>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
