<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>VNPAY (giả lập) — Cổng thanh toán</title>
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;600;700&amp;display=swap">
  <link rel="stylesheet" href="${ctx}/css/bootstrap.min.css">
  <link rel="stylesheet" href="${ctx}/css/shop.css?v=2">
  <style>
    body { background: #f0f4f8; }
    .gateway { max-width: 520px; margin: 60px auto; }
    .gateway .brand { color: #005baa; font-weight: 700; font-size: 28px; }
    .gateway .brand span { color: #ed1c24; }
  </style>
</head>
<body>
<div class="gateway">
  <div class="card shadow-sm">
    <div class="card-body p-4">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div class="brand">VN<span>PAY</span></div>
        <span class="badge badge-warning">MÔI TRƯỜNG GIẢ LẬP</span>
      </div>
      <p class="text-muted small">Đây là cổng VNPAY giả lập chạy trong ứng dụng (vnpay.mode=mock) để demo luồng thanh toán.
        Yêu cầu và kết quả được ký HMAC-SHA512 giống VNPAY thật.</p>
      <c:choose>
        <c:when test="${not valid}">
          <div class="alert alert-danger">Chữ ký yêu cầu thanh toán không hợp lệ. Giao dịch bị từ chối.</div>
        </c:when>
        <c:otherwise>
          <table class="table table-sm">
            <tr><td>Nhà cung cấp</td><td class="text-right"><c:out value="${vnp.vnp_TmnCode}"/></td></tr>
            <tr><td>Mã giao dịch</td><td class="text-right"><c:out value="${vnp.vnp_TxnRef}"/></td></tr>
            <tr><td>Nội dung</td><td class="text-right"><c:out value="${vnp.vnp_OrderInfo}"/></td></tr>
            <tr class="font-weight-bold"><td>Số tiền</td><td class="text-right">${f:vnd(amount)}</td></tr>
          </table>
          <div class="form-group">
            <label class="small text-muted">Ngân hàng</label>
            <input class="form-control" value="NCB — Thẻ ATM nội địa (thử nghiệm)" disabled>
          </div>
          <a class="btn btn-primary btn-block" href="${ctx}/payment/mock-vnpay/complete?${fn:escapeXml(query)}&amp;result=success">
            Xác nhận thanh toán thành công</a>
          <a class="btn btn-outline-secondary btn-block" href="${ctx}/payment/mock-vnpay/complete?${fn:escapeXml(query)}&amp;result=cancel">
            Huỷ giao dịch</a>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</div>
</body>
</html>
