# Đặc tả bài toán — Shop quần áo E-commerce

Oct 1, 2026 · @tsst

## 1. Bài toán

Hệ thống là một cửa hàng quần áo bán trực tuyến. Khách hàng xem sản phẩm, chọn size và màu, cho vào giỏ, đặt hàng, thanh toán và viết đánh giá. Nhân viên cửa hàng xác nhận đơn và giao hàng.

Tài liệu này mô tả mô hình đối tượng của hệ thống bằng lời: mỗi đối tượng là gì, nó biết gì, nó làm được gì, và nó liên hệ với đối tượng khác ra sao. Mọi quan hệ đều được giải thích bằng tình huống thật ngoài cửa hàng, không phải bằng cách lưu trữ dữ liệu.

Các bên tham gia:

- **Khách hàng**: người mua, có tài khoản, sổ địa chỉ, giỏ hàng và lịch sử mua.
- **Nhân viên (STAFF)**: xác nhận đơn, đóng gói và giao hàng.
- **Quản trị viên (ADMIN)**: là nhân viên có thêm quyền quản lý sản phẩm, khuyến mãi và khoá tài khoản.
- **Cổng thanh toán VNPAY**: bên ngoài hệ thống, báo kết quả giao dịch về cho cửa hàng.

Phạm vi gồm: tài khoản, sổ địa chỉ, danh mục và sản phẩm, giỏ hàng, danh sách yêu thích, đơn hàng (kể cả giao thất bại và trả hàng), thanh toán (COD và VNPAY), voucher, chương trình khuyến mãi và đánh giá. Tích hợp đơn vị vận chuyển và quản lý nhiều kho nằm ngoài phạm vi; phí vận chuyển lấy theo báo giá của đơn vị vận chuyển.

Sơ đồ lớp chia thành bốn package: **Tài khoản** (mục 2–3), **Sản phẩm & mua sắm** (mục 4, 5, 9), **Đơn hàng & thanh toán** (mục 6–7) và **Giảm giá** (mục 8).

## 2. Người dùng: User, Customer, Employee

Mọi người dùng hệ thống đều là một **User**, nhưng không ai chỉ là "User" chung chung: họ hoặc là khách hàng, hoặc là nhân viên. Vì vậy User là lớp trừu tượng, còn Customer và Employee kế thừa từ nó.

**User** mô tả những gì mọi tài khoản đều có: tên đăng nhập, mật khẩu, họ tên, giới tính, ngày sinh, email, số điện thoại và trạng thái hoạt động. Mỗi tài khoản tự làm được các việc sau:

- Kiểm tra mật khẩu nhập vào có đúng không (`verifyPassword`).
- Đổi mật khẩu, phải đưa mật khẩu cũ (`changePassword`).
- Cập nhật họ tên và số điện thoại (`updateProfile`).
- Bị khoá hoặc mở khoá (`lock`, `unlock`). Khoá là trạng thái của chính tài khoản, nên tài khoản tự đổi trạng thái. Chỉ quản trị viên được quyền ra lệnh đó; đây là quy tắc phân quyền, không phải hành vi của lớp.

**Customer** là người mua. Khách hàng không có thuộc tính riêng nào phải lưu; những gì đặc trưng cho họ đều đến từ các mối quan hệ: sổ địa chỉ, giỏ hàng, danh sách yêu thích và lịch sử đơn hàng. Hành vi của khách hàng phản ánh đúng những gì một người mua làm ngoài đời:

- Thêm hoặc bỏ sản phẩm khỏi danh sách yêu thích.
- Đặt hàng từ giỏ (`checkout`), chọn cùng lúc địa chỉ giao, phương thức thanh toán và voucher (nếu có), giống màn hình thanh toán thật.
- Viết đánh giá cho sản phẩm đã mua (`writeReview`), sau khi tự kiểm tra lịch sử mua (`hasPurchased`).
- Đếm số lần mình đã dùng một voucher (`countVoucherUsage`), để voucher biết khách còn lượt hay không.
- Biết tổng số tiền đã chi (`getTotalSpent`) và hạng thành viên của mình (`getLevel`).

Việc quản lý địa chỉ không nằm trực tiếp trong Customer mà được giao cho sổ địa chỉ (mục 3), để Customer không phải ôm quá nhiều nhóm việc.

