<c:set var="pageTitle" value="Sản phẩm"/>
<c:set var="adminNav" value="products"/>
<%@ include file="/WEB-INF/views/admin/top.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
  <h1 class="h3 text-black mb-2">Sản phẩm <small class="text-muted">(${fn:length(products)})</small></h1>
  <div class="d-flex">
    <form class="form-inline mr-2" method="get" action="${ctx}/admin/products">
      <input type="text" name="q" value="${fn:escapeXml(param.q)}" class="form-control form-control-sm mr-2" placeholder="Tên sản phẩm">
      <button class="btn btn-sm btn-outline-primary">Tìm</button>
    </form>
    <a href="${ctx}/admin/products/new" class="btn btn-sm btn-primary">+ Thêm sản phẩm</a>
  </div>
</div>

<div class="card"><div class="card-body p-0">
  <div class="table-responsive">
    <table class="table table-hover mb-0">
      <thead class="thead-light">
        <tr><th></th><th>Sản phẩm</th><th>Danh mục</th><th class="text-right">Giá gốc</th><th class="text-right">Giá bán</th>
          <th class="text-center">Biến thể</th><th class="text-center">Tồn kho</th><th class="text-center">Còn bán</th><th>Trạng thái</th><th></th></tr>
      </thead>
      <tbody>
        <c:forEach items="${products}" var="p">
          <tr class="${p.active ? '' : 'text-muted'}">
            <td><img src="${ctx}/${p.imageUrl}" alt="" style="width: 48px; height: 48px; object-fit: cover;" class="rounded"></td>
            <td><a href="${ctx}/admin/products/edit?id=${p.id}" class="font-weight-bold"><c:out value="${p.name}"/></a></td>
            <td class="small"><c:out value="${p.category.fullName}"/></td>
            <td class="text-right">${f:vnd(p.basePrice)}</td>
            <td class="text-right">${f:vnd(p.getFinalPrice(now))}
              <c:if test="${p.isOnSale(now)}"><span class="badge badge-danger">SALE</span></c:if></td>
            <td class="text-center">${fn:length(p.variants)}</td>
            <td class="text-center">${p.totalStock}</td>
            <td class="text-center ${p.totalAvailable == 0 ? 'text-danger' : ''}">${p.totalAvailable}</td>
            <td>
              <c:choose>
                <c:when test="${p.active}"><span class="badge badge-success">Đang bán</span></c:when>
                <c:otherwise><span class="badge badge-secondary">Ngừng bán</span></c:otherwise>
              </c:choose>
            </td>
            <td><a href="${ctx}/admin/products/edit?id=${p.id}" class="btn btn-sm btn-outline-secondary">Sửa</a></td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</div></div>

<%@ include file="/WEB-INF/views/admin/bottom.jspf" %>
</body>
</html>
