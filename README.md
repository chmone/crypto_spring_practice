# 🚀 Full-Stack Crypto Backend - Complete Learning Project

A complete full-stack cryptocurrency application built with **Spring Boot**, **CoinMarketCap API**, **Supabase PostgreSQL**, and a modern **JavaScript frontend**. Perfect for learning modern web development!

## 🌟 What We've Built

This is now a **complete full-stack application** featuring:

### 🎯 Backend (Spring Boot)
- **Real CoinMarketCap API integration** for live crypto data
- **Supabase PostgreSQL database** for data persistence
- **RESTful API endpoints** with comprehensive error handling
- **Data caching and synchronization** between API and database
- **Spring Data JPA** for database operations
- **Environment variable configuration** with `.env` support

### 🎨 Frontend (Modern Web UI)
- **Responsive dark-themed interface** with gradient designs
- **Real-time cryptocurrency data display** with live prices
- **Search functionality** to find specific cryptocurrencies
- **Portfolio calculator** for multiple crypto holdings
- **Health monitoring** with API and database status indicators
- **Auto-refresh** every 5 minutes

### 💾 Database (Supabase PostgreSQL)
- **Cryptocurrency data table** with comprehensive schema
- **Automatic data synchronization** with CoinMarketCap
- **Query optimization** with proper indexes
- **Auto-updating timestamps** with triggers

## 📁 Project Structure

```
practice_spring/
├── src/main/java/com/cryptoapp/
│   ├── CryptoBackendApplication.java          # 🚀 Main Spring Boot app
│   ├── controller/
│   │   └── CryptoController.java              # 🌐 REST API endpoints
│   ├── service/
│   │   ├── CryptoService.java                 # 💼 Enhanced business logic
│   │   └── CoinMarketCapApiService.java       # 🔗 External API client
│   ├── repository/
│   │   └── CryptoCurrencyRepository.java      # 🗃️ Database operations
│   ├── model/
│   │   ├── CryptoCurrency.java                # 📊 JPA entity model
│   │   └── CoinMarketCapResponse.java         # 📄 API response DTOs
│   ├── component/
│   │   └── CryptoValidator.java               # ✅ Validation logic
│   └── config/
│       └── CryptoConfig.java                  # ⚙️ Spring configuration
├── src/main/resources/
│   ├── application.properties                 # 🔧 App configuration
│   └── static/                               # 🎨 Frontend files
│       ├── index.html                        # 📄 Main web page
│       ├── styles.css                        # 🎨 Modern CSS styles
│       └── app.js                            # ⚡ Frontend JavaScript
├── src/test/java/                            # 🧪 Test files
├── pom.xml                                   # 📦 Maven dependencies
├── .env                                      # 🔐 Environment variables
└── README.md                                 # 📖 This file
```

## 🛠️ Setup & Installation