Hạng thành viên (CASUAL, REGULAR, LOYAL) **không phải thứ khách tự đặt**: nó là kết quả của việc mua sắm. Mỗi hạng mang một ngưỡng chi tiêu tối thiểu (`minSpent`), và enum CustomerLevel tự chọn hạng ứng với một tổng chi tiêu (`fromSpent`). Hạng của khách vì vậy là `CustomerLevel.fromSpent(getTotalSpent())`. Mức ngưỡng cụ thể do cửa hàng quyết định.

**Employee** là nhân viên cửa hàng, có ngày vào làm và vai trò (STAFF hoặc ADMIN). Nhân viên tự biết mình có phải quản trị viên không (`isAdmin`). Quyền ADMIN gồm quản lý sản phẩm, khuyến mãi và khoá tài khoản; đây là quy tắc phân quyền ở tầng chức năng, sơ đồ chỉ ghi chú lại. Việc nhân viên xử lý đơn được thể hiện trên đơn hàng: đơn được xác nhận và giao *bởi* một nhân viên (mục 6).

Một người vừa làm nhân viên vừa muốn mua hàng sẽ cần hai tài khoản. Đây là đánh đổi có chủ ý của thiết kế kế thừa, chấp nhận được với quy mô cửa hàng này.

## 3. Sổ địa chỉ: AddressBook, Address

Mỗi khách hàng có đúng một **sổ địa chỉ** (AddressBook), giống cuốn sổ ghi những nơi hay nhận hàng: nhà riêng, công ty, nhà người thân. Sổ địa chỉ chịu trách nhiệm thêm, xoá địa chỉ và giữ địa chỉ mặc định.

**Address** là một **value object bất biến**: hai địa chỉ giống hệt nhau về nội dung được coi là một, và muốn sửa thì thay bằng địa chỉ mới chứ không chỉnh đối tượng cũ. Mỗi địa chỉ gồm tên người nhận, số điện thoại, số nhà và tên đường, phường/xã, tỉnh/thành, theo mô hình hai cấp tỉnh – xã áp dụng từ tháng 7/2025. Địa chỉ biết tự ghép thành một dòng đầy đủ để in lên phiếu giao (`getFullAddress`).

**AddressBook** không có thuộc tính riêng; nó có các hành động `add`, `remove`, `setDefault` và `getDefault`. Quy tắc "tối đa một địa chỉ mặc định" nằm gọn trong sổ, vì chỉ sổ mới đổi được địa chỉ mặc định.

Bốn mối quan hệ quanh địa chỉ:

- **Khách hàng ◆ sổ địa chỉ** (1 – 1): sổ là của riêng khách, mất theo tài khoản.
- **Sổ địa chỉ ◆ địa chỉ** (1 – 0..\*): sổ chứa các địa chỉ đã lưu.
- **Sổ địa chỉ → địa chỉ «mặc định»** (0..1): một liên kết riêng, không phải cờ đánh dấu trên từng địa chỉ.
- **Đơn hàng ◆ địa chỉ «giao đến»** (1 – 1): lúc đặt hàng, đơn giữ một bản địa chỉ của riêng nó. Sau này khách sửa hay xoá địa chỉ trong sổ thì đơn cũ vẫn giữ nguyên nơi đã giao, giống phiếu giao hàng đã in.

## 4. Sản phẩm: Product, ProductVariant, Category

**Biến thể** (ProductVariant) là một phiên bản cụ thể của một mẫu sản phẩm, xác định bởi đúng một cặp **size + màu**. Ví dụ, mẫu "Áo thun basic" bán 2 màu và 3 size thì có 2 × 3 = 6 biến thể, mỗi biến thể đếm tồn kho riêng (số liệu minh hoạ):

| Mẫu sản phẩm (Product) | Biến thể (ProductVariant) | Tồn kho |
| --- | --- | --- |
| Áo thun basic | Đen – S | 12 |
| Áo thun basic | Đen – M | 0 |
| Áo thun basic | Đen – L | 7 |
| Áo thun basic | Trắng – S | 5 |
| Áo thun basic | Trắng – M | 9 |
| Áo thun basic | Trắng – L | 3 |

Khách không mua "áo thun basic" chung chung mà mua một biến thể, ví dụ "đen, size M". Nếu Đen – M hết hàng, khách vẫn mua được Đen – L, nên tồn kho phải nằm ở biến thể chứ không nằm ở mẫu. Tên, mô tả và giá gốc thì giống nhau cho mọi size và màu, nên nằm ở mẫu sản phẩm. Ngoài đời, biến thể chính là món hàng thật nằm trên kệ; ngành bán lẻ thường gọi nó là một mã hàng (SKU).

**Product** có tên, mô tả, giá gốc và trạng thái đang bán. Sản phẩm tự làm được:

