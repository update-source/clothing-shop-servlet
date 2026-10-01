<c:set var="pageTitle" value="Liên hệ"/>
<c:set var="activeNav" value="contact"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="bg-light py-3">
  <div class="container">
    <div class="row">
      <div class="col-md-12 mb-0"><a href="${ctx}/">Trang chủ</a> <span class="mx-2 mb-0">/</span> <strong class="text-black">Liên hệ</strong></div>
    </div>
  </div>
</div>

<div class="site-section">
  <div class="container">
    <div class="row">
      <div class="col-md-12"><h2 class="h3 mb-3 text-black">Liên hệ với chúng tôi</h2></div>
      <div class="col-md-7">
        <div class="p-3 p-lg-5 border">
          <h3 class="h5 text-black mb-3">Câu hỏi thường gặp</h3>
          <p class="mb-1 text-black font-weight-bold">Tôi có thể huỷ đơn không?</p>
          <p>Được, khi đơn chưa được giao. Vào <a href="${ctx}/orders">Đơn hàng của tôi</a>, mở đơn và bấm "Huỷ đơn".
            Nếu đã thanh toán qua VNPAY, cửa hàng hoàn tiền cho bạn.</p>
          <p class="mb-1 text-black font-weight-bold">Thanh toán VNPAY bị lỗi thì sao?</p>
          <p>Bạn có thể thanh toán lại trong trang chi tiết đơn. Đơn VNPAY chưa thanh toán sẽ tự huỷ sau một thời gian
            để trả lại hàng đang giữ.</p>
          <p class="mb-1 text-black font-weight-bold">Hạng thành viên được tính thế nào?</p>
          <p class="mb-0">Theo tổng tiền các đơn đã hoàn tất: từ 2.000.000 ₫ là Thân thiết, từ 10.000.000 ₫ là VIP.
            Một số mã giảm giá chỉ dành cho hạng cao.</p>
        </div>
      </div>
      <div class="col-md-5 ml-auto">
        <div class="p-4 border mb-3">
          <span class="d-block text-primary h6 text-uppercase">Cửa hàng TP. Hồ Chí Minh</span>
          <p class="mb-0">1 Võ Văn Ngân, Phường Thủ Đức, TP. Hồ Chí Minh</p>
        </div>
        <div class="p-4 border mb-3">
          <span class="d-block text-primary h6 text-uppercase">Cửa hàng Hà Nội</span>
          <p class="mb-0">25 Lý Thường Kiệt, Phường Cửa Nam, Hà Nội</p>
        </div>
        <div class="p-4 border mb-3">
          <span class="d-block text-primary h6 text-uppercase">Hotline &amp; email</span>
          <p class="mb-0">1900 1234 (8h – 22h) · <a href="mailto:hotro@shoppers.vn">hotro@shoppers.vn</a></p>
        </div>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
