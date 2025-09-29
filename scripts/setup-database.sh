#!/bin/bash

# Review Service Database Setup Script
# This script sets up the database for both Docker and local MySQL installations

set -e

# Default values
DB_NAME="review_db"
DB_USER="reviewuser"
DB_PASSWORD="reviewpass"
DB_ROOT_PASSWORD="rootpass"
DB_HOST="localhost"
DB_PORT="3306"
DOCKER_CONTAINER_NAME="review-mysql"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Review Service Database Setup ===${NC}"

# Function to check if Docker is running
check_docker() {
    if ! command -v docker >/dev/null 2>&1; then
        return 1
    fi
    if ! docker info >/dev/null 2>&1; then
        return 1
    fi
    return 0
}

# Function to check if MySQL is running locally
check_local_mysql() {
    if command -v mysql >/dev/null 2>&1; then
        # Check if MySQL service is running (Windows/Linux compatible)
        if netstat -an 2>/dev/null | grep -qi ":${DB_PORT}.*LISTEN" || \
           ss -tuln 2>/dev/null | grep -q ":${DB_PORT}" || \
           mysql -h "$DB_HOST" -P "$DB_PORT" -uroot -e "SELECT 1;" >/dev/null 2>&1; then
            return 0
        fi
    fi
    return 1
}

# Function to setup Docker MySQL
setup_docker_mysql() {
    echo -e "${YELLOW}Setting up Docker MySQL...${NC}"

    # Check if container already exists
    if docker ps -a --format 'table {{.Names}}' | grep -q "^${DOCKER_CONTAINER_NAME}$"; then
        echo -e "${YELLOW}Container ${DOCKER_CONTAINER_NAME} already exists.${NC}"

        # Check if it's running
        if docker ps --format 'table {{.Names}}' | grep -q "^${DOCKER_CONTAINER_NAME}$"; then
            echo -e "${GREEN}Container ${DOCKER_CONTAINER_NAME} is already running.${NC}"
        else
            echo -e "${YELLOW}Starting existing container...${NC}"
            docker start "$DOCKER_CONTAINER_NAME"
        fi
    else
        echo -e "${YELLOW}Creating new MySQL container...${NC}"
        docker run -d \
            --name "$DOCKER_CONTAINER_NAME" \
            -e MYSQL_DATABASE="$DB_NAME" \
            -e MYSQL_USER="$DB_USER" \
            -e MYSQL_PASSWORD="$DB_PASSWORD" \
            -e MYSQL_ROOT_PASSWORD="$DB_ROOT_PASSWORD" \
            -p "${DB_PORT}:3306" \
            mysql:8.0
    fi

    # Wait for MySQL to be ready
    echo -e "${YELLOW}Waiting for MySQL to be ready...${NC}"
    max_attempts=30
    attempt=1

    while [ $attempt -le $max_attempts ]; do
        if docker exec "$DOCKER_CONTAINER_NAME" mysql -u"$DB_USER" -p"$DB_PASSWORD" -e "SELECT 1;" >/dev/null 2>&1; then
            echo -e "${GREEN}MySQL is ready!${NC}"
            break
        fi
        echo -e "${YELLOW}Attempt $attempt/$max_attempts - MySQL not ready yet...${NC}"
        sleep 2
        attempt=$((attempt + 1))
    done

    if [ $attempt -gt $max_attempts ]; then
        echo -e "${RED}MySQL failed to start within expected time.${NC}"
        exit 1
    fi
}

# Function to setup local MySQL
setup_local_mysql() {
    echo -e "${YELLOW}Setting up local MySQL...${NC}"

    # Try to connect as root without password first
    if mysql -h"$DB_HOST" -P"$DB_PORT" -uroot -e "SELECT 1;" >/dev/null 2>&1; then
        ROOT_CMD="mysql -h${DB_HOST} -P${DB_PORT} -uroot"
    else
        echo -e "${YELLOW}Please enter MySQL root password:${NC}"
        read -s ROOT_PASSWORD
        ROOT_CMD="mysql -h${DB_HOST} -P${DB_PORT} -uroot -p${ROOT_PASSWORD}"
    fi

    echo -e "${YELLOW}Creating database and user...${NC}"
    $ROOT_CMD <<EOF
CREATE DATABASE IF NOT EXISTS $DB_NAME;
CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASSWORD';
CREATE USER IF NOT EXISTS '$DB_USER'@'%' IDENTIFIED BY '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'localhost';
GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'%';
FLUSH PRIVILEGES;
EOF

    echo -e "${GREEN}✓ Local MySQL database setup completed!${NC}"
}

# Function to verify database connection
verify_connection() {
    echo -e "${YELLOW}Verifying database connection...${NC}"

    if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "SELECT 'Connection successful' as status;" 2>/dev/null; then
        echo -e "${GREEN}✓ Database connection verified successfully!${NC}"
        return 0
    else
        echo -e "${RED}✗ Database connection failed!${NC}"
        return 1
    fi
}

# Function to display connection info
show_connection_info() {
    echo -e "${BLUE}=== Database Connection Information ===${NC}"
    echo -e "${GREEN}Database URL:${NC} jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME"
    echo -e "${GREEN}Username:${NC} $DB_USER"
    echo -e "${GREEN}Password:${NC} $DB_PASSWORD"
    echo -e "${GREEN}Database Name:${NC} $DB_NAME"
    echo ""
    echo -e "${BLUE}=== Environment Variables ===${NC}"
    echo -e "${GREEN}DB_USERNAME=$DB_USER"
    echo -e "${GREEN}DB_PASSWORD=$DB_PASSWORD"
    echo ""
}

# Main logic
main() {
    echo -e "${BLUE}Checking available options...${NC}"

    # Try Docker first
    if check_docker; then
        echo -e "${GREEN}✓ Docker is available${NC}"
        setup_docker_mysql
    elif check_local_mysql; then
        echo -e "${GREEN}✓ Local MySQL is available${NC}"
        setup_local_mysql
    else
        echo -e "${RED}Neither Docker nor local MySQL is available.${NC}"
        echo -e "${YELLOW}Please install and start either Docker or MySQL.${NC}"
        exit 1
    fi

    # Verify the setup
    if verify_connection; then
        show_connection_info
        echo -e "${GREEN}=== Database setup completed successfully! ===${NC}"
        echo -e "${YELLOW}You can now run the Review Service with:${NC}"
        echo -e "${BLUE}mvn spring-boot:run${NC}"
    else
        echo -e "${RED}Database setup failed. Please check the logs above.${NC}"
        exit 1
    fi
}

# Run main function
main "$@"