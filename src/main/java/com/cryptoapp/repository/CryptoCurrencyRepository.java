package com.cryptoapp.repository;

import com.cryptoapp.model.CryptoCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 💾 CRYPTO CURRENCY REPOSITORY - THE DATA ACCESS LAYER
 * 
 * ==== WHAT IS A REPOSITORY IN SPRING BOOT? ====
 * A Repository is the "file clerk" of your application - it handles all database operations.
 * Think of it like this:
 * 
 * - Controller = Receptionist (handles HTTP requests)
 * - Service = Manager (business logic and coordination)
 * - Repository = File Clerk (stores, retrieves, and organizes data)
 * 
 * ==== SPRING DATA JPA MAGIC ====
 * This interface extends JpaRepository, which gives us TONS of functionality for FREE:
 * - save() - Insert or update records
 * - findById() - Find a record by its ID
 * - findAll() - Get all records
 * - delete() - Remove records
 * - count() - Count total records
 * - And many more!
 * 
 * We don't have to write any SQL or implementation code - Spring Boot generates it all!
 * 
 * ==== HOW DOES THIS MAGIC WORK? ====
 * 1. Spring Boot sees this interface extends JpaRepository
 * 2. At startup, it automatically creates a PROXY IMPLEMENTATION
 * 3. The proxy handles all the database operations using JPA/Hibernate
 * 4. When other classes @Autowire this repository, they get the proxy
 * 5. The proxy translates method calls into SQL queries automatically!
 * 
 * ==== JPA vs HIBERNATE vs SPRING DATA JPA ====
 * - JPA = Java Persistence API (the standard specification)
 * - Hibernate = The implementation of JPA (the actual engine)
 * - Spring Data JPA = Spring's wrapper that makes JPA super easy to use
 * 
 * ==== OBJECT-RELATIONAL MAPPING (ORM) ====
 * ORM means mapping between Java objects and database tables:
 * - Java Class CryptoCurrency ↔ Database table cryptocurrencies
 * - Java object fields ↔ Database table columns
 * - Java method calls ↔ SQL queries
 * 
 * This lets us work with objects instead of writing SQL everywhere!
 */

// ==== SPRING BOOT ANNOTATIONS EXPLAINED ====

/**
 * @Repository - MARKS THIS AS A DATA ACCESS COMPONENT 💾
 * 
 *             This tells Spring Boot: "This interface handles database
 *             operations"
 * 
 *             What @Repository does:
 *             1. Spring Boot automatically creates a proxy implementation
 *             2. Enables exception translation (database errors → Spring
 *             exceptions)
 *             3. Makes it available for dependency injection (@Autowired)
 *             4. Enables transaction support
 *             5. Provides additional data access features
 * 
 *             Note: @Repository is technically optional when extending
 *             JpaRepository,
 *             but it's good practice to include it for clarity and potential
 *             future features.
 */
@Repository

/**
 * JpaRepository<CryptoCurrency, Long> - THE GENERIC INTERFACE 🎯
 * 
 * This generic interface tells Spring Boot:
 * - CryptoCurrency = The entity class (maps to database table)
 * - Long = The type of the primary key (ID field)
 * 
 * By extending JpaRepository, we automatically get these methods:
 * - save(entity) - Insert or update
 * - saveAll(entities) - Batch save
 * - findById(id) - Find by primary key
 * - findAll() - Get all records
 * - deleteById(id) - Delete by primary key
 * - delete(entity) - Delete specific entity
 * - count() - Count total records
 * - existsById(id) - Check if record exists
 * 
 * These are all generated automatically - no code needed!
 */
public interface CryptoCurrencyRepository extends JpaRepository<CryptoCurrency, Long> {

  // ==== CUSTOM QUERY METHODS ====