- Tạo biến thể mới khi cửa hàng nhập thêm size hoặc màu (`addVariant`).
- Đổi giá gốc (`changePrice`).
- Ngừng bán (`discontinue`). Sản phẩm thật không bị "xoá"; nó chỉ thôi được bán, còn các đơn cũ vẫn nhắc đến nó.
- Nhận đánh giá mới (`addReview`), từ chối nếu khách đó đã đánh giá rồi (`hasReviewBy`), và tính điểm trung bình (`getAverageRating`).
- Tính tổng tồn kho từ các biến thể (`getTotalStock`).
- Tính giá bán cuối tại một thời điểm (`getFinalPrice`): lấy mức giảm cao nhất trong các chương trình khuyến mãi đang chạy.

**ProductVariant** có size, màu, số lượng tồn kho và **số lượng đang giữ** cho các đơn chưa xác nhận. Số hàng còn bán được bằng tồn kho trừ số đang giữ (`getAvailableQuantity`). Biến thể tự quản lý kho của mình qua bốn hành động:

- `reserve(qty)`: giữ hàng khi khách đặt đơn. Không đủ hàng còn bán thì bị từ chối ngay.
- `releaseReservation(qty)`: nhả hàng đang giữ khi đơn bị huỷ trước khi xác nhận.
- `commit(qty)`: khi nhân viên xác nhận đơn, hàng đang giữ được trừ hẳn khỏi kho.
- `restock(qty)`: nhập hàng lại vào kho khi đơn đã xác nhận bị huỷ, giao thất bại hoặc bị trả.

Nhờ giữ hàng ngay lúc đặt, hai khách không thể cùng mua chiếc áo cuối cùng: người đặt sau thấy biến thể đã hết hàng còn bán. Ràng buộc luôn đúng: 0 ≤ số đang giữ ≤ tồn kho.

Quan hệ **sản phẩm – biến thể là composition** (1 sản phẩm – 1..\* biến thể). Một chiếc "size M màu đen" không tồn tại nếu không gắn với một mẫu áo cụ thể. Cũng vì vậy, chỉ sản phẩm mới tạo ra biến thể của nó.

**Category** là danh mục hàng, chỉ có tên. Danh mục có thể nằm trong một danh mục cha (0..1) và chứa nhiều danh mục con, ví dụ "Áo" chứa "Áo thun" và "Áo sơ mi". Mỗi sản phẩm thuộc đúng một danh mục; sản phẩm biết danh mục của mình, còn danh mục không cần giữ danh sách sản phẩm.

## 5. Giỏ hàng và danh sách yêu thích

Mỗi khách hàng có đúng **một giỏ hàng** (composition 1–1), giống như mỗi người vào siêu thị đẩy một xe đẩy của riêng mình. Giỏ hàng gắn với khách, không gắn với cách hệ thống lưu trữ.

**Cart** không có thuộc tính riêng; nội dung của nó chính là các dòng hàng bên trong. Giỏ hàng là bên duy nhất được thêm, bớt và sửa số lượng các dòng của mình:

- Thêm một biến thể với số lượng (`addItem`). Nếu biến thể đó đã có trong giỏ, giỏ tự cộng dồn số lượng thay vì tạo dòng mới.
- Bỏ một biến thể (`removeItem`) hoặc đổi số lượng (`setQuantity`).
- Tính tổng tiền tạm tính (`getTotal`) và làm trống giỏ (`clear`) sau khi khách đặt hàng xong.

**CartItem** là một dòng trong giỏ, chỉ có số lượng. Mỗi dòng trỏ đến đúng một biến thể sản phẩm (CartItem → ProductVariant). Giỏ hàng chứa các dòng theo quan hệ composition: bỏ giỏ thì các dòng mất theo. Giá của dòng (`getSubtotal`) luôn lấy theo giá hiện tại của sản phẩm, vì khách chưa trả tiền thì giá chưa được chốt.

**Danh sách yêu thích** không cần một lớp riêng, vì nó không có dữ liệu hay hành vi nào ngoài việc "khách thích những sản phẩm này". Nó được thể hiện trực tiếp bằng quan hệ **Customer → Product «yêu thích»** (nhiều – nhiều): một khách thích nhiều sản phẩm, một sản phẩm được nhiều khách thích. Khách thêm và bỏ sản phẩm qua `addToWishlist` và `removeFromWishlist`. Khách yêu thích mẫu sản phẩm, chưa cần chọn size hay màu, nên quan hệ trỏ tới Product chứ không trỏ tới biến thể.

