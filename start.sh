#!/bin/bash

echo "=========================================="
echo "  Seckill System - Quick Start Script"
echo "=========================================="
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "Error: Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "Error: Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

echo "Step 1: Building Java microservices..."
echo "This may take a few minutes on first run..."
echo ""

# Build all services
docker-compose build

echo ""
echo "Step 2: Starting all services..."
echo ""

# Start all services
docker-compose up -d

echo ""
echo "Step 3: Waiting for services to be ready..."
echo "This may take 1-2 minutes..."
echo ""

# Wait for services to be healthy
sleep 30

echo ""
echo "=========================================="
echo "  Seckill System Started Successfully!"
echo "=========================================="
echo ""
echo "Service URLs:"
echo "  - Frontend:        http://localhost"
echo "  - API Gateway:     http://localhost:8080"
echo "  - Nacos Console:   http://localhost:8848/nacos (username: nacos, password: nacos)"
echo "  - RabbitMQ Console: http://localhost:15672 (username: guest, password: guest)"
echo ""
echo "Test Accounts:"
echo "  - Username: testuser1, Password: password"
echo "  - Username: testuser2, Password: password"
echo "  - Username: testuser3, Password: password"
echo ""
echo "To view logs: docker-compose logs -f [service-name]"
echo "To stop: docker-compose down"
echo "To stop and remove data: docker-compose down -v"
echo ""
echo "=========================================="
