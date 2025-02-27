# Stage 1: Build

FROM node:22.14 AS node
WORKDIR /usr/src/app
COPY package.json package-lock.json ./
RUN npm install
COPY . .
RUN npm run build --omit=dev

# Stage 2: Run

FROM nginx:alpine
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=node /usr/src/app/dist/poker /usr/share/poker
