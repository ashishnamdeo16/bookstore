# syntax=docker/dockerfile:1

# ---------- Stage 1: build the static files ----------
FROM node:22-alpine AS build
WORKDIR /app

# Vite bakes VITE_* vars into the build, so they must be present at build time.
# For production behind this nginx image, leave VITE_API_BASE_URL empty so the
# browser calls same-origin /auth and /api paths (proxied to the gateway).
ARG VITE_API_BASE_URL=
ARG VITE_STRIPE_PUBLISHABLE_KEY
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_STRIPE_PUBLISHABLE_KEY=$VITE_STRIPE_PUBLISHABLE_KEY

# Install dependencies (cached unless package files change)
COPY package*.json ./
RUN npm ci

# Build the app -> produces /app/dist
COPY . .
RUN npm run build

# ---------- Stage 2: serve with nginx ----------
FROM nginx:1.27-alpine

# envsubst template — API_UPSTREAM is injected at container start.
COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html

ENV API_UPSTREAM=http://api-gateway:8080
# Only substitute API_UPSTREAM — leave nginx $host / $uri variables alone.
ENV NGINX_ENVSUBST_FILTER=API_UPSTREAM

EXPOSE 80
