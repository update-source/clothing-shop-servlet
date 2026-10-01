<c:set var="pageTitle" value="${empty selectedCategory ? 'Cửa hàng' : selectedCategory.name}"/>
<c:set var="activeNav" value="shop"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="bg-light py-3">
  <div class="container">
    <div class="row">
      <div class="col-md-12 mb-0">
        <a href="${ctx}/">Trang chủ</a> <span class="mx-2 mb-0">/</span>
        <c:choose>
          <c:when test="${empty selectedCategory}"><strong class="text-black">Cửa hàng</strong></c:when>
          <c:otherwise>
            <a href="${ctx}/shop">Cửa hàng</a>
            <c:if test="${not empty selectedCategory.parent}">
              <span class="mx-2 mb-0">/</span>
              <a href="${ctx}/shop?category=${selectedCategory.parent.id}"><c:out value="${selectedCategory.parent.name}"/></a>
            </c:if>
            <span class="mx-2 mb-0">/</span> <strong class="text-black"><c:out value="${selectedCategory.name}"/></strong>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>
</div>

<div class="site-section">
  <div class="container">
    <form id="filter-form" action="${ctx}/shop" method="get">
    <c:if test="${not empty selectedCategory}"><input type="hidden" name="category" value="${selectedCategory.id}"></c:if>
    <c:if test="${not empty param.q}"><input type="hidden" name="q" value="${fn:escapeXml(param.q)}"></c:if>
    <div class="row mb-5">
      <div class="col-md-9 order-2">
        <div class="row">
          <div class="col-md-12 mb-5">
            <div class="float-md-left mb-4">
              <h2 class="text-black h5">
                <c:choose>
                  <c:when test="${not empty param.q}">Kết quả cho "<c:out value="${param.q}"/>"</c:when>
                  <c:when test="${not empty selectedCategory}"><c:out value="${selectedCategory.name}"/></c:when>
                  <c:otherwise>Tất cả sản phẩm</c:otherwise>
                </c:choose>
                <small class="text-muted">(${result.total} sản phẩm)</small>
              </h2>
            </div>
            <div class="d-flex">
              <div class="ml-md-auto">
                <select name="sort" class="form-control form-control-sm" onchange="this.form.submit()" aria-label="Sắp xếp">
                  <option value="newest" ${sort == 'newest' ? 'selected' : ''}>Mới nhất</option>
                  <option value="name_asc" ${sort == 'name_asc' ? 'selected' : ''}>Tên A → Z</option>
                  <option value="name_desc" ${sort == 'name_desc' ? 'selected' : ''}>Tên Z → A</option>
                  <option value="price_asc" ${sort == 'price_asc' ? 'selected' : ''}>Giá thấp → cao</option>
                  <option value="price_desc" ${sort == 'price_desc' ? 'selected' : ''}>Giá cao → thấp</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        <div class="row mb-5">
          <c:if test="${empty result.items}">
            <div class="col-12"><div class="alert alert-info">Không tìm thấy sản phẩm phù hợp. Hãy thử bỏ bớt bộ lọc.</div></div>
          </c:if>
          <c:forEach items="${result.items}" var="p">
            <div class="col-sm-6 col-lg-4 mb-4" data-aos="fade-up">
              <t:product-card product="${p}" bordered="true"/>
            </div>
          </c:forEach>
        </div>

        <c:if test="${result.totalPages > 1}">
          <div class="row" data-aos="fade-up">
            <div class="col-md-12 text-center">
              <div class="site-block-27">
                <ul>
                  <c:if test="${result.hasPrevious}">
                    <li><a href="${ctx}/shop?${empty pageQuery ? '' : pageQuery.concat('&')}page=${result.page - 1}">&lt;</a></li>
                  </c:if>
                  <c:forEach begin="1" end="${result.totalPages}" var="i">
                    <c:choose>
                      <c:when test="${i == result.page}"><li class="active"><span>${i}</span></li></c:when>
                      <c:otherwise><li><a href="${ctx}/shop?${empty pageQuery ? '' : pageQuery.concat('&')}page=${i}">${i}</a></li></c:otherwise>
                    </c:choose>
                  </c:forEach>
                  <c:if test="${result.hasNext}">
                    <li><a href="${ctx}/shop?${empty pageQuery ? '' : pageQuery.concat('&')}page=${result.page + 1}">&gt;</a></li>
                  </c:if>
                </ul>
              </div>
            </div>
          </div>
        </c:if>
      </div>

      <div class="col-md-3 order-1 mb-5 mb-md-0">
        <div class="border p-4 rounded mb-4">
          <h3 class="mb-3 h6 text-uppercase text-black d-block">Danh mục</h3>
          <ul class="list-unstyled mb-0">
            <li class="mb-1">
              <a href="${ctx}/shop" class="d-flex ${empty selectedCategory ? 'font-weight-bold' : ''}"><span>Tất cả</span></a>
            </li>
            <c:forEach items="${nav.categories}" var="node">
              <li class="mb-1">
                <a href="${ctx}/shop?category=${node.category.id}"
                   class="d-flex ${selectedCategory.id == node.category.id ? 'font-weight-bold' : ''}">
                  <span><c:out value="${node.category.name}"/></span>
                  <span class="text-black ml-auto">(${node.productCount})</span>
                </a>
                <c:if test="${not empty node.children}">
                  <ul class="list-unstyled ml-3 mt-1">
                    <c:forEach items="${node.children}" var="child">
                      <li class="mb-1 small">
                        <a href="${ctx}/shop?category=${child.id}"
                           class="d-flex ${selectedCategory.id == child.id ? 'font-weight-bold' : ''}">
                          <span><c:out value="${child.name}"/></span>
                          <span class="text-black ml-auto">(${empty counts[child.id] ? 0 : counts[child.id]})</span>
                        </a>
                      </li>
                    </c:forEach>
                  </ul>
                </c:if>
              </li>
            </c:forEach>
          </ul>
        </div>

        <div class="border p-4 rounded mb-4">
          <div class="mb-4">
            <h3 class="mb-3 h6 text-uppercase text-black d-block">Lọc theo giá</h3>
            <div id="slider-range" class="border-primary" data-min="0" data-max="2000000"
                 data-from="${empty minPrice ? 0 : f:plain(minPrice)}" data-to="${empty maxPrice ? 2000000 : f:plain(maxPrice)}"></div>
            <input type="text" id="amount" class="form-control border-0 pl-0 bg-white" disabled>
            <input type="hidden" id="minPrice" name="minPrice" value="${f:plain(minPrice)}">
            <input type="hidden" id="maxPrice" name="maxPrice" value="${f:plain(maxPrice)}">
          </div>

          <div class="mb-4">
            <h3 class="mb-3 h6 text-uppercase text-black d-block">Size</h3>
            <c:forEach items="${allSizes}" var="s">
              <label for="s_${s}" class="d-flex">
                <input type="checkbox" id="s_${s}" name="size" value="${s}" class="mr-2 mt-1"
                  ${selectedSizes.contains(s) ? 'checked' : ''}>
                <span class="text-black">${s}</span>
              </label>
            </c:forEach>
          </div>

          <div class="mb-4">
            <h3 class="mb-3 h6 text-uppercase text-black d-block">Màu</h3>
            <c:forEach items="${allColors}" var="col" varStatus="st">
              <label for="c_${st.index}" class="d-flex align-items-center color-item">
                <input type="checkbox" id="c_${st.index}" name="color" value="${fn:escapeXml(col)}" class="mr-2"
                  ${selectedColors.contains(col) ? 'checked' : ''}>
                <span class="color d-inline-block rounded-circle mr-2 border"
                      style="background-color: ${f:colorHex(col)}"></span>
                <span class="text-black"><c:out value="${col}"/></span>
              </label>
            </c:forEach>
          </div>

          <button type="submit" class="btn btn-primary btn-sm btn-block">Áp dụng bộ lọc</button>
          <a href="${ctx}/shop${empty selectedCategory ? '' : '?category='.concat(selectedCategory.id)}"
             class="btn btn-link btn-sm btn-block">Xoá bộ lọc</a>
        </div>
      </div>
    </div>
    </form>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
</body>
</html>
