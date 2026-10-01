<%@ tag pageEncoding="UTF-8" body-content="empty" description="Thẻ sản phẩm dùng ở trang chủ, cửa hàng, yêu thích" %>
<%@ attribute name="product" required="true" type="com.shop.model.catalog.Product" %>
<%@ attribute name="bordered" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="f" uri="http://shop.local/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="now" value="${requestScope.now}"/>
<c:set var="onSale" value="${product.isOnSale(now)}"/>
<div class="block-4 text-center product-card ${bordered ? 'border' : ''}">
  <c:if test="${onSale}"><span class="badge badge-danger badge-sale">SALE</span></c:if>
  <c:if test="${product.totalAvailable == 0}"><span class="badge badge-secondary badge-soldout">Hết hàng</span></c:if>
  <figure class="block-4-image">
    <a href="${ctx}/product?id=${product.id}">
      <img src="${ctx}/${empty product.imageUrl ? 'images/cloth_1.jpg' : product.imageUrl}"
           alt="${fn:escapeXml(product.name)}" class="img-fluid">
    </a>
  </figure>
  <div class="block-4-text p-4">
    <h3><a href="${ctx}/product?id=${product.id}"><c:out value="${product.name}"/></a></h3>
    <p class="mb-0 text-muted small"><c:out value="${product.category.name}"/></p>
    <p class="text-primary font-weight-bold mb-0">
      <c:if test="${onSale}"><span class="price-old">${f:vnd(product.basePrice)}</span></c:if>
      ${f:vnd(product.getFinalPrice(now))}
    </p>
  </div>
</div>
