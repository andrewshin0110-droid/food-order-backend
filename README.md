# Food Order System | 外賣點餐核心系統（個人後端實戰練習）

[![Java](https://img.shields.io/badge/Java-8%2B-orange.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.x-green.svg)]()
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)]()
[![Redis](https://img.shields.io/badge/Redis-6.x-red.svg)]()

## 📌 專案緣起與個人背景
本專案是我在退伍前後，為了將大學所學的資料庫理論與業界後端架構接軌，所進行的個人自主進階實戰專案。

在學校經歷過基礎網頁作業後，我深知業界系統不會只有單純的 CRUD（增刪查改）。因此，我參考了成熟的外送平台業務閉環，一邊理解商務邏輯、一邊手敲程式碼並獨自完成所有 Bug 的除錯。這個專案幫助我跳脫了學生的開發思維，開始學習如何處理快取應用、即時通訊與系統分層架構。

---

## 🛠️ 開發環境與技術棧 (Tech Stack)

為了模擬業界開發場景，本專案使用了以下技術與工具進行建構：
- **後端核心框架**：Java 8 / Spring Boot (2.7.x) / Spring MVC
- **專案建置與管理**：Maven
- **資料儲存與 ORM**：MySQL 8.0 / MyBatis
- **快取與記憶體資料庫**：Redis (Cache Aside Pattern 練習)
- **安全與驗證機制**：JWT (JSON Web Token) / ThreadLocal / Spring Interceptor (攔截器)
- **即時通訊協定**：WebSocket (基礎推播)
- **第三方工具與套件**：Apache POI (Excel 報表產出) / Swagger & Knife4j (API 文件自動化)
- **開發工具**：IntelliJ IDEA / Git / Apifox / DataGrip

---

## 💡 開發過程與核心收穫

### 1. 練習業界標準架構分層 (Spring Boot 三層架構)
- **實作方式**：嚴格遵循 Controller、Service、Mapper 的分層規範，並使用 Maven 進行相依性管理。
- **核心收穫**：以前寫學校作業程式碼常常缺乏統整，這次真正體會到「高內聚、低耦合」的好處。配合 Swagger (Knife4j) 產生 API 文件，也讓我更清楚如何設計出格式清晰、符合前後端分離規範的 RESTful API。

### 2. 初次導入 Redis 快取處理效能問題 (Cache Aside 模式)
- **實作方式**：考慮到外送平台的「菜品和套餐分類」是極高頻率被查詢、但很少變動的資料，我將其寫入 Redis 記憶體快取。
- **核心收穫**：理解到減少資料庫（MySQL）I/O 負載的重要性。同時，我也實作了快取一致性的基礎概念：當商家端修改或刪除菜品時，後端會主動清除對應的 Redis 快取，確保使用者端不會看到過期的選單資訊。
- *💡 筆記*：本設計主要用於本地開發環境下練習快取基本架構。我了解在商用生產環境（Production）的高併發場景下，還需進一步考量快取擊穿、雪崩以及利用分布式鎖處理並發雙寫的一致性問題，這是我未來在實務中渴望深入挑戰的領域。

### 3. 安全機制練習：雙端 JWT 身份驗證與 ThreadLocal 應用
- **實作方式**：實作自定義的 Spring 攔截器 (Interceptor)，統一攔截非匿名請求並解析 JWT Token。
- **核心收穫**：學會了如何區分管理員與一般用戶的身份。透過將解析出的用戶資訊存入 ThreadLocal，確保在同一個請求執行緒中的資料安全，也簡化了後續 Service 層獲取當前登入者資訊的步驟。
- *💡 筆記*：為防範 Tomcat 線程池機制下的執行緒重用風險（避免記憶體流失 Memory Leak 與資料交叉污染），本專案特別在 Interceptor 的 `afterCompletion` 階段，**強制呼叫 `.remove()` 清除 ThreadLocal 數據**，確保執行緒安全釋放。

### 4. 整合 WebSocket 實作基礎即時通知
- **實作方式**：不採用前端定時輪詢（Polling）這種浪費資源的方式，而是整合 WebSocket 協定，實作基礎的雙向推播應用。
- **核心收穫**：當使用者模擬支付成功後，後端能即時將訊息推播給商家端，觸發語音與彈窗提醒，這讓我完整體驗了異步即時通訊在實際商業場景下的應用。

### 5. 實作完整的點餐與訂單狀態機流程 (Order Lifecycle)
- **實作方式**：
  - **購物車管理**：設計 `shopping_cart` 邏輯，處理動態加入、修改數量及口味規格關聯。
  - **提交訂單與明細分離**：下單時利用單一事務同時寫入 `orders` (主表) 與 `order_detail` (明細表)，並清空購物車。
  - **狀態演變控制**：實作狀態機邏輯，嚴格控制訂單從 *待付款 ➔ 待接單 ➔ 派送中 ➔ 已完成* 的商務狀態切換。
- **核心收穫**：這讓我真正理解了電商/點餐系統核心的業務閉環。我學到了在處理多表操作時，必須使用 `@Transactional` 事務管理，避免發生「主表建立成功，明細卻漏掉」的嚴重系統 Bug，確保交易邊界內的資料強一致性。

### 6. 實作動態報表產出 (Apache POI)
- **實作方式**：在後台營運統計模組中，使用 MySQL 聚合函數計算近 30 天的營業額與熱銷排行，並利用 Apache POI 將數據填入 Excel 範本提供下載。
- **核心收穫**：掌握了將結構化數據轉化為實體檔案並提供下載的商務功能實作。

---

## 📐 系統目錄結構 (Architecture)

```text
├── config/          # 系統配置 (Redis, WebMvcConfigurer)
├── controller/      # 控制層 (RESTful API 路由與請求參數校驗)
├── service/         # 業務邏輯層 (核心業務邏輯與 @Transactional 事務控制)
│   └── impl/
├── mapper/          # 資料持久層 (MyBatis SQL 對接)
├── entity/          # 實體類 (POJO, DTO, VO)
└── interceptor/     # 攔截器 (JWT 鑑權與 ThreadLocal 注入/清除)
