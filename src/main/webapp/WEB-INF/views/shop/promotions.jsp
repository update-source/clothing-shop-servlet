<c:set var="pageTitle" value="Khuyến mãi"/>
<c:set var="activeNav" value="promotions"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="bg-light py-3">
  <div class="container">
    <div class="row">
      <div class="col-md-12 mb-0"><a href="${ctx}/">Trang chủ</a> <span class="mx-2 mb-0">/</span> <strong class="text-black">Khuyến mãi</strong></div>
    </div>
  </div>
</div>

<div class="site-section">
  <div class="container">
    <div class="row mb-4">
      <div class="col-md-12">
        <h2 class="h3 text-black">Chương trình đang diễn ra</h2>
        <p class="text-muted">Giá đã được giảm sẵn trên sản phẩm, bạn không cần nhập mã. Nếu một sản phẩm thuộc nhiều
          chương trình, bạn luôn được hưởng mức giảm cao nhất.</p>
      </div>
    </div>
    <c:if test="${empty promotions}">
      <div class="alert alert-info">Hiện chưa có chương trình khuyến mãi nào. Quay lại sau nhé!</div>
    </c:if>
    <c:forEach items="${promotions}" var="promo">
      <div class="mb-5">
        <div class="d-flex flex-wrap justify-content-between align-items-end border-bottom pb-2 mb-4">
          <div>
            <h3 class="h4 text-black mb-1"><c:out value="${promo.name}"/></h3>
            <span class="badge badge-danger"><c:out value="${promo.policy.describe()}"/></span>
          </div>
          <small class="text-muted">${f:dateTime(promo.period.start)} – ${f:dateTime(promo.period.end)}</small>
        </div>
        <div class="row">
          <c:forEach items="${promo.products}" var="p">
            <c:if test="${p.active}">
              <div class="col-sm-6 col-lg-3 mb-4"><t:product-card product="${p}" bordered="true"/></div>
            </c:if>
          </c:forEach>
        </div>
      </div>
    </c:forEach>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
