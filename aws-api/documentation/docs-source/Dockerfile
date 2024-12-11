## Base ########################################################################
# Use a larger node image to do the build for native deps (e.g., gcc, python)
FROM registry.wipo.int/docker.io/node:lts as base

RUN npm config set registry https://intranet.wipo.int/nexus/repository/wipo-npm-central/
RUN npm config set ca ""
RUN npm config set strict-ssl false
RUN export NODE_TLS_REJECT_UNAUTHORIZED=0

RUN yarn config set registry https://intranet.wipo.int/nexus/repository/wipo-npm-central/
RUN yarn config set ca ""
RUN yarn config set strict-ssl false
RUN export NODE_TLS_REJECT_UNAUTHORIZED=0

# Reduce npm log spam and colour during install within Docker
ENV NPM_CONFIG_LOGLEVEL=warn
ENV NPM_CONFIG_COLOR=false

# We'll run the app as the `node` user, so put it in their home directory
WORKDIR /home/node/app
# Copy the source code over
# COPY --chown=node:node app /home/node/app/
COPY app /home/node/app/

## Development #################################################################
# Define a development target that installs devDeps and runs in dev mode
FROM base as development
WORKDIR /home/node/app
# Install (not ci) with dependencies, and for Linux vs. Linux Musl (which we use for -alpine)
RUN yarn

RUN yarn clean-api-all
RUN yarn gen-api-all

# Switch to the node user vs. root
# USER node
# Expose port 3000
EXPOSE 3000
# Start the app in debug mode so we can attach the debugger
CMD ["yarn", "start"]

## Production ##################################################################
# Also define a production target which doesn't use devDeps
FROM base as production

RUN apt update
RUN apt install zip

WORKDIR /home/node/app
COPY --from=development /home/node/app /home/node/app
# Build the Docusaurus app
RUN npm run build

# RUN cd build
RUN cd build && zip -r doc.zip *

## Deploy ######################################################################
# Use a stable nginx image
FROM nginx:stable-alpine as deploy
WORKDIR /home/node/app
# Copy what we've installed/built from production
COPY --from=production /home/node/app/build /usr/share/nginx/html/
# COPY --from=production /home/node/app/doc.zip /usr/share/nginx/html/