## 6. Đơn hàng: Order, OrderDetail

Đơn hàng ra đời khi khách đặt hàng từ giỏ (`Customer.checkout`), và từ đó tự quản lý toàn bộ vòng đời của mình. Không ai bên ngoài được sửa thẳng trạng thái đơn; mọi thay đổi đi qua các hành động có tên rõ ràng.

**Cách đơn được tạo.** Giống màn hình thanh toán thật, khách chọn cùng lúc địa chỉ giao, phương thức thanh toán và voucher (nếu có), rồi bấm đặt hàng. Khi đó:

1. Đơn chuyển từng dòng giỏ thành một dòng đơn, chốt đơn giá, và giữ hàng cho từng biến thể (`reserve`).
2. Đơn lưu một bản địa chỉ giao và phương thức thanh toán đã chọn.
3. Nếu có voucher, đơn hỏi voucher có áp được không rồi ghi nhận một lượt dùng (`redeem`).
4. Giỏ hàng được làm trống. Đơn bắt đầu ở trạng thái PENDING.

Khách hàng tạo đơn vì khách sở hữu giỏ hàng; đơn tạo các dòng đơn vì đơn sở hữu chúng.

**Order** có ngày đặt, ghi chú, phương thức thanh toán, phí vận chuyển và trạng thái. Phí vận chuyển lấy theo báo giá của đơn vị vận chuyển. Các con số tiền khác được tính khi cần: tạm tính là tổng các dòng (`getSubtotal`), giảm giá do voucher tính (`getDiscount`), và tổng phải trả bằng tạm tính trừ giảm giá cộng phí vận chuyển (`getTotal`).

**OrderDetail** là một dòng đơn, gồm đơn giá đã chốt và số lượng, trỏ đến đúng biến thể khách đã mua. Đơn giá được giữ riêng vì giá sản phẩm có thể đổi sau này, còn số tiền khách đã đồng ý trả thì không đổi.

**Vòng đời đơn hàng.** Trạng thái chỉ đổi qua các hành động sau, mỗi hành động tự kiểm tra trạng thái hiện tại có cho phép không:

&#91;embedded content: vòng đời đơn hàng · 7 trạng thái, 7 chuyển đổi\]

Đơn chỉ tiến theo một chiều. Huỷ được khi chưa giao; đang giao thì hoặc hoàn tất, hoặc giao thất bại; đơn đã hoàn tất có thể bị trả.

Tác động của từng hành động lên kho, voucher và tiền:

| Hành động | Chuyển trạng thái | Kho | Voucher | Tiền |
| --- | --- | --- | --- | --- |
| `confirm(by: Employee)` | PENDING → CONFIRMED | Trừ hẳn hàng đang giữ (`commit`) | — | VNPAY phải thanh toán xong mới xác nhận được |
| `ship(by: Employee)` | CONFIRMED → SHIPPING | — | — | — |
| `complete()` | SHIPPING → COMPLETED | — | — | COD: thanh toán chuyển PAID |
| `failDelivery()` | SHIPPING → DELIVERY\_FAILED | Nhập lại (`restock`) | Trả lượt (`release`) | Hoàn tiền nếu đã trả |
| `returnGoods()` | COMPLETED → RETURNED | Nhập lại (`restock`) | — | Hoàn tiền |
| `cancel()` khi PENDING | PENDING → CANCELLED | Nhả hàng giữ (`releaseReservation`) | Trả lượt (`release`) | Hoàn tiền nếu đã trả |
| `cancel()` khi CONFIRMED | CONFIRMED → CANCELLED | Nhập lại (`restock`) | Trả lượt (`release`) | Hoàn tiền nếu đã trả |

Đơn VNPAY quá hạn thanh toán sẽ tự huỷ (`cancel`), nên hàng đang giữ và lượt voucher không bị chiếm mãi khi khách bỏ dở.

**Quan hệ của đơn hàng:**

- **Khách hàng – đơn hàng** (1 – 0..\*, hai chiều): khách đặt đơn và giữ lịch sử mua để tính hạng, kiểm tra đã mua và đếm lượt dùng voucher; đơn biết khách của mình để xét voucher. Không phải composition, vì đơn là chứng từ mua bán, vẫn tồn tại khi khách ngừng dùng tài khoản.
- **Đơn ◆ dòng đơn** (1 – 1..\*): đơn không thể rỗng, dòng đơn không tồn tại ngoài đơn.
- **Đơn ◆ địa chỉ «giao đến»** (1 – 1): bản địa chỉ riêng của đơn (mục 3).
- **Đơn → nhân viên «xử lý»** (0..1): chưa xác nhận thì chưa có ai, xác nhận xong thì có đúng một người chịu trách nhiệm.
- **Đơn → thanh toán** (0..\*) và **đơn → voucher** (0..1) được mô tả ở mục 7 và 8.

