<c:set var="pageTitle" value="Voucher"/>
<c:set var="adminNav" value="vouchers"/>
<%@ include file="/WEB-INF/views/admin/top.jspf" %>

<h1 class="h3 text-black mb-1">Voucher</h1>
<p class="text-muted">Mã khách tự nhập ở bước thanh toán để giảm trên cả đơn. Lượt dùng được ghi khi đặt hàng và trả lại khi
  đơn bị huỷ hoặc giao thất bại.</p>
<div class="row">
  <div class="col-xl-8 mb-4">
    <div class="card"><div class="card-body p-0">
      <div class="table-responsive">
        <table class="table mb-0">
          <thead class="thead-light"><tr><th>Mã</th><th>Mức giảm</th><th>Điều kiện</th><th>Hiệu lực</th>
            <th class="text-center">Đã dùng</th><th>Trạng thái</th><th></th></tr></thead>
          <tbody>
            <c:forEach items="${vouchers}" var="v">
              <tr>
                <td class="font-weight-bold">${v.code}</td>
                <td class="small"><c:out value="${v.policy.describe()}"/></td>
                <td class="small">Đơn từ ${f:vnd(v.minOrderValue)}<br>Hạng: ${v.tierLabels}<br>Tối đa ${v.perCustomerLimit} lượt/khách</td>
                <td class="small">${f:dateTime(v.period.start)}<br>→ ${f:dateTime(v.period.end)}</td>
                <td class="text-center">${v.usedCount}/${v.usageLimit}</td>
                <td>
                  <c:choose>
                    <c:when test="${not v.active}"><span class="badge badge-secondary">Đã tắt</span></c:when>
                    <c:when test="${v.isValid(now)}"><span class="badge badge-success">Đang hiệu lực</span></c:when>
                    <c:when test="${v.period.isUpcoming(now)}"><span class="badge badge-info">Chưa bắt đầu</span></c:when>
                    <c:when test="${v.usedCount >= v.usageLimit}"><span class="badge badge-warning">Hết lượt</span></c:when>
                    <c:otherwise><span class="badge badge-dark">Hết hạn</span></c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <form method="post" action="${ctx}/admin/vouchers">
                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="action" value="toggle"><input type="hidden" name="id" value="${v.id}">
                    <button class="btn btn-sm ${v.active ? 'btn-outline-danger' : 'btn-outline-success'}">${v.active ? 'Tắt' : 'Bật'}</button>
                  </form>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </div></div>
  </div>
  <div class="col-xl-4">
    <form method="post" action="${ctx}/admin/vouchers" class="card"><div class="card-body">
      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
      <input type="hidden" name="action" value="create">
      <h2 class="h6 text-uppercase text-muted">Tạo voucher</h2>
      <div class="form-group">
        <label class="form-required" for="code">Mã</label>
        <input class="form-control text-uppercase" id="code" name="code" required maxlength="30" pattern="[A-Za-z0-9_\-]{3,30}" placeholder="SALE1111">
      </div>
      <div class="form-group">
        <label class="form-required">Hạng khách được dùng</label><br>
        <c:forEach items="${levels}" var="l">
          <div class="form-check form-check-inline">
            <input class="form-check-input" type="checkbox" id="tier_${l}" name="tiers" value="${l}" checked>
            <label class="form-check-label" for="tier_${l}">${l.label}</label>
          </div>
        </c:forEach>
      </div>
      <div class="form-row">
        <div class="form-group col-6">
          <label for="minOrderValue">Đơn tối thiểu (₫)</label>
          <input class="form-control" id="minOrderValue" name="minOrderValue" value="0">
        </div>
        <div class="form-group col-3">
          <label class="form-required" for="usageLimit">Tổng lượt</label>
          <input type="number" min="1" class="form-control" id="usageLimit" name="usageLimit" value="100" required>
        </div>
        <div class="form-group col-3">
          <label class="form-required" for="perCustomerLimit">Lượt/khách</label>
          <input type="number" min="1" class="form-control" id="perCustomerLimit" name="perCustomerLimit" value="1" required>
        </div>
      </div>
      <%@ include file="/WEB-INF/views/admin/policy-fields.jspf" %>
      <button class="btn btn-primary">Tạo voucher</button>
    </div></form>
  </div>
</div>

<%@ include file="/WEB-INF/views/admin/bottom.jspf" %>
<script src="${ctx}/js/admin.js"></script>
</body>
</html>
