<c:set var="pageTitle" value="Thanh toán"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>
<c:set var="book" value="${preview.book}"/>
<c:set var="hasSaved" value="${not empty book.addresses}"/>
<c:set var="useNew" value="${not hasSaved or param.addressId == 'new'}"/>

<div class="bg-light py-3">
  <div class="container">
    <div class="row">
      <div class="col-md-12 mb-0">
        <a href="${ctx}/">Trang chủ</a> <span class="mx-2 mb-0">/</span>
        <a href="${ctx}/cart">Giỏ hàng</a> <span class="mx-2 mb-0">/</span>
        <strong class="text-black">Thanh toán</strong>
      </div>
    </div>
  </div>
</div>

<div class="site-section">
  <div class="container">
    <c:if test="${not empty error}">
      <div class="alert alert-danger"><c:out value="${error}"/></div>
    </c:if>
    <form id="checkout-form" action="${ctx}/checkout" method="post">
      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
      <div class="row">
        <div class="col-md-6 mb-5 mb-md-0">
          <h2 class="h3 mb-3 text-black">Địa chỉ giao hàng</h2>
          <div class="p-3 p-lg-5 border">
            <c:forEach items="${book.addresses}" var="a">
              <div class="custom-control custom-radio mb-3">
                <input type="radio" id="addr_${a.id}" name="addressId" value="${a.id}" class="custom-control-input js-address"
                       data-province="${fn:escapeXml(a.province)}"
                       ${not useNew and preview.address.id == a.id ? 'checked' : ''}>
                <label class="custom-control-label text-black" for="addr_${a.id}">
                  <strong><c:out value="${a.recipientName}"/></strong> · <c:out value="${a.phone}"/>
                  <c:if test="${book.isDefault(a)}"><span class="badge badge-primary ml-1">Mặc định</span></c:if><br>
                  <span class="text-muted"><c:out value="${a.fullAddress}"/></span>
                </label>
              </div>
            </c:forEach>
            <div class="custom-control custom-radio mb-3 ${hasSaved ? '' : 'd-none'}">
              <input type="radio" id="addr_new" name="addressId" value="new" class="custom-control-input js-address"
                     ${useNew ? 'checked' : ''}>
              <label class="custom-control-label text-black" for="addr_new">Giao đến địa chỉ khác</label>
            </div>

            <fieldset id="new-address" class="${useNew ? '' : 'd-none'}" ${useNew ? '' : 'disabled'}>
              <%@ include file="/WEB-INF/views/common/address-fields.jspf" %>
              <div class="form-group form-check">
                <input type="checkbox" class="form-check-input" id="saveAddress" name="saveAddress" value="1" checked>
                <label class="form-check-label" for="saveAddress">Lưu địa chỉ này vào sổ địa chỉ</label>
              </div>
            </fieldset>

            <div class="form-group mb-0">
              <label for="note" class="text-black">Ghi chú đơn hàng</label>
              <textarea name="note" id="note" rows="4" maxlength="500" class="form-control"
                        placeholder="Ví dụ: giao giờ hành chính, gọi trước khi giao...">${fn:escapeXml(param.note)}</textarea>
            </div>
          </div>
        </div>

        <div class="col-md-6">
          <div class="row mb-5">
            <div class="col-md-12">
              <h2 class="h3 mb-3 text-black">Mã giảm giá</h2>
              <div class="p-3 p-lg-5 border">
                <label for="voucherCode" class="text-black mb-3">Nhập mã giảm giá nếu bạn có</label>
                <div class="input-group w-75">
                  <input type="text" class="form-control text-uppercase" id="voucherCode" name="voucherCode"
                         value="${fn:escapeXml(preview.voucherCode)}" placeholder="Mã giảm giá" maxlength="30">
                  <div class="input-group-append">
                    <button class="btn btn-primary btn-sm" type="button" id="apply-voucher">Áp dụng</button>
                  </div>
                </div>
                <c:choose>
                  <c:when test="${not empty preview.voucherError}">
                    <small class="text-danger d-block mt-2"><c:out value="${preview.voucherError}"/></small>
                  </c:when>
                  <c:when test="${not empty preview.voucher}">
                    <small class="text-success d-block mt-2">Đã áp dụng mã ${preview.voucher.code}:
                      <c:out value="${preview.voucher.policy.describe()}"/></small>
                  </c:when>
                </c:choose>
                <c:if test="${not empty preview.usableVouchers}">
                  <div class="mt-3">
                    <small class="text-muted d-block mb-1">Mã bạn có thể dùng cho giỏ hàng này:</small>
                    <c:forEach items="${preview.usableVouchers}" var="v">
                      <button type="button" class="btn btn-outline-secondary btn-sm mb-1 js-pick-voucher" data-code="${v.code}">
                        ${v.code} · <c:out value="${v.policy.describe()}"/>
                      </button>
                    </c:forEach>
                  </div>
                </c:if>
              </div>
            </div>
          </div>

          <div class="row mb-5">
            <div class="col-md-12">
              <h2 class="h3 mb-3 text-black">Đơn hàng của bạn</h2>
              <div class="p-3 p-lg-5 border">
                <table class="table site-block-order-table mb-5">
                  <thead><tr><th>Sản phẩm</th><th class="text-right">Thành tiền</th></tr></thead>
                  <tbody>
                    <c:forEach items="${preview.cart.items}" var="item">
                      <tr>
                        <td><c:out value="${item.variant.product.name}"/>
                          <small class="text-muted">(<c:out value="${item.variant.label}"/>)</small>
                          <strong class="mx-2">x</strong> ${item.quantity}</td>
                        <td class="text-right">${f:vnd(item.subtotal)}</td>
                      </tr>
                    </c:forEach>
                    <tr>
                      <td class="text-black font-weight-bold"><strong>Tạm tính</strong></td>
                      <td class="text-black text-right">${f:vnd(preview.subtotal)}</td>
                    </tr>
                    <tr>
                      <td class="text-black">Giảm giá <c:if test="${not empty preview.voucher}">(${preview.voucher.code})</c:if></td>
                      <td class="text-black text-right">− ${f:vnd(preview.discount)}</td>
                    </tr>
                    <tr>
                      <td class="text-black">Phí vận chuyển <small class="text-muted d-block" id="ship-to"></small></td>
                      <td class="text-black text-right" id="shipping-fee">${f:vnd(preview.shippingFee)}</td>
                    </tr>
                    <tr>
                      <td class="text-black font-weight-bold"><strong>Tổng cộng</strong></td>
                      <td class="text-black font-weight-bold text-right"><strong id="order-total">${f:vnd(preview.total)}</strong></td>
                    </tr>
                  </tbody>
                </table>

                <c:forEach items="${methods}" var="m" varStatus="st">
                  <div class="border p-3 mb-3">
                    <div class="custom-control custom-radio">
                      <input type="radio" id="pm_${m}" name="paymentMethod" value="${m}" class="custom-control-input"
                             ${(empty param.paymentMethod and st.first) or param.paymentMethod == m.name() ? 'checked' : ''}>
                      <label class="custom-control-label h6 mb-0 text-black" for="pm_${m}">${m.label}</label>
                    </div>
                    <p class="mb-0 mt-2 small text-muted">
                      <c:choose>
                        <c:when test="${m == 'COD'}">Bạn trả tiền mặt cho shipper khi nhận hàng. Từ chối nhận hàng thì đơn
                          chuyển "Giao thất bại", không mất phí.</c:when>
                        <c:otherwise>Thanh toán trước qua cổng VNPAY (thẻ ATM, QR, ví). Đơn chỉ được xác nhận sau khi thanh
                          toán thành công; quá hạn chưa thanh toán đơn sẽ tự huỷ.</c:otherwise>
                      </c:choose>
                    </p>
                  </div>
                </c:forEach>

                <div class="form-group mt-4">
                  <button type="submit" class="btn btn-primary btn-lg py-3 btn-block">Đặt hàng</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </form>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