## 7. Thanh toán: Payment

Mỗi lần khách trả tiền cho một đơn là một **lần thanh toán** riêng. Một đơn có thể có nhiều lần (0..\*), vì giao dịch VNPAY có thể thất bại và khách thử lại.

Phương thức thanh toán (COD hoặc VNPAY) được chọn lúc đặt hàng và lưu trên đơn, nên đơn biết mình có cần được trả trước hay không. Thanh toán do đơn tạo ra (`Order.pay()`) theo đúng phương thức đó. Đơn biết các lần thanh toán của mình nên tự trả lời "đã trả tiền chưa" (`isPaid`). Quan hệ này là association chứ không phải composition: kể cả khi đơn bị huỷ, các lần thanh toán vẫn được giữ lại để đối soát với ngân hàng.

**Payment** gồm số tiền, trạng thái (UNPAID, PAID, FAILED, REFUNDED), thời điểm trả và mã giao dịch do cổng thanh toán cấp. Thanh toán tự đổi trạng thái qua các hành động:

- `markPaid(transactionNo)`: cổng VNPAY báo thành công, hoặc shipper đã thu tiền COD.
- `markFailed()`: giao dịch bị từ chối.
- `refund()`: hoàn tiền, chỉ được khi đang ở trạng thái PAID.

Hai luồng theo phương thức:

- **COD**: lần thanh toán được tạo lúc đặt hàng ở trạng thái UNPAID và chỉ thành PAID khi khách nhận hàng (`complete`). Khách từ chối nhận thì đơn giao thất bại, không có tiền nào phải hoàn.
- **VNPAY**: khách trả trước; đơn chỉ được xác nhận khi đã thanh toán xong. Quá hạn mà chưa trả thì đơn tự huỷ.

## 8. Giảm giá: Voucher, Promotion, DiscountPolicy, Period

Cửa hàng có hai cách giảm giá, khác nhau ở đối tượng được giảm. **Voucher** là mã khách tự nhập để giảm trên cả đơn hàng. **Promotion** là chương trình cửa hàng tự áp lên giá từng sản phẩm, khách không cần làm gì.

Hai loại giảm giá có chung hai phần, nên mỗi phần được tách thành một đối tượng riêng mà cả hai cùng dùng:

- **Cách tính mức giảm**, thể hiện bằng interface **DiscountPolicy** với một hành động duy nhất: cho một số tiền, trả về số tiền được giảm. Có hai cách cài đặt: **PercentDiscount** (giảm theo phần trăm, có mức giảm tối đa, ví dụ 10% tối đa 50.000 đ) và **FixedAmountDiscount** (giảm một số tiền cố định, ví dụ 30.000 đ).
- **Thời gian hiệu lực**, thể hiện bằng value object **Period** gồm thời điểm bắt đầu và kết thúc, biết tự kiểm tra một thời điểm có nằm trong khoảng đó không (`contains`).

Voucher và Promotion mỗi bên chứa đúng một cách tính và một thời gian hiệu lực (composition 1–1). Muốn thêm kiểu giảm mới, như "đồng giá 99.000 đ", chỉ cần thêm một lớp cài đặt DiscountPolicy; Voucher và Promotion không phải sửa gì.

**Voucher** có mã, các hạng khách được dùng, giá trị đơn tối thiểu, tổng số lượt dùng, số lượt tối đa mỗi khách, số lượt đã dùng và trạng thái kích hoạt. Voucher tự làm được:

- Kiểm tra còn hiệu lực tại một thời điểm (`isValid`): đang kích hoạt, trong thời gian hiệu lực, còn lượt.
- Kiểm tra áp được cho một khách và một số tiền (`isApplicable`): đúng hạng khách, đủ giá trị tối thiểu, và khách chưa dùng quá số lượt cho phép (voucher hỏi `customer.countVoucherUsage`).
- Tính số tiền được giảm (`calculateDiscount`), bằng cách giao cho DiscountPolicy của nó.
- Ghi nhận một lượt dùng khi khách đặt hàng (`redeem`) và trả lại lượt đó (`release`) khi đơn bị huỷ hoặc giao thất bại.

