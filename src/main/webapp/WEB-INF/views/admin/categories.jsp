<c:set var="pageTitle" value="Danh mục"/>
<c:set var="adminNav" value="categories"/>
<%@ include file="/WEB-INF/views/admin/top.jspf" %>

<h1 class="h3 text-black mb-4">Danh mục</h1>
<div class="row">
  <div class="col-lg-7 mb-4">
    <div class="card"><div class="card-body p-0">
      <table class="table mb-0">
        <thead class="thead-light"><tr><th>Tên đầy đủ</th><th class="text-center">Sản phẩm đang bán</th><th></th></tr></thead>
        <tbody>
          <c:forEach items="${categories}" var="cat">
            <tr>
              <td class="${empty cat.parent ? 'font-weight-bold' : ''}"><c:out value="${cat.fullName}"/></td>
              <td class="text-center">${empty counts[cat.id] ? 0 : counts[cat.id]}</td>
              <td class="text-right text-nowrap">
                <a href="${ctx}/admin/categories?edit=${cat.id}" class="btn btn-sm btn-outline-secondary">Sửa</a>
                <form method="post" action="${ctx}/admin/categories" class="d-inline" onsubmit="return confirm('Xoá danh mục này?');">
                  <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                  <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${cat.id}">
                  <button class="btn btn-sm btn-outline-danger">Xoá</button>
                </form>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div></div>
  </div>
  <div class="col-lg-5">
    <form method="post" action="${ctx}/admin/categories" class="card"><div class="card-body">
      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
      <input type="hidden" name="action" value="${empty editing ? 'create' : 'update'}">
      <c:if test="${not empty editing}"><input type="hidden" name="id" value="${editing.id}"></c:if>
      <h2 class="h6 text-uppercase text-muted">${empty editing ? 'Thêm danh mục' : 'Sửa danh mục'}</h2>
      <div class="form-group">
        <label class="form-required" for="name">Tên danh mục</label>
        <input class="form-control" id="name" name="name" required maxlength="100" value="${fn:escapeXml(editing.name)}">
      </div>
      <div class="form-group">
        <label for="parentId">Danh mục cha</label>
        <select class="form-control" id="parentId" name="parentId">
          <option value="">— Không có (danh mục gốc) —</option>
          <c:forEach items="${categories}" var="cat">
            <c:if test="${empty editing or cat.id != editing.id}">
              <option value="${cat.id}" ${editing.parent.id == cat.id ? 'selected' : ''}><c:out value="${cat.fullName}"/></option>
            </c:if>
          </c:forEach>
        </select>
      </div>
      <button class="btn btn-primary">${empty editing ? 'Thêm' : 'Lưu'}</button>
      <c:if test="${not empty editing}"><a href="${ctx}/admin/categories" class="btn btn-link">Huỷ</a></c:if>
    </div></form>
  </div>
</div>

<%@ include file="/WEB-INF/views/admin/bottom.jspf" %>
</body>
</html>
