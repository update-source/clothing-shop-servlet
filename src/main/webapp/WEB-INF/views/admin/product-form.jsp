<c:set var="isNew" value="${empty product}"/>
<c:set var="pageTitle" value="${isNew ? 'Thêm sản phẩm' : product.name}"/>
<c:set var="adminNav" value="products"/>
<%@ include file="/WEB-INF/views/admin/top.jspf" %>

<p class="mb-2"><a href="${ctx}/admin/products">← Danh sách sản phẩm</a></p>
<div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
  <h1 class="h3 text-black mb-2"><c:out value="${pageTitle}"/>
    <c:if test="${not isNew and not product.active}"><span class="badge badge-secondary">Ngừng bán</span></c:if></h1>
  <c:if test="${not isNew}">
    <a href="${ctx}/product?id=${product.id}" target="_blank" class="btn btn-sm btn-outline-secondary">Xem trên cửa hàng ↗</a>
  </c:if>
</div>

<div class="row">
  <div class="col-lg-7 mb-4">
    <%-- Form multipart: token CSRF gửi qua query string để luôn đọc được trước khi phân tích tệp --%>
    <form method="post" enctype="multipart/form-data" class="card"
          action="${ctx}/admin/products/${isNew ? 'new' : 'edit'}?_csrf=${sessionScope.csrfToken}${isNew ? '' : '&amp;id='.concat(product.id)}">
      <div class="card-body">
        <h2 class="h6 text-uppercase text-muted">Thông tin sản phẩm</h2>
        <div class="form-group">
          <label class="form-required" for="name">Tên sản phẩm</label>
          <input class="form-control" id="name" name="name" required maxlength="150" value="${fn:escapeXml(product.name)}">
        </div>
        <div class="form-group">
          <label class="form-required" for="categoryId">Danh mục</label>
          <select class="form-control" id="categoryId" name="categoryId" required>
            <option value="">— Chọn danh mục —</option>
            <c:forEach items="${categories}" var="cat">
              <option value="${cat.id}" ${product.category.id == cat.id ? 'selected' : ''}><c:out value="${cat.fullName}"/></option>
            </c:forEach>
          </select>
        </div>
        <c:if test="${isNew}">
          <div class="form-group">
            <label class="form-required" for="basePrice">Giá gốc (₫)</label>
            <input class="form-control" id="basePrice" name="basePrice" required inputmode="numeric" placeholder="199000">
          </div>
        </c:if>
        <div class="form-group">
          <label for="description">Mô tả</label>
          <textarea class="form-control" id="description" name="description" rows="5" maxlength="2000"><c:out value="${product.description}"/></textarea>
        </div>
        <div class="form-group">
          <label for="image">Ảnh sản phẩm (JPG/PNG/WEBP, tối đa 2 MB)</label>
          <c:if test="${not isNew}">
            <div class="mb-2"><img src="${ctx}/${product.imageUrl}" alt="" style="max-width: 160px;" class="rounded border"></div>
          </c:if>
          <input type="file" class="form-control-file" id="image" name="image" accept="image/jpeg,image/png,image/webp,image/gif">
        </div>
        <button class="btn btn-primary">${isNew ? 'Tạo sản phẩm' : 'Lưu thông tin'}</button>
      </div>
    </form>
  </div>

  <c:if test="${not isNew}">
    <div class="col-lg-5 mb-4">
      <form method="post" action="${ctx}/admin/products/price" class="card mb-4"><div class="card-body">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="id" value="${product.id}">
        <h2 class="h6 text-uppercase text-muted">Giá</h2>
        <p class="mb-2 small">Giá gốc hiện tại: <strong>${f:vnd(product.basePrice)}</strong> · Giá bán hôm nay:
          <strong>${f:vnd(product.getFinalPrice(now))}</strong></p>
        <div class="input-group">
          <input class="form-control" name="basePrice" required inputmode="numeric" value="${f:plain(product.basePrice)}">
          <div class="input-group-append"><button class="btn btn-outline-primary">Đổi giá</button></div>
        </div>
      </div></form>

      <c:if test="${product.active}">
        <form method="post" action="${ctx}/admin/products/discontinue" class="card mb-4"
              onsubmit="return confirm('Ngừng bán sản phẩm này? Khách sẽ không đặt được nữa.');"><div class="card-body">
          <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="id" value="${product.id}">
          <h2 class="h6 text-uppercase text-muted">Ngừng bán</h2>
          <p class="small text-muted">Sản phẩm không bị xoá: các đơn cũ vẫn nhắc đến nó, nhưng khách không thể mua thêm.</p>
          <button class="btn btn-outline-danger btn-sm">Ngừng bán sản phẩm</button>
        </div></form>
      </c:if>
    </div>

    <div class="col-12 mb-4">
      <div class="card"><div class="card-body">
        <h2 class="h6 text-uppercase text-muted">Biến thể (size × màu) và tồn kho</h2>
        <div class="table-responsive">
          <table class="table table-sm">
            <thead><tr><th>Màu</th><th>Size</th><th class="text-center">Tồn kho</th><th class="text-center">Đang giữ cho đơn chờ</th>
              <th class="text-center">Còn bán</th><th>Nhập thêm hàng</th></tr></thead>
            <tbody>
              <c:forEach items="${product.variants}" var="v">
                <tr>
                  <td><span class="d-inline-block rounded-circle border mr-1" style="width:12px;height:12px;background:${f:colorHex(v.color)}"></span>
                    <c:out value="${v.color}"/></td>
                  <td>${fn:escapeXml(v.size)}</td>
                  <td class="text-center">${v.stockQuantity}</td>
                  <td class="text-center">${v.reservedQuantity}</td>
                  <td class="text-center ${v.availableQuantity == 0 ? 'text-danger' : ''}">${v.availableQuantity}</td>
                  <td>
                    <form method="post" action="${ctx}/admin/products/restock" class="form-inline">
                      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                      <input type="hidden" name="id" value="${product.id}"><input type="hidden" name="variantId" value="${v.id}">
                      <input type="number" name="qty" min="1" value="10" class="form-control form-control-sm mr-2" style="width: 80px;">
                      <button class="btn btn-sm btn-outline-primary">Nhập kho</button>
                    </form>
                  </td>
                </tr>
              </c:forEach>
              <c:if test="${empty product.variants}">
                <tr><td colspan="6" class="text-muted">Chưa có biến thể — khách chưa thể mua sản phẩm này.</td></tr>
              </c:if>
            </tbody>
          </table>
        </div>
        <form method="post" action="${ctx}/admin/products/variant" class="form-inline mt-2">
          <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="id" value="${product.id}">
          <strong class="mr-2 small text-uppercase text-muted">Thêm biến thể:</strong>
          <input name="color" required maxlength="30" class="form-control form-control-sm mr-2 mb-1" placeholder="Màu (vd: Đen)">
          <input name="size" required maxlength="10" class="form-control form-control-sm mr-2 mb-1" placeholder="Size (vd: M)" style="width: 110px;">
          <input type="number" name="stock" min="0" value="0" class="form-control form-control-sm mr-2 mb-1" style="width: 90px;" title="Tồn kho ban đầu">
          <button class="btn btn-sm btn-primary mb-1">Thêm</button>
        </form>
      </div></div>
    </div>
  </c:if>
</div>

<%@ include file="/WEB-INF/views/admin/bottom.jspf" %>
</body>
</html>