Quan hệ **đơn hàng → voucher** là 0..\* – 0..1: một đơn dùng tối đa một voucher, một voucher được dùng cho nhiều đơn. Khách chọn voucher ngay lúc đặt hàng (`checkout`), giống màn hình thanh toán thật; đơn hỏi voucher có áp được không rồi mới ghi nhận lượt dùng.

**Promotion** có tên, tự kiểm tra đang diễn ra không (`isOngoing`), tính giá sau giảm cho một mức giá (`apply`), và được quản trị viên thêm hoặc bớt sản phẩm (`addProduct`, `removeProduct`). Quan hệ **sản phẩm – khuyến mãi** là nhiều – nhiều và **hai chiều**: chương trình biết mình áp cho những sản phẩm nào, để trả lời "Sale 11.11 gồm những gì"; sản phẩm biết mình nằm trong những chương trình nào, để tính giá. Khi nhiều chương trình chồng nhau, sản phẩm lấy mức giảm cao nhất.

## 9. Đánh giá: Review

Chỉ khách đã mua sản phẩm mới được đánh giá sản phẩm đó, và mỗi khách đánh giá một sản phẩm tối đa một lần; muốn đổi ý thì sửa đánh giá cũ. Đánh giá gồm số sao từ 1 đến 5 và lời nhận xét.

Cách một đánh giá ra đời phản ánh đúng ai làm gì:

1. Khách gọi `writeReview(product, rating, comment)`.
2. Khách tự kiểm tra lịch sử mua của mình (`hasPurchased`). Chưa mua thì không viết được.
3. Đánh giá được tạo; số sao ngoài khoảng 1–5 bị từ chối ngay lúc tạo, nên không bao giờ tồn tại một đánh giá sai.
4. Khách giao đánh giá cho sản phẩm (`product.addReview`). Sản phẩm từ chối nếu khách này đã có đánh giá (`hasReviewBy`).

Hai quan hệ của đánh giá:

- **Sản phẩm chứa các đánh giá** (composition, 1 – 0..\*). Đánh giá nói về một sản phẩm cụ thể và chỉ có nghĩa khi gắn với sản phẩm đó. Sản phẩm giữ danh sách đánh giá nên tự tính được điểm trung bình.
- **Đánh giá được viết bởi một khách** (0..\* – 1). Đánh giá biết tác giả để hiển thị tên người viết; khách không cần giữ danh sách đánh giá của mình.

Người viết có thể sửa lại số sao và nhận xét (`edit`), với cùng ràng buộc 1–5 sao.

## 10. Bảng tổng hợp quan hệ

Hệ thống có 28 quan hệ, gồm 2 quan hệ kế thừa, 2 quan hệ cài đặt interface, 12 composition và 12 association, xếp theo package. Bội số ghi theo thứ tự hai đầu của cột "Quan hệ".

