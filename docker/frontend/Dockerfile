FROM node:20-alpine AS build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm install
COPY frontend .
RUN npm run build

FROM alpine:3.20
WORKDIR /app
COPY --from=build /app/dist ./dist
CMD ["sh", "-c", "mkdir -p /frontend-dist && cp -r /app/dist/. /frontend-dist && tail -f /dev/null"]
