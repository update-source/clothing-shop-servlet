<c:set var="pageTitle" value="${promotion.name}"/>
<c:set var="adminNav" value="promotions"/>
<%@ include file="/WEB-INF/views/admin/top.jspf" %>

<p class="mb-2"><a href="${ctx}/admin/promotions">← Danh sách chương trình</a></p>
<div class="d-flex flex-wrap justify-content-between align-items-start mb-3">
  <div>
    <h1 class="h3 text-black mb-1"><c:out value="${promotion.name}"/></h1>
    <p class="text-muted mb-0"><c:out value="${promotion.policy.describe()}"/> ·
      ${f:dateTime(promotion.period.start)} → ${f:dateTime(promotion.period.end)}
      <c:if test="${promotion.isOngoing(now)}"><span class="badge badge-success">Đang chạy</span></c:if></p>
  </div>
  <form method="post" action="${ctx}/admin/promotions" onsubmit="return confirm('Xoá chương trình này? Giá sản phẩm trở về giá gốc.');">
    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
    <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${promotion.id}">
    <button class="btn btn-sm btn-outline-danger">Xoá chương trình</button>
  </form>
</div>

<div class="row">
  <div class="col-lg-8 mb-4">
    <div class="card"><div class="card-body">
      <h2 class="h6 text-uppercase text-muted">Sản phẩm áp dụng (${fn:length(promotion.products)})</h2>
      <table class="table table-sm mb-3">
        <thead><tr><th>Sản phẩm</th><th class="text-right">Giá gốc</th><th class="text-right">Giá theo chương trình</th><th></th></tr></thead>
        <tbody>
          <c:forEach items="${promotion.products}" var="p">
            <tr>
              <td><c:out value="${p.name}"/> <c:if test="${not p.active}"><span class="badge badge-secondary">Ngừng bán</span></c:if></td>
              <td class="text-right">${f:vnd(p.basePrice)}</td>
              <td class="text-right">${f:vnd(promotion.apply(p.basePrice))}</td>
              <td class="text-right">
                <form method="post" action="${ctx}/admin/promotions">
                  <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                  <input type="hidden" name="action" value="removeProduct"><input type="hidden" name="id" value="${promotion.id}">
                  <input type="hidden" name="productId" value="${p.id}">
                  <button class="btn btn-sm btn-link text-danger p-0">Bỏ</button>
                </form>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty promotion.products}"><tr><td colspan="4" class="text-muted">Chưa có sản phẩm nào.</td></tr></c:if>
        </tbody>
      </table>
      <form method="post" action="${ctx}/admin/promotions" class="form-inline">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
        <input type="hidden" name="action" value="addProduct"><input type="hidden" name="id" value="${promotion.id}">
        <select name="productId" class="form-control form-control-sm mr-2" required>
          <option value="">— Chọn sản phẩm để thêm —</option>
          <c:forEach items="${allProducts}" var="p">
            <c:if test="${p.active and not promotion.includes(p)}">
              <option value="${p.id}"><c:out value="${p.name}"/> (${f:vnd(p.basePrice)})</option>
            </c:if>
          </c:forEach>
        </select>
        <button class="btn btn-sm btn-primary">Thêm vào chương trình</button>
      </form>
    </div></div>
  </div>
  <div class="col-lg-4">
    <form method="post" action="${ctx}/admin/promotions" class="card"><div class="card-body">
      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
      <input type="hidden" name="action" value="rename"><input type="hidden" name="id" value="${promotion.id}">
      <h2 class="h6 text-uppercase text-muted">Đổi tên</h2>
      <input class="form-control mb-2" name="name" required maxlength="150" value="${fn:escapeXml(promotion.name)}">
      <button class="btn btn-sm btn-outline-primary">Lưu tên</button>
      <p class="small text-muted mt-3 mb-0">Cách tính giảm và thời gian là giá trị bất biến của chương trình; muốn đổi thì
        tạo chương trình mới.</p>
    </div></form>
  </div>
</div>

<%@ include file="/WEB-INF/views/admin/bottom.jspf" %>
</body>
</html>
