<c:set var="pageTitle" value="Sổ địa chỉ"/>
<c:set var="accountNav" value="addresses"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="bg-light py-3">
  <div class="container">
    <div class="row">
      <div class="col-md-12 mb-0">
        <a href="${ctx}/">Trang chủ</a> <span class="mx-2 mb-0">/</span>
        <a href="${ctx}/account/profile">Tài khoản</a> <span class="mx-2 mb-0">/</span>
        <strong class="text-black">Sổ địa chỉ</strong>
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
        <h2 class="h4 text-black mb-3">Địa chỉ đã lưu (${fn:length(book.addresses)})</h2>
        <c:if test="${empty book.addresses}">
          <div class="alert alert-info">Bạn chưa lưu địa chỉ nào. Thêm địa chỉ để đặt hàng nhanh hơn.</div>
        </c:if>
        <div class="row">
          <c:forEach items="${book.addresses}" var="a">
            <div class="col-lg-6 mb-4">
              <div class="border rounded p-4 h-100 ${book.isDefault(a) ? 'border-primary' : ''}">
                <div class="d-flex justify-content-between align-items-start">
                  <h3 class="h6 text-black mb-1"><c:out value="${a.recipientName}"/></h3>
                  <c:if test="${book.isDefault(a)}"><span class="badge badge-primary">Mặc định</span></c:if>
                </div>
                <p class="mb-1"><c:out value="${a.phone}"/></p>
                <p class="mb-3 text-muted"><c:out value="${a.fullAddress}"/></p>
                <div class="d-flex flex-wrap" style="gap: 6px;">
                  <c:if test="${not book.isDefault(a)}">
                    <form method="post" action="${ctx}/account/addresses" class="d-inline">
                      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                      <input type="hidden" name="action" value="default">
                      <input type="hidden" name="id" value="${a.id}">
                      <button class="btn btn-sm btn-outline-primary">Đặt mặc định</button>
                    </form>
                  </c:if>
                  <a href="${ctx}/account/addresses?edit=${a.id}#address-form" class="btn btn-sm btn-outline-secondary">Sửa</a>
                  <form method="post" action="${ctx}/account/addresses" class="d-inline"
                        onsubmit="return confirm('Xoá địa chỉ này khỏi sổ?');">
                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="id" value="${a.id}">
                    <button class="btn btn-sm btn-outline-danger">Xoá</button>
                  </form>
                </div>
              </div>
            </div>
          </c:forEach>
        </div>

        <div id="address-form" class="border rounded p-4 mt-2">
          <c:set var="addr" value="${editing}"/>
          <h3 class="h5 text-black mb-3">${empty editing ? 'Thêm địa chỉ mới' : 'Sửa địa chỉ'}</h3>
          <c:if test="${not empty editing}">
            <p class="text-muted small">Địa chỉ được lưu như một bản ghi bất biến: khi sửa, địa chỉ cũ được thay bằng
              địa chỉ mới. Các đơn hàng cũ vẫn giữ nguyên nơi đã giao.</p>
          </c:if>
          <form method="post" action="${ctx}/account/addresses">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
            <input type="hidden" name="action" value="${empty editing ? 'add' : 'replace'}">
            <c:if test="${not empty editing}"><input type="hidden" name="id" value="${editing.id}"></c:if>
            <%@ include file="/WEB-INF/views/common/address-fields.jspf" %>
            <c:if test="${empty editing}">
              <div class="form-group form-check">
                <input type="checkbox" class="form-check-input" id="makeDefault" name="makeDefault" value="1">
                <label class="form-check-label" for="makeDefault">Đặt làm địa chỉ mặc định</label>
              </div>
            </c:if>
            <button type="submit" class="btn btn-primary">${empty editing ? 'Thêm địa chỉ' : 'Lưu địa chỉ'}</button>
            <c:if test="${not empty editing}">
              <a href="${ctx}/account/addresses" class="btn btn-link">Huỷ</a>
            </c:if>
          </form>
        </div>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