| Quan hệ | Loại | Bội số | Chiều | Ý nghĩa đời thực |
| --- | --- | --- | --- | --- |
| Customer → User | Kế thừa | — | — | Khách hàng là một người dùng |
| Employee → User | Kế thừa | — | — | Nhân viên là một người dùng |
| Customer ◆ AddressBook | Composition | 1 – 1 | Customer → AddressBook | Mỗi khách có một sổ địa chỉ |
| AddressBook ◆ Address | Composition | 1 – 0..\* | AddressBook → Address | Sổ chứa các địa chỉ đã lưu |
| AddressBook → Address «mặc định» | Association | 1 – 0..1 | AddressBook → Address | Tối đa một địa chỉ mặc định |
| Customer ◆ Cart | Composition | 1 – 1 | Customer → Cart | Mỗi khách có một giỏ hàng |
| Cart ◆ CartItem | Composition | 1 – 0..\* | Cart → CartItem | Giỏ chứa các dòng hàng |
| CartItem → ProductVariant | Association | 0..\* – 1 | CartItem → ProductVariant | Mỗi dòng giỏ là một size, màu cụ thể |
| Customer → Product «yêu thích» | Association | 0..\* – 0..\* | Customer → Product | Khách thích nhiều mẫu, mẫu được nhiều khách thích |
| Product ◆ ProductVariant | Composition | 1 – 1..\* | Product → ProductVariant | Mẫu áo có các size, màu |
| Product ◆ Review | Composition | 1 – 0..\* | Product → Review | Sản phẩm chứa các đánh giá về nó |
| Review → Customer «viết bởi» | Association | 0..\* – 1 | Review → Customer | Đánh giá biết người viết |
| Product → Category | Association | 0..\* – 1 | Product → Category | Mỗi sản phẩm thuộc một danh mục |
| Category → Category «cha» | Association | 0..\* – 0..1 | Con → cha | Danh mục lồng nhau |
| Customer – Order «đặt» | Association | 1 – 0..\* | Hai chiều | Khách đặt đơn và giữ lịch sử mua |
| Order ◆ OrderDetail | Composition | 1 – 1..\* | Order → OrderDetail | Đơn gồm ít nhất một dòng hàng |
| Order ◆ Address «giao đến» | Composition | 1 – 1 | Order → Address | Đơn giữ bản địa chỉ giao của riêng nó |
| OrderDetail → ProductVariant | Association | 0..\* – 1 | OrderDetail → ProductVariant | Dòng đơn ghi biến thể đã mua |
| Order → Employee «xử lý» | Association | 0..\* – 0..1 | Order → Employee | Nhân viên xác nhận và giao đơn |
| Order → Payment | Association | 1 – 0..\* | Order → Payment | Một đơn có thể thanh toán nhiều lần |
| Order → Voucher «áp dụng» | Association | 0..\* – 0..1 | Order → Voucher | Một đơn dùng tối đa một mã |
| Product – Promotion | Association | 0..\* – 0..\* | Hai chiều | Chương trình biết sản phẩm của nó, sản phẩm biết các chương trình đang có |
| Voucher ◆ DiscountPolicy | Composition | 1 – 1 | Voucher → DiscountPolicy | Mã có một cách tính giảm |
| Voucher ◆ Period | Composition | 1 – 1 | Voucher → Period | Mã có một thời gian hiệu lực |
| Promotion ◆ DiscountPolicy | Composition | 1 – 1 | Promotion → DiscountPolicy | Chương trình có một cách tính giảm |
| Promotion ◆ Period | Composition | 1 – 1 | Promotion → Period | Chương trình có một thời gian diễn ra |
| PercentDiscount ⇢ DiscountPolicy | Cài đặt interface | — | — | Giảm theo phần trăm là một cách tính giảm |
| FixedAmountDiscount ⇢ DiscountPolicy | Cài đặt interface | — | — | Giảm số tiền cố định là một cách tính giảm |

Sáu enum (Gender, EmployeeRole, CustomerLevel, OrderStatus, PaymentMethod, PaymentStatus) chỉ là kiểu của thuộc tính, không phải quan hệ giữa các đối tượng, nên không có trong bảng.

## 11. Kịch bản tiêu biểu

Năm kịch bản dưới đây cho thấy các đối tượng phối hợp với nhau như thế nào, theo đúng thứ tự xảy ra ngoài đời.

**Kịch bản A: Mua áo, trả bằng VNPAY, dùng voucher**

1. Khách xem "Áo thun basic". Sản phẩm tính giá cuối theo chương trình khuyến mãi đang chạy.
2. Khách chọn Đen – M và thêm 2 cái vào giỏ. Giỏ kiểm tra biến thể còn đủ hàng bán rồi tạo dòng giỏ.
3. Ở màn hình thanh toán, khách chọn địa chỉ mặc định, VNPAY và mã voucher, rồi gọi `checkout`.
4. Đơn được tạo ở PENDING: giữ 2 cái Đen – M, chốt giá, lưu bản địa chỉ giao, voucher kiểm tra điều kiện và ghi nhận một lượt dùng. Giỏ được làm trống.
5. Đơn tạo một lần thanh toán VNPAY; cổng báo thành công nên thanh toán chuyển PAID.
6. Nhân viên xác nhận đơn. Đơn kiểm tra đã thanh toán, ghi nhận nhân viên, và 2 cái đang giữ được trừ hẳn khỏi kho.
7. Nhân viên giao hàng, khách nhận, đơn chuyển COMPLETED. Tổng chi tiêu của khách tăng, hạng thành viên có thể lên theo.

**Kịch bản B: Hai khách cùng mua chiếc áo cuối cùng**

1. Biến thể Trắng – L chỉ còn 1 cái bán được.
2. Khách thứ nhất đặt hàng trước. Biến thể giữ 1 cái cho đơn của khách này; số còn bán được về 0.
3. Khách thứ hai đặt sau vài giây. Biến thể từ chối giữ hàng vì không còn hàng bán, nên đơn thứ hai không được tạo.
4. Nếu khách thứ nhất huỷ trước khi xác nhận, biến thể nhả hàng giữ và chiếc áo lại có thể bán.

**Kịch bản C: Khách huỷ đơn COD đã xác nhận**

