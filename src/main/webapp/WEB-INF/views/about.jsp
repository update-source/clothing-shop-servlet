<c:set var="pageTitle" value="Giới thiệu"/>
<c:set var="activeNav" value="about"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="bg-light py-3">
  <div class="container">
    <div class="row">
      <div class="col-md-12 mb-0"><a href="${ctx}/">Trang chủ</a> <span class="mx-2 mb-0">/</span> <strong class="text-black">Giới thiệu</strong></div>
    </div>
  </div>
</div>

<div class="site-section border-bottom" data-aos="fade">
  <div class="container">
    <div class="row mb-5">
      <div class="col-md-6">
        <div class="block-16">
          <figure><img src="${ctx}/images/blog_1.jpg" alt="Cửa hàng Shoppers" class="img-fluid rounded"></figure>
        </div>
      </div>
      <div class="col-md-1"></div>
      <div class="col-md-5">
        <div class="site-section-heading pt-3 mb-4"><h2 class="text-black">Câu chuyện của chúng tôi</h2></div>
        <p>Shoppers bắt đầu từ một cửa hàng quần áo nhỏ với mong muốn mang đến những món đồ cơ bản, bền đẹp
          và dễ phối cho mọi người. Mỗi mẫu áo, quần đều có đủ size và màu để bạn chọn đúng thứ mình cần.</p>
        <p>Khi mua online, bạn chọn đúng size và màu, đặt hàng trong vài bước và theo dõi đơn đến tận lúc nhận.
          Hàng được giữ cho bạn ngay khi đặt, nên không lo hai người cùng mua chiếc áo cuối cùng.</p>
      </div>
    </div>
  </div>
</div>

<div class="site-section border-bottom" data-aos="fade">
  <div class="container">
    <div class="row justify-content-center mb-5">
      <div class="col-md-7 site-section-heading text-center pt-4"><h2>Đội ngũ</h2></div>
    </div>
    <div class="row">
      <c:forEach var="i" begin="1" end="4">
        <c:set var="names" value="${['Nguyễn Minh Anh', 'Trần Thu Hà', 'Lê Quốc Bảo', 'Phạm Gia Huy']}"/>
        <c:set var="roles" value="${['Sáng lập', 'Thiết kế', 'Vận hành kho', 'Chăm sóc khách hàng']}"/>
        <div class="col-md-6 col-lg-3">
          <div class="block-38 text-center">
            <div class="block-38-img">
              <div class="block-38-header">
                <img src="${ctx}/images/person_${i}.jpg" alt="${names[i - 1]}" class="mb-4">
                <h3 class="block-38-heading h4">${names[i - 1]}</h3>
                <p class="block-38-subheading">${roles[i - 1]}</p>
              </div>
            </div>
          </div>
        </div>
      </c:forEach>
    </div>
  </div>
</div>

<div class="site-section site-section-sm site-blocks-1 border-0" data-aos="fade">
  <div class="container">
    <div class="row">
      <div class="col-md-6 col-lg-4 d-lg-flex mb-4 mb-lg-0 pl-4">
        <div class="icon mr-4 align-self-start"><span class="icon-truck"></span></div>
        <div class="text"><h2 class="text-uppercase">Giao toàn quốc</h2><p>COD hoặc thanh toán trước qua VNPAY.</p></div>
      </div>
      <div class="col-md-6 col-lg-4 d-lg-flex mb-4 mb-lg-0 pl-4">
        <div class="icon mr-4 align-self-start"><span class="icon-refresh2"></span></div>
        <div class="text"><h2 class="text-uppercase">Đổi trả</h2><p>Hoàn tiền đầy đủ khi trả hàng.</p></div>
      </div>
      <div class="col-md-6 col-lg-4 d-lg-flex mb-4 mb-lg-0 pl-4">
        <div class="icon mr-4 align-self-start"><span class="icon-help"></span></div>
        <div class="text"><h2 class="text-uppercase">Thành viên</h2><p>Mua càng nhiều, hạng càng cao, ưu đãi càng lớn.</p></div>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
