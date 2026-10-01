<c:set var="pageTitle" value="Sản phẩm yêu thích"/>
<c:set var="accountNav" value="wishlist"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="bg-light py-3">
  <div class="container">
    <div class="row">
      <div class="col-md-12 mb-0">
        <a href="${ctx}/">Trang chủ</a> <span class="mx-2 mb-0">/</span>
        <a href="${ctx}/account/profile">Tài khoản</a> <span class="mx-2 mb-0">/</span>
        <strong class="text-black">Yêu thích</strong>
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
        <h2 class="h4 text-black mb-4">Sản phẩm yêu thích (${fn:length(products)})</h2>
        <c:if test="${empty products}">
          <div class="alert alert-info">Bạn chưa yêu thích sản phẩm nào. Bấm biểu tượng trái tim ở trang sản phẩm để lưu lại.</div>
        </c:if>
        <div class="row">
          <c:forEach items="${products}" var="p">
            <div class="col-sm-6 col-lg-4 mb-4">
              <t:product-card product="${p}" bordered="true"/>
              <form action="${ctx}/wishlist/remove" method="post" class="text-center mt-2">
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                <input type="hidden" name="productId" value="${p.id}">
                <c:if test="${not p.active}"><small class="text-muted d-block">Đã ngừng bán</small></c:if>
                <button type="submit" class="btn btn-link btn-sm text-danger">Bỏ khỏi yêu thích</button>
              </form>
            </div>
          </c:forEach>
        </div>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