<script>
  (function ($) {
    var fees = {
      <c:forEach items="${shippingTable}" var="e" varStatus="st">"${e.key}": ${f:plain(e.value)}${st.last ? '' : ','}</c:forEach>
    };
    var subtotal = ${f:plain(preview.subtotal)}, discount = ${f:plain(preview.discount)};
    function vnd(v) { return v.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.') + ' ₫'; }
    function currentProvince() {
      var picked = $('input.js-address:checked');
      if (!picked.length || picked.val() === 'new') { return $('#province').val(); }
      return picked.data('province');
    }
    function refresh() {
      var useNew = $('#addr_new').is(':checked') || !$('input.js-address').not('#addr_new').length;
      $('#new-address').toggleClass('d-none', !useNew).prop('disabled', !useNew);
      var province = currentProvince();
      var fee = province && fees[province] !== undefined ? fees[province] : null;
      $('#ship-to').text(province ? 'Giao đến ' + province : 'Chọn tỉnh/thành để tính phí');
      $('#shipping-fee').text(fee === null ? '—' : vnd(fee));
      $('#order-total').text(vnd(subtotal - discount + (fee || 0)));
    }
    function reloadWithVoucher(code) {
      var picked = $('input.js-address:checked').val();
      var url = '${ctx}/checkout?voucher=' + encodeURIComponent(code || '');
      if (picked) { url += '&addressId=' + encodeURIComponent(picked); }
      window.location = url;
    }
    $('input.js-address').on('change', refresh);
    $(document).on('change', '#province', refresh);
    $('#apply-voucher').on('click', function () { reloadWithVoucher($('#voucherCode').val().trim()); });
    $('.js-pick-voucher').on('click', function () { reloadWithVoucher($(this).data('code')); });
    $('#voucherCode').on('keydown', function (e) {
      if (e.key === 'Enter') { e.preventDefault(); reloadWithVoucher($(this).val().trim()); }
    });
    refresh();
  })(jQuery);
</script>
</body>
</html>
