# Use Node.js for both build and production
FROM node:25-alpine

WORKDIR /app

# Accept build arg for server URL
ARG VITE_SERVER_URL=http://localhost:8080
ENV VITE_SERVER_URL=$VITE_SERVER_URL

# Copy package files
COPY package*.json ./
COPY .npmrc ./

# Install dependencies
RUN npm install

# Copy source code
COPY . .

# Build the application
RUN npm run build

# Expose port 3000 (default for SvelteKit preview)
EXPOSE 3000

# Start the application
CMD ["npm", "run", "preview", "--", "--host", "0.0.0.0", "--port", "3000"]
