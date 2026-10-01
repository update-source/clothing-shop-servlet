<c:set var="pageTitle" value="${product.name}"/>
<c:set var="activeNav" value="shop"/>
<c:set var="onSale" value="${product.isOnSale(now)}"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="bg-light py-3">
  <div class="container">
    <div class="row">
      <div class="col-md-12 mb-0">
        <a href="${ctx}/">Trang chủ</a> <span class="mx-2 mb-0">/</span>
        <a href="${ctx}/shop">Cửa hàng</a> <span class="mx-2 mb-0">/</span>
        <a href="${ctx}/shop?category=${product.category.id}"><c:out value="${product.category.name}"/></a>
        <span class="mx-2 mb-0">/</span> <strong class="text-black"><c:out value="${product.name}"/></strong>
      </div>
    </div>
  </div>
</div>

<div class="site-section">
  <div class="container">
    <div class="row">
      <div class="col-md-6 mb-4">
        <img src="${ctx}/${empty product.imageUrl ? 'images/cloth_1.jpg' : product.imageUrl}"
             alt="${fn:escapeXml(product.name)}" class="img-fluid rounded">
      </div>
      <div class="col-md-6">
        <h2 class="text-black"><c:out value="${product.name}"/></h2>
        <p class="mb-2">
          <c:choose>
            <c:when test="${not empty product.reviews}">
              <span class="rating-stars">${f:stars(product.averageRating)}</span>
              <span class="text-muted">${f:rating(product.averageRating)}/5 · ${fn:length(product.reviews)} đánh giá</span>
            </c:when>
            <c:otherwise><span class="text-muted">Chưa có đánh giá</span></c:otherwise>
          </c:choose>
        </p>
        <p class="mb-2">
          <strong class="text-primary h4">${f:vnd(product.getFinalPrice(now))}</strong>
          <c:if test="${onSale}"><span class="price-old ml-2">${f:vnd(product.basePrice)}</span></c:if>
        </p>
        <c:forEach items="${product.promotions}" var="promo">
          <c:if test="${promo.isOngoing(now)}">
            <span class="badge badge-danger mb-2"><c:out value="${promo.name}"/></span>
          </c:if>
        </c:forEach>
        <p class="mt-2" style="white-space: pre-line;"><c:out value="${product.description}"/></p>

        <c:choose>
          <c:when test="${not product.active}">
            <div class="alert alert-secondary">Sản phẩm này đã ngừng bán.</div>
          </c:when>
          <c:otherwise>
            <ul id="variant-data" class="d-none">
              <c:forEach items="${product.variants}" var="v">
                <li data-id="${v.id}" data-size="${fn:escapeXml(v.size)}" data-color="${fn:escapeXml(v.color)}"
                    data-available="${v.availableQuantity}"></li>
              </c:forEach>
            </ul>
            <form action="${ctx}/cart/add" method="post">
              <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
              <input type="hidden" name="variantId" id="variantId" value="">
              <div class="mb-3">
                <h3 class="h6 text-black mb-2">Màu sắc</h3>
                <div class="variant-options">
                  <c:forEach items="${product.colors}" var="col" varStatus="st">
                    <input type="radio" name="pickColor" id="col_${st.index}" value="${fn:escapeXml(col)}">
                    <label for="col_${st.index}">
                      <span class="d-inline-block rounded-circle border mr-1"
                            style="width:12px;height:12px;background:${f:colorHex(col)}"></span><c:out value="${col}"/>
                    </label>
                  </c:forEach>
                </div>
              </div>
              <div class="mb-3">
                <h3 class="h6 text-black mb-2">Size</h3>
                <div class="variant-options">
                  <c:forEach items="${product.sizes}" var="s" varStatus="st">
                    <input type="radio" name="pickSize" id="size_${st.index}" value="${fn:escapeXml(s)}">
                    <label for="size_${st.index}">${fn:escapeXml(s)}</label>
                  </c:forEach>
                </div>
              </div>
              <p id="stock-info" class="text-muted small mb-3">Vui lòng chọn màu và size</p>
              <div class="mb-4">
                <div class="input-group mb-3" style="max-width: 140px;">
                  <div class="input-group-prepend">
                    <button class="btn btn-outline-primary js-btn-minus" type="button">&minus;</button>
                  </div>
                  <input type="number" id="qty" name="qty" class="form-control text-center" value="1" min="1"
                         data-min="1" aria-label="Số lượng">
                  <div class="input-group-append">
                    <button class="btn btn-outline-primary js-btn-plus" type="button">&plus;</button>
                  </div>
                </div>
              </div>
              <p class="d-flex flex-wrap" style="gap: 8px;">
                <button type="submit" id="add-to-cart" class="buy-now btn btn-sm btn-primary" disabled>Thêm vào giỏ</button>
                <button type="submit" formaction="${ctx}/cart/add?buyNow=1" class="btn btn-sm btn-outline-primary"
                        id="buy-now" disabled>Mua ngay</button>
              </p>
            </form>
            <form action="${ctx}/wishlist/toggle" method="post" class="mb-3">
              <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
              <input type="hidden" name="productId" value="${product.id}">
              <button type="submit" class="btn btn-link p-0">
                <span class="icon ${inWishlist ? 'icon-heart text-danger' : 'icon-heart-o'}"></span>
                ${inWishlist ? 'Bỏ khỏi yêu thích' : 'Thêm vào yêu thích'}
              </button>
            </form>
          </c:otherwise>
        </c:choose>

        <details class="mt-3">
          <summary class="text-black">Tình trạng kho theo biến thể</summary>
          <table class="table table-sm mt-2">
            <thead><tr><th>Màu</th><th>Size</th><th class="text-right">Còn bán được</th></tr></thead>
            <tbody>
              <c:forEach items="${product.variants}" var="v">
                <tr>
                  <td><c:out value="${v.color}"/></td>
                  <td>${fn:escapeXml(v.size)}</td>
                  <td class="text-right ${v.availableQuantity == 0 ? 'text-muted' : ''}">
                    ${v.availableQuantity == 0 ? 'Hết hàng' : v.availableQuantity}
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </details>
      </div>
    </div>

    <div class="row mt-5" id="reviews">
      <div class="col-md-12">
        <h3 class="h4 text-black mb-3">Đánh giá sản phẩm</h3>
        <%@ include file="/WEB-INF/views/shop/review-form.jspf" %>
        <c:if test="${empty product.reviews}">
          <p class="text-muted">Chưa có đánh giá nào. Chỉ khách đã mua và nhận hàng mới có thể đánh giá.</p>
        </c:if>
        <c:forEach items="${product.reviews}" var="r">
          <div class="review-item">
            <div class="d-flex justify-content-between">
              <strong class="text-black"><c:out value="${r.author.fullName}"/></strong>
              <small class="text-muted">${f:dateTime(empty r.updatedAt ? r.createdAt : r.updatedAt)}
                <c:if test="${not empty r.updatedAt}">(đã sửa)</c:if></small>
            </div>
            <div class="rating-stars">${f:stars(r.rating)}</div>
            <p class="mb-0" style="white-space: pre-line;"><c:out value="${r.comment}"/></p>
          </div>
        </c:forEach>
      </div>
    </div>
  </div>