1. Đơn đang ở CONFIRMED, kho đã bị trừ, lần thanh toán COD vẫn UNPAID.
2. Khách gọi `cancel`. Đơn kiểm tra chưa giao nên cho phép huỷ.
3. Đơn nhập lại hàng vào kho từng biến thể và trả lượt dùng voucher (nếu có).
4. Chưa thu tiền nên không cần hoàn tiền. Đơn chuyển CANCELLED.

**Kịch bản D: Khách COD từ chối nhận hàng**

1. Đơn đang ở SHIPPING, shipper gọi nhưng khách không nhận.
2. Nhân viên ghi nhận `failDelivery`. Đơn chuyển DELIVERY\_FAILED.
3. Hàng được nhập lại kho, lượt voucher được trả. Thanh toán COD vẫn UNPAID nên không có gì để hoàn.

**Kịch bản E: Viết đánh giá**

1. Khách mở sản phẩm đã mua ở kịch bản A và gọi `writeReview` với 5 sao.
2. Khách tự kiểm tra lịch sử: có đơn COMPLETED chứa sản phẩm này, nên được viết.
3. Sản phẩm kiểm tra khách chưa từng đánh giá, rồi nhận đánh giá. Điểm trung bình thay đổi ngay. Lần sau khách muốn đổi ý thì sửa đánh giá cũ.

## 12. Nguyên tắc hướng đối tượng đã áp dụng

Mỗi quyết định thiết kế ở trên đều dựa trên một nguyên tắc cụ thể. Bảng dưới chỉ ra nguyên tắc nào được dùng ở đâu.

| Nguyên tắc | Nội dung | Thể hiện trong hệ thống |
| --- | --- | --- |
| Đóng gói (Encapsulation) | Trạng thái chỉ đổi qua hành vi của chính đối tượng | Đơn đổi trạng thái qua confirm, ship, complete, failDelivery, returnGoods, cancel; biến thể tự giữ, nhả, trừ và nhập kho; voucher tự đếm lượt; thanh toán tự đổi trạng thái |
| Kế thừa (Inheritance) | Lớp con là một dạng của lớp cha | Customer và Employee kế thừa User trừu tượng |
| Trừu tượng (Abstraction) | Chỉ lộ ra cái cần dùng, giấu cách làm | User là lớp trừu tượng; DiscountPolicy là interface chỉ có một hành động tính giảm |
| Đa hình (Polymorphism) | Cùng một lời gọi, mỗi đối tượng xử lý theo cách riêng | calculate() cho kết quả khác nhau ở PercentDiscount và FixedAmountDiscount, không cần câu lệnh rẽ nhánh |
| Information Expert | Ai nắm dữ liệu thì làm việc với dữ liệu đó | Biến thể tính số hàng còn bán; sản phẩm tính tồn kho và điểm trung bình; khách tính tổng chi tiêu; CustomerLevel tự chọn hạng theo ngưỡng |
| Creator | Ai chứa thì tạo | Sản phẩm tạo biến thể; đơn tạo dòng đơn và thanh toán; khách tạo đơn từ giỏ của mình |
| Gắn kết cao (High Cohesion) | Mỗi lớp lo một nhóm việc | Sổ địa chỉ tách khỏi Customer để Customer không ôm thêm việc quản lý địa chỉ |
| Mở – đóng (Open/Closed) | Mở để mở rộng, đóng để sửa đổi | Thêm kiểu giảm giá mới chỉ cần thêm một lớp cài đặt DiscountPolicy |
| Liên kết thấp (Low Coupling) | Chỉ biết những gì thật sự cần | Hầu hết quan hệ một chiều; chỉ Customer – Order và Product – Promotion hai chiều, vì cả hai phía đều cần nhau |
| Composition hơn trùng lặp | Phần dùng chung tách thành đối tượng riêng | Voucher và Promotion cùng dùng DiscountPolicy và Period |
| Value object | Đối tượng xác định bằng giá trị, không đổi sau khi tạo | Address và Period bất biến; đơn giữ bản địa chỉ giao riêng |
| Gom nhóm (package) | Lớp liên quan đặt cùng một nhóm | Bốn package: Tài khoản, Sản phẩm & mua sắm, Đơn hàng & thanh toán, Giảm giá |

Một quy tắc chung xuyên suốt: thuộc tính chỉ có mặt khi đối tượng cần nó để thực hiện hành vi của chính mình. Những gì tính được từ quan hệ (tổng tiền, hạng khách, điểm trung bình) được thể hiện bằng phương thức, không lưu thành thuộc tính.
