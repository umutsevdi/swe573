# Use Node.js for both build and production
FROM node:25-alpine

WORKDIR /app



# Copy package files
COPY package*.json ./
COPY .npmrc ./

# Install dependencies
RUN npm install

# Copy source code
COPY . .
ENV PUBLIC_API=http://localhost:8080
# Build the application
RUN npm run build

# Expose port 3000 (default for SvelteKit preview)
EXPOSE 3000

# Start the application
CMD ["npm", "run", "preview", "--", "--host", "0.0.0.0", "--port", "3000"]