</div>

<c:if test="${not empty related}">
<div class="site-section block-3 site-blocks-2 bg-light">
  <div class="container">
    <div class="row justify-content-center">
      <div class="col-md-7 site-section-heading text-center pt-4"><h2>Sản phẩm liên quan</h2></div>
    </div>
    <div class="row">
      <c:forEach items="${related}" var="p">
        <div class="col-sm-6 col-lg-3 mb-4"><t:product-card product="${p}"/></div>
      </c:forEach>
    </div>
  </div>
</div>
</c:if>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
<script>
  (function ($) {
    var variants = $('#variant-data li').map(function () {
      return { id: $(this).data('id'), size: String($(this).data('size')),
               color: String($(this).data('color')), available: parseInt($(this).data('available'), 10) };
    }).get();
    function find(color, size) {
      return variants.filter(function (v) { return v.color === color && v.size === size; })[0];
    }
    function update() {
      var color = $('input[name=pickColor]:checked').val();
      var size = $('input[name=pickSize]:checked').val();
      $('input[name=pickSize]').each(function () {
        var v = color ? find(color, $(this).val()) : null;
        $(this).prop('disabled', !!color && (!v || v.available <= 0));
      });
      var v = color && size ? find(color, size) : null;
      var ok = v && v.available > 0;
      $('#variantId').val(ok ? v.id : '');
      $('#add-to-cart, #buy-now').prop('disabled', !ok);
      if (ok) {
        $('#stock-info').text('Còn ' + v.available + ' sản phẩm có thể đặt');
        $('#qty').data('max', v.available).attr('max', v.available);
        if (parseInt($('#qty').val(), 10) > v.available) { $('#qty').val(v.available); }
      } else {
        $('#stock-info').text(v ? 'Biến thể này đã hết hàng' : (color && size ? 'Không có biến thể này' : 'Vui lòng chọn màu và size'));
      }
    }
    $('input[name=pickColor], input[name=pickSize]').on('change', update);
    if ($('input[name=pickColor]').length === 1) { $('input[name=pickColor]').prop('checked', true); }
    update();
  })(jQuery);
</script>
</body>
</html>
