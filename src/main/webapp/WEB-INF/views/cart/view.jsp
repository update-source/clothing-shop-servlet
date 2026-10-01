<c:set var="pageTitle" value="Giỏ hàng"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="bg-light py-3">
  <div class="container">
    <div class="row">
      <div class="col-md-12 mb-0"><a href="${ctx}/">Trang chủ</a> <span class="mx-2 mb-0">/</span> <strong class="text-black">Giỏ hàng</strong></div>
    </div>
  </div>
</div>

<div class="site-section">
  <div class="container">
    <c:choose>
      <c:when test="${empty cart.items}">
        <div class="row">
          <div class="col-md-12 text-center py-5">
            <span class="icon-shopping_cart display-3 text-muted"></span>
            <h2 class="h4 text-black mt-3">Giỏ hàng đang trống</h2>
            <p class="mb-4">Hãy chọn vài món bạn thích nhé.</p>
            <a href="${ctx}/shop" class="btn btn-primary btn-sm">Tiếp tục mua sắm</a>
          </div>
        </div>
      </c:when>
      <c:otherwise>
        <form id="cart-form" action="${ctx}/cart/update" method="post">
          <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
          <div class="row mb-5">
            <div class="col-md-12">
              <div class="site-blocks-table">
                <table class="table table-bordered">
                  <thead>
                    <tr>
                      <th class="product-thumbnail">Ảnh</th>
                      <th class="product-name">Sản phẩm</th>
                      <th class="product-price">Đơn giá</th>
                      <th class="product-quantity">Số lượng</th>
                      <th class="product-total">Thành tiền</th>
                      <th class="product-remove">Bỏ</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:forEach items="${cart.items}" var="item">
                      <c:set var="v" value="${item.variant}"/>
                      <c:set var="p" value="${v.product}"/>
                      <tr>
                        <td class="product-thumbnail">
                          <a href="${ctx}/product?id=${p.id}">
                            <img src="${ctx}/${empty p.imageUrl ? 'images/cloth_1.jpg' : p.imageUrl}" alt="" class="img-fluid">
                          </a>
                        </td>
                        <td class="product-name">
                          <h2 class="h5 text-black mb-1"><a href="${ctx}/product?id=${p.id}" class="text-black"><c:out value="${p.name}"/></a></h2>
                          <small class="text-muted">Màu <c:out value="${v.color}"/> · Size ${fn:escapeXml(v.size)}</small>
                          <c:choose>
                            <c:when test="${not p.active}">
                              <div class="text-danger small">Sản phẩm đã ngừng bán — vui lòng bỏ khỏi giỏ</div>
                            </c:when>
                            <c:when test="${v.availableQuantity == 0}">
                              <div class="text-danger small">Đã hết hàng</div>
                            </c:when>
                            <c:when test="${item.quantity > v.availableQuantity}">
                              <div class="text-danger small">Chỉ còn ${v.availableQuantity} sản phẩm</div>
                            </c:when>
                          </c:choose>
                        </td>
                        <td>
                          <c:if test="${p.isOnSale(now)}"><span class="price-old d-block">${f:vnd(p.basePrice)}</span></c:if>
                          ${f:vnd(item.unitPrice)}
                        </td>
                        <td>
                          <div class="input-group mb-0 mx-auto" style="max-width: 130px;">
                            <div class="input-group-prepend">
                              <button class="btn btn-outline-primary js-btn-minus" type="button">&minus;</button>
                            </div>
                            <input type="number" class="form-control text-center" name="qty_${v.id}" value="${item.quantity}"
                                   min="0" data-min="0" data-max="${v.availableQuantity}" aria-label="Số lượng">
                            <div class="input-group-append">
                              <button class="btn btn-outline-primary js-btn-plus" type="button">&plus;</button>
                            </div>
                          </div>
                        </td>
                        <td>${f:vnd(item.subtotal)}</td>
                        <td>
                          <button type="submit" formaction="${ctx}/cart/remove?variantId=${v.id}"
                                  class="btn btn-primary btn-sm" title="Bỏ khỏi giỏ">X</button>
                        </td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div class="row">
            <div class="col-md-6">
              <div class="row mb-5">
                <div class="col-md-6 mb-3 mb-md-0">
                  <button type="submit" class="btn btn-primary btn-sm btn-block">Cập nhật giỏ hàng</button>
                </div>
                <div class="col-md-6">
                  <a href="${ctx}/shop" class="btn btn-outline-primary btn-sm btn-block">Tiếp tục mua sắm</a>
                </div>
              </div>
              <div class="row">
                <div class="col-md-12">
                  <label class="text-black h4">Mã giảm giá</label>
                  <p>Bạn sẽ nhập mã giảm giá (nếu có) ở bước thanh toán, cùng lúc với địa chỉ giao hàng và phương thức
                    thanh toán. Giá trong giỏ luôn theo giá hiện tại và chỉ được chốt khi bạn đặt hàng.</p>
                </div>
              </div>
            </div>
            <div class="col-md-6 pl-5">
              <div class="row justify-content-end">
                <div class="col-md-7">
                  <div class="row">
                    <div class="col-md-12 text-right border-bottom mb-5">
                      <h3 class="text-black h4 text-uppercase">Tổng giỏ hàng</h3>
                    </div>
                  </div>
                  <div class="row mb-3">
                    <div class="col-md-6"><span class="text-black">Số món</span></div>
                    <div class="col-md-6 text-right"><strong class="text-black">${cart.itemCount}</strong></div>
                  </div>
                  <div class="row mb-5">
                    <div class="col-md-6"><span class="text-black">Tạm tính</span></div>
                    <div class="col-md-6 text-right"><strong class="text-black">${f:vnd(cart.total)}</strong></div>
                  </div>
                  <div class="row">
                    <div class="col-md-12">
                      <c:choose>
                        <c:when test="${hasIssues}">
                          <button type="button" class="btn btn-secondary btn-lg py-3 btn-block" disabled>Tiến hành thanh toán</button>
                          <small class="text-danger d-block mt-2">Vui lòng điều chỉnh các sản phẩm được đánh dấu đỏ.</small>
                        </c:when>
                        <c:otherwise>
                          <a href="${ctx}/checkout" class="btn btn-primary btn-lg py-3 btn-block">Tiến hành thanh toán</a>
                        </c:otherwise>
                      </c:choose>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </form>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
