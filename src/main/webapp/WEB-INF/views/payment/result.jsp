<c:set var="pageTitle" value="Kết quả thanh toán"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="site-section">
  <div class="container min-vh-50">
    <div class="row">
      <div class="col-md-12 text-center">
        <c:choose>
          <c:when test="${outcome.success or outcome.code == 'ALREADY_PROCESSED'}">
            <span class="icon-check_circle display-3 text-success"></span>
            <h2 class="display-4 text-black">Thanh toán hoàn tất</h2>
          </c:when>
          <c:when test="${outcome.code == 'FAILED'}">
            <span class="icon-close display-3 text-danger"></span>
            <h2 class="display-4 text-black">Thanh toán chưa thành công</h2>
          </c:when>
          <c:otherwise>
            <span class="icon-warning display-3 text-warning"></span>
            <h2 class="display-4 text-black">Không xác minh được giao dịch</h2>
          </c:otherwise>
        </c:choose>
        <p class="lead mb-5"><c:out value="${outcome.message}"/></p>
        <p>
          <c:if test="${not empty outcome.orderId}">
            <a href="${ctx}/orders/detail?id=${outcome.orderId}" class="btn btn-sm btn-primary">Xem đơn hàng #${outcome.orderId}</a>
          </c:if>
          <a href="${ctx}/shop" class="btn btn-sm btn-outline-primary">Tiếp tục mua sắm</a>
        </p>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
