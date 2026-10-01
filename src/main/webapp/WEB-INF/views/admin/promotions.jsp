<c:set var="pageTitle" value="Khuyến mãi"/>
<c:set var="adminNav" value="promotions"/>
<%@ include file="/WEB-INF/views/admin/top.jspf" %>

<h1 class="h3 text-black mb-1">Chương trình khuyến mãi</h1>
<p class="text-muted">Cửa hàng tự áp lên giá từng sản phẩm, khách không cần nhập mã. Nhiều chương trình chồng nhau thì sản phẩm
  lấy mức giảm cao nhất.</p>
<div class="row">
  <div class="col-xl-7 mb-4">
    <div class="card"><div class="card-body p-0">
      <table class="table mb-0">
        <thead class="thead-light"><tr><th>Tên</th><th>Mức giảm</th><th>Thời gian</th><th class="text-center">SP</th><th>Trạng thái</th><th></th></tr></thead>
        <tbody>
          <c:if test="${empty promotions}"><tr><td colspan="6" class="text-muted text-center">Chưa có chương trình.</td></tr></c:if>
          <c:forEach items="${promotions}" var="p">
            <tr>
              <td><a href="${ctx}/admin/promotions/edit?id=${p.id}" class="font-weight-bold"><c:out value="${p.name}"/></a></td>
              <td class="small"><c:out value="${p.policy.describe()}"/></td>
              <td class="small">${f:dateTime(p.period.start)}<br>→ ${f:dateTime(p.period.end)}</td>
              <td class="text-center">${fn:length(p.products)}</td>
              <td>
                <c:choose>
                  <c:when test="${p.isOngoing(now)}"><span class="badge badge-success">Đang chạy</span></c:when>
                  <c:when test="${p.period.isUpcoming(now)}"><span class="badge badge-info">Sắp diễn ra</span></c:when>
                  <c:otherwise><span class="badge badge-secondary">Đã kết thúc</span></c:otherwise>
                </c:choose>
              </td>
              <td><a href="${ctx}/admin/promotions/edit?id=${p.id}" class="btn btn-sm btn-outline-secondary">Quản lý</a></td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div></div>
  </div>
  <div class="col-xl-5">
    <form method="post" action="${ctx}/admin/promotions" class="card"><div class="card-body">
      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
      <input type="hidden" name="action" value="create">
      <h2 class="h6 text-uppercase text-muted">Tạo chương trình</h2>
      <div class="form-group">
        <label class="form-required" for="name">Tên chương trình</label>
        <input class="form-control" id="name" name="name" required maxlength="150" placeholder="Sale 11.11">
      </div>
      <%@ include file="/WEB-INF/views/admin/policy-fields.jspf" %>
      <button class="btn btn-primary">Tạo chương trình</button>
    </div></form>
  </div>
</div>

<%@ include file="/WEB-INF/views/admin/bottom.jspf" %>
<script src="${ctx}/js/admin.js"></script>
</body>
</html>