### Prerequisites
- **Java 17+** (Cursor has Java built-in!)
- **Maven** (or use the included Maven wrapper)
- **CoinMarketCap API Key** (free tier available)
- **Supabase Account** (we've already created your project!)

### Step 1: Clone/Download the Project
This project is already set up in your current directory!

### Step 2: Configure Environment Variables
Create a `.env` file in the project root with your API keys:

```env
# CoinMarketCap API Configuration
COINMARKET_API_KEY=your_coinmarketcap_api_key_here

# Supabase Database Configuration (already configured!)
SUPABASE_URL=https://ptyepzhjmlpolfpdkhtf.supabase.co
SUPABASE_DB_PASSWORD=your_supabase_db_password_here
```

**🔑 Get Your CoinMarketCap API Key:**
1. Go to [CoinMarketCap API](https://coinmarketcap.com/api/)
2. Sign up for a free account
3. Copy your API key to the `.env` file

### Step 3: Database Setup
Your Supabase database is already configured! The schema includes:
- **cryptocurrencies table** with comprehensive crypto data fields
- **Indexes** for optimal query performance
- **Triggers** for automatic timestamp updates

### Step 4: Run the Application

**Option A: Using Cursor/VS Code (Easiest)**
1. Open `CryptoBackendApplication.java`
2. Click the "Run" button or press `F5`

**Option B: Using Maven Command Line**
```bash
# If Maven is installed:
mvn spring-boot:run

# Or use the Maven wrapper:
./mvnw spring-boot:run    # Linux/Mac
mvnw.cmd spring-boot:run  # Windows
```

## 🌐 Using the Application

### Frontend Interface
Once running, open your browser to:
**http://localhost:8080**

The web interface includes:
- 🏥 **Health Status** - API and database connectivity
- 📊 **Top Cryptocurrencies** - Live data from CoinMarketCap
- 🔍 **Search** - Find specific cryptocurrencies
- 🔄 **Sync Button** - Force refresh from CoinMarketCap API
- 💰 **Portfolio Calculator** - Calculate total value of crypto holdings

### API Endpoints
The backend provides these REST endpoints:

| Endpoint | Method | Description |
|----------|---------|-------------|
| `/api/crypto/health` | GET | Health check with DB/API status |
| `/api/crypto/popular` | GET | Top cryptocurrencies by rank |
| `/api/crypto/price/{symbol}` | GET | Get price for specific crypto |
| `/api/crypto/search?q=term` | GET | Search cryptocurrencies |
| `/api/crypto/sync` | POST | Force sync with CoinMarketCap |
| `/api/crypto/config` | GET | View service configuration |
| `/api/crypto/portfolio/value` | POST | Calculate portfolio value |
| `/api/crypto/details/{symbol}` | GET | Detailed crypto information |
| `/api/crypto/info` | GET | API information and endpoints |

### Example API Calls
```bash
# Get Bitcoin price
curl http://localhost:8080/api/crypto/price/BTC

# Search for Ethereum
curl http://localhost:8080/api/crypto/search?q=ethereum

# Force sync with CoinMarketCap
curl -X POST http://localhost:8080/api/crypto/sync
```

## 🎓 Learning Objectives Achieved

This project demonstrates:

### Spring Boot Concepts
- ✅ **@SpringBootApplication** - Auto-configuration and component scanning
- ✅ **@RestController** - REST API creation with JSON responses
- ✅ **@Service** - Business logic layer with dependency injection
- ✅ **@Repository** - Data access layer with Spring Data JPA
- ✅ **@Entity** - JPA entity mapping to database tables
- ✅ **@Configuration** - Custom Spring configuration
- ✅ **@Value** - Property injection from application.properties
- ✅ **@Autowired** - Dependency injection and IoC
- ✅ **@Transactional** - Transaction management

### Database Integration
- ✅ **Spring Data JPA** - Repository pattern and query methods
- ✅ **PostgreSQL** - Production-ready database with Supabase
- ✅ **Entity Relationships** - Proper database modeling
- ✅ **Custom Queries** - @Query annotations for complex searches
- ✅ **Database Migrations** - Schema management

### External API Integration
- ✅ **WebClient** - Non-blocking HTTP calls with Spring WebFlux
- ✅ **JSON Mapping** - Jackson for API response parsing
- ✅ **Error Handling** - Robust API error management
- ✅ **Environment Variables** - Secure API key management

### Frontend Development
- ✅ **Modern JavaScript** - ES6+ with async/await
- ✅ **Responsive Design** - Mobile-friendly CSS Grid/Flexbox
- ✅ **API Integration** - Fetch API for backend communication
- ✅ **User Experience** - Loading states, error handling, auto-refresh

## 🔧 Configuration Options

### Application Properties
Key configuration options in `application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration (Supabase)
spring.datasource.url=jdbc:postgresql://db.ptyepzhjmlpolfpdkhtf.supabase.co:5432/postgres

# Custom Application Settings
app.crypto.max-results=10          # Number of cryptos to fetch
app.crypto.cache-enabled=true      # Enable database caching
app.crypto.sync-interval=300000    # Auto-sync interval (5 minutes)
```

## 🐛 Troubleshooting

### Common Issues

**🔑 "API key not configured"**
- Add your CoinMarketCap API key to `.env` file
- Restart the application after adding the key

**🗃️ Database connection errors**
- Check your Supabase database password in `.env`
- Verify the database URL in `application.properties`

**🌐 Frontend not loading**
- Make sure you're accessing `http://localhost:8080` (not 8081)
- Check browser console for JavaScript errors

**📊 No cryptocurrency data showing**
- Click "Sync with API" button to fetch initial data
- Check health status indicators at the top

## 🚀 Next Steps & Enhancements

Ready to take this further? Consider adding:

1. **🔐 Authentication & User Accounts**
   - Spring Security implementation
   - User-specific portfolios and watchlists

2. **📱 Mobile App**
   - React Native or Flutter companion app

3. **📈 Advanced Analytics**
   - Price charts with historical data
   - Technical indicators and alerts

4. **🔔 Real-time Updates**
   - WebSocket integration for live price updates
   - Push notifications for price alerts

5. **☁️ Cloud Deployment**
   - Docker containerization
   - Deploy to AWS, Google Cloud, or Heroku

## 📚 Technologies Used

- **Backend:** Spring Boot 3.2.0, Java 17, Maven
- **Database:** PostgreSQL (Supabase), Spring Data JPA, Hibernate
- **External APIs:** CoinMarketCap Pro API
- **Frontend:** HTML5, CSS3, Modern JavaScript (ES6+)
- **Styling:** CSS Grid, Flexbox, Custom animations
- **Tools:** VS Code/Cursor, Maven Wrapper, Git

## 🤝 Contributing

This is a learning project! Feel free to:
- Add new features and improvements
- Fix bugs or enhance error handling
- Improve the UI/UX design
- Add more comprehensive tests
- Enhance documentation

## 📄 License

This project is for educational purposes. Please respect the terms of service for:
- [CoinMarketCap API](https://coinmarketcap.com/api/terms)
- [Supabase](https://supabase.com/docs/guides/platform/terms)

---

**🎉 Congratulations!** You've built a complete full-stack cryptocurrency application with real-time data, database persistence, and a modern web interface. This project showcases professional-level Spring Boot development with external API integration and database management! 