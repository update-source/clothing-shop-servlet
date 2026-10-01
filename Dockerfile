# ---------------------------------------------------------------------------
# Image chạy ứng dụng trên Tomcat 10.1 (Jakarta EE 10) + Java 21.
#   docker build -t clothing-shop .
#   docker run -d -p 8080:8080 -v shop-data:/data clothing-shop
# Cấu hình bằng biến môi trường SHOP_<KHOÁ> (db.url -> SHOP_DB_URL, ...), xem docs/07.
# ---------------------------------------------------------------------------

# --- Giai đoạn 1: build WAR bằng Maven ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

# Tải thư viện trước để Docker cache lại lớp này khi chỉ sửa mã nguồn
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Đặt SKIP_TESTS=true để build nhanh hơn: docker build --build-arg SKIP_TESTS=true .
ARG SKIP_TESTS=false
RUN mvn -B -q package -DskipTests=${SKIP_TESTS}

# --- Giai đoạn 2: chạy ---
FROM tomcat:10.1-jre21-temurin

# - Sau reverse proxy (Nginx, Caddy, Render...), lấy scheme/IP thật từ X-Forwarded-Proto / X-Forwarded-For
#   để return URL của VNPAY là https://<tên miền>/... và cookie phiên có cờ Secure.
# - Cổng HTTP lấy từ biến PORT nếu nền tảng đặt (Render: 10000), mặc định 8080.
RUN sed -i 's#</Host>#  <Valve className="org.apache.catalina.valves.RemoteIpValve" remoteIpHeader="X-Forwarded-For" protocolHeader="X-Forwarded-Proto" />\n      </Host>#' \
        conf/server.xml \
    && sed -i 's#<Connector port="8080"#<Connector port="${port.http}"#' conf/server.xml \
    && printf '%s\n' 'CATALINA_OPTS="$CATALINA_OPTS -Dport.http=${PORT:-8080}"' > bin/setenv.sh \
    && groupadd --system shop \
    && useradd --system --gid shop --home-dir /data --no-create-home shop \
    && mkdir -p /data/db /data/uploads \
    && chown -R shop:shop /data /usr/local/tomcat

# Triển khai dạng thư mục: không phải giải nén WAR mỗi lần khởi động
COPY --from=build --chown=shop:shop /src/target/clothing-shop webapps/ROOT

ENV TZ=Asia/Ho_Chi_Minh \
    JAVA_OPTS="-XX:MaxRAMPercentage=75" \
    SHOP_DB_URL="jdbc:h2:file:/data/db/shop;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH" \
    SHOP_UPLOAD_DIR=/data/uploads

USER shop
VOLUME /data
EXPOSE 8080

# Dùng chung một cookie để kiểm tra định kỳ không tạo phiên (session) mới mỗi lần
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS -o /dev/null -c /tmp/healthcheck.cookie -b /tmp/healthcheck.cookie "http://127.0.0.1:${PORT:-8080}/" || exit 1
