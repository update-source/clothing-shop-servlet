/* Khu quản trị: ẩn ô "Giảm tối đa" khi chọn giảm số tiền cố định */
(function ($) {
  function syncPolicy() {
    var percent = $('.js-policy-type').val() === 'PERCENT';
    $('.js-max-discount').toggle(percent);
    $('#policyValue').attr('placeholder', percent ? 'Ví dụ: 10 (%)' : 'Ví dụ: 30000 (₫)');
  }
  $(document).on('change', '.js-policy-type', syncPolicy);
  $(syncPolicy);
})(jQuery);
