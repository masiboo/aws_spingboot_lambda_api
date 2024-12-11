FROM ruby:2.6.5-alpine

RUN echo -e '--- \nsources:\n- http://rubygems.org/' >> ~/.gemrc

# RUN echo -e '--- \nsources:\n- http://rubygems.org/' >> ~/.gemrc
RUN echo -e "\n:ssl_verify_mode: 0" >> ~/.gemrc

RUN gem update --system --source http://rubygems.org/

RUN apk update \
    && apk upgrade --no-cache \
    && apk add --no-cache --repository http://dl-cdn.alpinelinux.org/alpine/v3.14/main/ nodejs=14.20.0-r0 npm \
    && apk add git \
    && apk add --no-cache build-base

RUN npm set registry https://intranet.wipo.int/nexus/repository/wipo-npm-central/

# Install node packages and gems
RUN npm install -g widdershins \
    && gem install bundler --source http://rubygems.org

# working directory
WORKDIR /openapi

RUN git -c http.sslVerify=false clone https://github.com/PigStyAlleyCoders/slate-insecure.git
RUN cd slate-insecure \
    && bundle install