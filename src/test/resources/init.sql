-- Test database initialization script
-- This will be run when Testcontainers starts the MySQL container

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS usermanagementservice;
USE usermanagementservice;

-- The JPA will handle table creation with ddl-auto: create-drop
-- This file can be used for any additional test data setup if needed
