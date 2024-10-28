FROM ubuntu:22.04 as build

ARG NGINX_VERSION=1.27.0

# install yarn maven
RUN apt-get update && apt-get install -y curl gpg
RUN curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | gpg --dearmor -o /usr/share/keyrings/yarn-archive-keyring.gpg \
    && echo "deb [signed-by=/usr/share/keyrings/yarn-archive-keyring.gpg] https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt update && apt install -y nodejs 
RUN npm config set registry https://registry.npmmirror.com/ \
    && npm -g install yarn \
    && yarn config set strict-ssl false

WORKDIR /
COPY web web
RUN cd web \
    && yarn install \
    && yarn build 
    # result in /web/build

# Install nginx
RUN apt-get update && apt-get -y install --no-install-recommends wget openssl libssl-dev build-essential \
    && wget https://nginx.org/download/nginx-${NGINX_VERSION}.tar.gz \
    && tar -xzvf nginx-${NGINX_VERSION}.tar.gz

# Download and extract the upload module
RUN wget https://github.com/vkholodkov/nginx-upload-module/archive/2.255.tar.gz \
    && tar -xzvf 2.255.tar.gz

# Download zlib
RUN wget https://zlib.net/zlib-1.3.1.tar.gz \
    && tar -xzvf zlib-1.3.1.tar.gz

# Download pcre2
RUN wget https://github.com/PCRE2Project/pcre2/releases/download/pcre2-10.44/pcre2-10.44.tar.gz \
    && tar -xzvf pcre2-10.44.tar.gz

# Build and install Nginx
WORKDIR /nginx-${NGINX_VERSION}
RUN ./configure \
    --with-http_ssl_module \
    --with-http_gzip_static_module \
    --with-zlib=../zlib-1.3.1 \
    --with-pcre=../pcre2-10.44 \
    --add-module=../nginx-upload-module-2.255
RUN make && make install


FROM ubuntu:22.04

ARG DEBIAN_FRONTEND=noninteractive
ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8
ENV LANGUAGE=C.UTF-8

RUN apt-get update && apt-get install -y openssl

# Install nginx
COPY --from=build /usr/local/nginx /usr/local/nginx
COPY --from=build /web/build /opt/supra/web

# Copy nginx configuration
COPY conf/* /usr/local/nginx/conf/
COPY entry.sh /opt/supra/bin/entry.sh
RUN mkdir -p /opt/supra/logs \
    && mkdir -p /opt/supra/cert \
    && chmod +x /opt/supra/bin/entry.sh

# Expose the nginx port
EXPOSE 443

CMD /opt/supra/bin/entry.sh
