package com.cryptoapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * 🚀 MAIN SPRING BOOT APPLICATION CLASS - THE HEART OF OUR APPLICATION
 * 
 * ==== WHAT IS SPRING BOOT? ====
 * Spring Boot is a Java framework that makes it SUPER EASY to create web applications.
 * Think of it like a pre-built kitchen - instead of building everything from scratch,
 * you get all the tools and appliances ready to use!
 * 
 * ==== WHAT DOES THIS CLASS DO? ====
 * This class is the "main entry point" - like the front door of your house.
 * When you start the application, THIS is the first thing that runs.
 * 
 * ==== KEY SPRING BOOT FEATURES WE'RE USING ====
 * 1. AUTO-CONFIGURATION: Spring Boot automatically sets up common things like:
 *    - Web server (Tomcat) to handle HTTP requests
 *    - Database connections
 *    - JSON parsing for REST APIs
 *    - Security features
 * 
 * 2. DEPENDENCY INJECTION: Spring Boot automatically creates and manages objects for us
 *    - We don't have to manually create Controllers, Services, Repositories
 *    - Spring Boot creates them and "injects" them where needed
 * 
 * 3. COMPONENT SCANNING: Spring Boot automatically finds all our classes marked with
 *    annotations like @Controller, @Service, @Repository and manages them
 * 
 * 4. EMBEDDED WEB SERVER: No need to install Apache Tomcat separately!
 *    Spring Boot includes its own web server that starts automatically
 */

// ==== SPRING BOOT ANNOTATIONS EXPLAINED ====

/**
 * @SpringBootApplication - THIS IS THE MAGIC ANNOTATION! 🪄
 * 
 *                        This ONE annotation is actually THREE annotations
 *                        combined:
 * 
 *                        1. @Configuration - Tells Spring this class can define
 *                        beans (objects Spring manages)
 *                        2. @EnableAutoConfiguration - Tells Spring to
 *                        automatically configure everything
 *                        3. @ComponentScan - Tells Spring to scan this package
 *                        and sub-packages for components
 * 
 *                        Think of it as telling Spring: "Hey, this is the main
 *                        class, start here and
 *                        automatically set up everything you need!"
 */
@SpringBootApplication

/**
 * @EnableScheduling - ENABLES BACKGROUND TASKS 🕒
 * 
 *                   This tells Spring Boot: "I want to run some tasks
 *                   automatically in the background"
 *                   For example: Every 45 seconds, fetch new cryptocurrency
 *                   prices from CoinMarketCap
 * 
 *                   Without this annotation, our @Scheduled methods in
 *                   CryptoService wouldn't work!
 */
@EnableScheduling
public class CryptoBackendApplication {

    /**
     * THE MAIN METHOD - WHERE EVERYTHING STARTS! 🎬
     * 
     * This is just like any regular Java application - it all starts with main()
     * But instead of doing everything ourselves, we let SpringApplication.run()
     * handle all the heavy lifting for us!
     * 
     * WHAT HAPPENS WHEN SpringApplication.run() EXECUTES:
     * 1. Creates an "Application Context" (Spring's container for all objects)
     * 2. Scans for all classes with Spring annotations (@Controller, @Service,
     * etc.)
     * 3. Creates instances of these classes and wires them together
     * 4. Starts the embedded Tomcat web server (usually on port 8080)
     * 5. Sets up all the auto-configurations (database, web layer, etc.)
     * 6. Your application is now ready to receive HTTP requests!
     */
    public static void main(String[] args) {

        // ==== NETWORK CONFIGURATION ====
        // Force IPv4 networking to avoid IPv6 connection issues
        // Some systems prefer IPv6 but external APIs might only support IPv4
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv6Addresses", "false");

        // ==== ENVIRONMENT VARIABLES LOADING ====
        // Load .env file if it exists (for local development)
        // .env files contain sensitive information like API keys, database passwords
        // This keeps secrets out of your code repository!
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(".") // Look in current directory
                    .ignoreIfMissing() // Don't crash if .env doesn't exist
                    .load();

            // Set system properties from .env file
            // This makes environment variables available to Spring Boot's @Value
            // annotations
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });

            System.out.println("✅ Environment variables loaded from .env file");
        } catch (Exception e) {
            System.out.println("⚠️ .env file not found or could not be loaded: " + e.getMessage());
            System.out.println(
                    "   This is okay in production - environment variables should be set by the hosting platform");
        }

        System.out.println("🌐 IPv4 networking enabled");

        // ==== THE BIG MOMENT - START THE SPRING BOOT APPLICATION! ====
        /**
         * SpringApplication.run() is the MAGIC METHOD that:
         * 
         * 1. CREATES APPLICATION CONTEXT: Think of this as a big container that holds
         * all your application objects (Controllers, Services, Repositories)
         * 
         * 2. COMPONENT SCANNING: Looks through all your packages for classes marked
         * with:
         * - @RestController (handles web requests)
         * - @Service (business logic)
         * - @Repository (database access)
         * - @Component (general Spring-managed objects)
         * - @Configuration (configuration classes)
         * 
         * 3. DEPENDENCY INJECTION: Creates instances of these classes and automatically
         * "injects" them into each other where needed (using @Autowired)
         * 
         * 4. AUTO-CONFIGURATION: Based on what's in your classpath (dependencies in
         * pom.xml),
         * Spring Boot automatically configures:
         * - Web layer (for REST APIs)
         * - Database layer (JPA, connection pooling)
         * - JSON serialization/deserialization
         * - Error handling
         * - Security (if Spring Security is included)
         * - And much more!
         * 
         * 5. STARTS EMBEDDED WEB SERVER: Usually Tomcat on port 8080
         * Now your application can receive HTTP requests!
         * 
         * CryptoBackendApplication.class - tells Spring which class is the main
         * application class
         * args - command line arguments (if any)
         */
        SpringApplication.run(CryptoBackendApplication.class, args);

        System.out.println("🚀 Crypto Backend is running!");
        System.out.println("🌐 You can access the application at: http://localhost:8080");
        System.out.println("📊 API endpoints are available at: http://localhost:8080/api/crypto/");
    }
}