  /**
   * FIND LATEST RECORD BY SYMBOL - CUSTOM JPQL QUERY 🔍
   * 
   * @Query - CUSTOM QUERY ANNOTATION
   * 
   *        Sometimes the auto-generated methods aren't enough, so we write custom
   *        queries.
   *        This uses JPQL (Java Persistence Query Language) - it's like SQL but
   *        for Java objects.
   * 
   *        JPQL vs SQL differences:
   *        - SELECT c FROM CryptoCurrency c (references Java class, not table
   *        name)
   *        - c.symbol (references Java field, not column name)
   *        - LOWER() functions work the same
   *        - Parameters use :paramName instead of ? placeholders
   * 
   *        @Param("symbol") - PARAMETER BINDING
   *        - Binds the method parameter to the :symbol in the query
   *        - Spring Boot automatically handles SQL injection prevention
   *        - Type-safe parameter binding
   * 
   *        Optional<CryptoCurrency> - SAFE RETURN TYPE
   *        - Optional handles "not found" cases safely
   *        - No more NullPointerExceptions!
   *        - Caller must explicitly handle empty case
   * 
   *        Business Logic: Find the most recent record for a cryptocurrency
   *        symbol
   *        Useful for getting current price data when we have historical records
   */
  @Query("SELECT c FROM CryptoCurrency c WHERE LOWER(c.symbol) = LOWER(:symbol) ORDER BY c.createdAt DESC LIMIT 1")
  Optional<CryptoCurrency> findLatestBySymbolIgnoreCase(@Param("symbol") String symbol);

  /**
   * FIND TOP 10 LATEST CRYPTOCURRENCIES - COMPLEX JPQL QUERY 🏆
   * 
   * This query demonstrates advanced JPQL features:
   * 
   * SUBQUERY EXPLANATION:
   * 1. Inner query: SELECT MAX(c2.id) FROM CryptoCurrency c2 GROUP BY
   * c2.coinMarketCapId
   * - Groups all records by coinMarketCapId (same crypto)
   * - Finds the highest ID (most recent record) for each crypto
   * - Returns list of IDs representing latest records
   * 
   * 2. Outer query: SELECT c FROM CryptoCurrency c WHERE c.id IN (...)
   * - Gets full records where ID is in the subquery results
   * - Filters for records with valid CMC rank
   * - Orders by CMC rank (Bitcoin=1, Ethereum=2, etc.)
   * - Limits to top 10
   * 
   * Why this complexity?
   * - We store historical data (multiple records per crypto)
   * - We want only the LATEST record for each crypto for the dashboard
   * - This efficiently gets the current state of top cryptocurrencies
   * 
   * TEXT BLOCKS ("""):
   * - Java 15+ feature for multi-line strings
   * - Makes complex queries more readable
   * - No need to concatenate strings with +
   */
  @Query("""
      SELECT c FROM CryptoCurrency c
      WHERE c.id IN (
          SELECT MAX(c2.id) FROM CryptoCurrency c2
          GROUP BY c2.coinMarketCapId
      )
      AND c.cmcRank IS NOT NULL
      ORDER BY c.cmcRank ASC
      LIMIT 10
      """)
  List<CryptoCurrency> findTop10LatestByOrderByCmcRankAsc();

  /**
   * SEARCH CRYPTOCURRENCIES - FLEXIBLE SEARCH WITH PATTERN MATCHING 🔎
   * 
   * This query demonstrates:
   * 
   * PATTERN MATCHING:
   * - LIKE LOWER(CONCAT('%', :searchTerm, '%'))
   * - CONCAT builds pattern: '%bitcoin%' for search term "bitcoin"
   * - % = wildcard (matches any characters)
   * - LOWER() makes search case-insensitive
   * 
   * LOGICAL OPERATORS:
   * - OR condition searches both name AND symbol
   * - User can search "BTC" or "Bitcoin" and find the same crypto
   * 
   * SAME SUBQUERY PATTERN:
   * - Uses same "latest records only" logic as above
   * - Ensures search results show current data, not historical
   * 
   * Business Use Cases:
   * - Search bar in frontend
   * - Auto-complete functionality
   * - Fuzzy matching for user convenience
   */
  @Query("""
      SELECT c FROM CryptoCurrency c
      WHERE c.id IN (
          SELECT MAX(c2.id) FROM CryptoCurrency c2
          GROUP BY c2.coinMarketCapId
      )
      AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
           OR LOWER(c.symbol) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
      ORDER BY c.cmcRank ASC
      """)
  List<CryptoCurrency> searchLatestByNameOrSymbol(@Param("searchTerm") String searchTerm);

