<c:set var="pageTitle" value="Trang chủ"/>
<c:set var="activeNav" value="home"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="site-blocks-cover" style="background-image: url(${ctx}/images/hero_1.jpg);" data-aos="fade">
  <div class="container">
    <div class="row align-items-start align-items-md-center justify-content-end">
      <div class="col-md-5 text-center text-md-left pt-5 pt-md-0">
        <h1 class="mb-2">Mặc đẹp mỗi ngày, giá vừa túi tiền</h1>
        <div class="intro-text text-center text-md-left">
          <p class="mb-4">Áo, quần, giày đủ size và màu. Đặt hàng trong vài bước, thanh toán khi nhận hàng hoặc qua VNPAY.</p>
          <p><a href="${ctx}/shop" class="btn btn-sm btn-primary">Mua ngay</a></p>
        </div>
      </div>
    </div>
  </div>
</div>

<div class="site-section site-section-sm site-blocks-1">
  <div class="container">
    <div class="row">
      <div class="col-md-6 col-lg-4 d-lg-flex mb-4 mb-lg-0 pl-4" data-aos="fade-up">
        <div class="icon mr-4 align-self-start"><span class="icon-truck"></span></div>
        <div class="text">
          <h2 class="text-uppercase">Giao hàng toàn quốc</h2>
          <p>Giao đến 34 tỉnh, thành theo địa chỉ hai cấp mới. Phí vận chuyển theo báo giá của đơn vị giao hàng.</p>
        </div>
      </div>
      <div class="col-md-6 col-lg-4 d-lg-flex mb-4 mb-lg-0 pl-4" data-aos="fade-up" data-aos-delay="100">
        <div class="icon mr-4 align-self-start"><span class="icon-refresh2"></span></div>
        <div class="text">
          <h2 class="text-uppercase">Đổi trả dễ dàng</h2>
          <p>Huỷ đơn bất cứ lúc nào trước khi giao. Hàng đã nhận vẫn có thể trả lại, cửa hàng hoàn tiền đầy đủ.</p>
        </div>
      </div>
      <div class="col-md-6 col-lg-4 d-lg-flex mb-4 mb-lg-0 pl-4" data-aos="fade-up" data-aos-delay="200">
        <div class="icon mr-4 align-self-start"><span class="icon-help"></span></div>
        <div class="text">
          <h2 class="text-uppercase">Hỗ trợ tận tình</h2>
          <p>Gọi 1900 1234 từ 8h đến 22h mỗi ngày để được tư vấn chọn size và theo dõi đơn hàng.</p>
        </div>
      </div>
    </div>
  </div>
</div>

<c:if test="${not empty nav.categories}">
<div class="site-section site-blocks-2">
  <div class="container">
    <div class="row justify-content-center mb-5">
      <div class="col-md-7 site-section-heading text-center pt-4"><h2>Danh mục</h2></div>
    </div>
    <div class="row">
      <c:set var="collectionImages" value="${['women.jpg', 'men.jpg', 'children.jpg']}"/>
      <c:forEach items="${nav.categories}" var="node" varStatus="st" end="2">
        <div class="col-sm-6 col-md-6 col-lg-4 mb-4 mb-lg-0" data-aos="fade" data-aos-delay="${st.index * 100}">
          <a class="block-2-item" href="${ctx}/shop?category=${node.category.id}">
            <figure class="image">
              <img src="${ctx}/images/${collectionImages[st.index]}" alt="" class="img-fluid">
            </figure>
            <div class="text">
              <span class="text-uppercase">${node.productCount} sản phẩm</span>
              <h3><c:out value="${node.category.name}"/></h3>
            </div>
          </a>
        </div>
      </c:forEach>
    </div>
  </div>
</div>
</c:if>

<div class="site-section block-3 site-blocks-2 bg-light">
  <div class="container">
    <div class="row justify-content-center">
      <div class="col-md-7 site-section-heading text-center pt-4"><h2>Sản phẩm mới</h2></div>
    </div>
    <div class="row">
      <div class="col-md-12">
        <div class="nonloop-block-3 owl-carousel">
          <c:forEach items="${featured}" var="p">
            <div class="item"><t:product-card product="${p}"/></div>
          </c:forEach>
        </div>
      </div>
    </div>
  </div>
</div>

<c:if test="${not empty promotions}">
<c:set var="promo" value="${promotions[0]}"/>
<div class="site-section block-8">
  <div class="container">
    <div class="row justify-content-center mb-5">
      <div class="col-md-7 site-section-heading text-center pt-4"><h2>Khuyến mãi lớn!</h2></div>
    </div>
    <div class="row align-items-center">
      <div class="col-md-12 col-lg-7 mb-5">
        <a href="${ctx}/promotions"><img src="${ctx}/images/blog_1.jpg" alt="Khuyến mãi" class="img-fluid rounded"></a>
      </div>
      <div class="col-md-12 col-lg-5 text-center pl-md-5">
        <h2><a href="${ctx}/promotions"><c:out value="${promo.name}"/></a></h2>
        <p class="post-meta mb-4">
          <c:out value="${promo.policy.describe()}"/> <span class="block-8-sep">&bullet;</span>
          đến ${f:dateTime(promo.period.end)}
        </p>
        <p>Giá đã được giảm sẵn trên từng sản phẩm trong chương trình — bạn không cần nhập mã.
          Ngoài ra còn ${fn:length(promotions) - 1} chương trình khác đang diễn ra.</p>
        <p><a href="${ctx}/promotions" class="btn btn-primary btn-sm">Xem sản phẩm khuyến mãi</a></p>
      </div>
    </div>
  </div>
</div>
</c:if>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