  /**
   * GET HISTORICAL DATA BY COINMARKETCAP ID - TIME SERIES DATA 📈
   * 
   * This query gets ALL historical records for a specific cryptocurrency.
   * Used for:
   * - Analytics charts showing price over time
   * - Statistical calculations (volatility, trends, etc.)
   * - Historical performance analysis
   * 
   * ORDER BY c.createdAt DESC:
   * - Most recent records first
   * - Important for time-series analysis
   * - Consistent ordering for frontend processing
   * 
   * No LIMIT clause:
   * - Returns ALL historical data
   * - Service layer can limit if needed for performance
   * - Flexibility for different use cases
   */
  @Query("SELECT c FROM CryptoCurrency c WHERE c.coinMarketCapId = :coinMarketCapId ORDER BY c.createdAt DESC")
  List<CryptoCurrency> findHistoryByCoinMarketCapId(@Param("coinMarketCapId") Long coinMarketCapId);

  /**
   * GET HISTORICAL DATA BY SYMBOL - ALTERNATIVE LOOKUP METHOD 📊
   * 
   * Similar to above but searches by symbol instead of ID.
   * Useful when:
   * - Frontend only knows the symbol (BTC, ETH, etc.)
   * - Building analytics for user-requested cryptocurrencies
   * - Symbol is more user-friendly than internal database IDs
   * 
   * Case-insensitive search:
   * - Users might type "btc", "BTC", or "Btc"
   * - LOWER() ensures all variations match
   * - Better user experience
   */
  @Query("SELECT c FROM CryptoCurrency c WHERE LOWER(c.symbol) = LOWER(:symbol) ORDER BY c.createdAt DESC")
  List<CryptoCurrency> findHistoryBySymbol(@Param("symbol") String symbol);
}

/**
 * 🎓 KEY SPRING BOOT REPOSITORY CONCEPTS DEMONSTRATED:
 * 
 * 1. INTERFACE-BASED REPOSITORIES
 * - We define an interface, Spring Boot creates the implementation
 * - No boilerplate code, just method signatures
 * - Automatic CRUD operations through JpaRepository
 * 
 * 2. JPQL (JAVA PERSISTENCE QUERY LANGUAGE)
 * - Object-oriented query language for JPA
 * - References Java classes/fields, not database tables/columns
 * - Type-safe and IDE-friendly
 * 
 * 3. PARAMETER BINDING
 * - @Param annotation safely binds method parameters to query
 * - Automatic SQL injection prevention
 * - Clean, readable parameter names in queries
 * 
 * 4. OPTIONAL RETURN TYPES
 * - Optional<T> for "might not exist" queries
 * - Forces callers to handle empty results
 * - Eliminates NullPointerExceptions
 * 
 * 5. COMPLEX QUERIES WITH SUBQUERIES
 * - GROUP BY for aggregating related records
 * - MAX() function to find latest records
 * - IN clause to filter based on subquery results
 * 
 * 6. PATTERN MATCHING & SEARCH
 * - LIKE operator for fuzzy text matching
 * - CONCAT for building search patterns
 * - Case-insensitive searching with LOWER()
 * 
 * 7. AUTOMATIC TRANSACTION MANAGEMENT
 * - Each repository method runs in a transaction
 * - Automatic commit/rollback on success/failure
 * - Integration with @Transactional in service layer
 * 
 * 8. DATABASE ABSTRACTION
 * - Same JPQL works with PostgreSQL, MySQL, H2, etc.
 * - Spring Boot handles database-specific SQL generation
 * - Easy to switch databases without changing queries
 * 
 * This is the Spring Boot way: Define what you want, let the framework handle
 * how! 🚀
 */