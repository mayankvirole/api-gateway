$filesToRefactor = @(
    "api-gateway/pom.xml",
    "discovery-server/pom.xml",
    "notification-service/pom.xml",
    "notification-service/src/main/java/com/ecommerce/notificationservice/entity/NotificationLog.java",
    "notification-service/src/main/java/com/ecommerce/notificationservice/event/PaymentProcessedEvent.java",
    "notification-service/src/main/java/com/ecommerce/notificationservice/service/NotificationService.java",
    "order-service/pom.xml",
    "order-service/src/main/java/com/ecommerce/orderservice/controller/OrderController.java",
    "order-service/src/main/java/com/ecommerce/orderservice/entity/Order.java",
    "order-service/src/main/java/com/ecommerce/orderservice/entity/OrderLineItem.java",
    "order-service/src/main/java/com/ecommerce/orderservice/event/OrderPlacedEvent.java",
    "order-service/src/main/java/com/ecommerce/orderservice/service/OrderService.java",
    "payment-service/pom.xml",
    "payment-service/src/main/java/com/ecommerce/paymentservice/entity/Payment.java",
    "payment-service/src/main/java/com/ecommerce/paymentservice/event/OrderPlacedEvent.java",
    "payment-service/src/main/java/com/ecommerce/paymentservice/event/PaymentProcessedEvent.java",
    "payment-service/src/main/java/com/ecommerce/paymentservice/service/PaymentService.java",
    "pom.xml",
    "product-service/pom.xml",
    "product-service/src/main/java/com/ecommerce/productservice/controller/ProductController.java",
    "product-service/src/main/java/com/ecommerce/productservice/entity/Product.java",
    "product-service/src/main/java/com/ecommerce/productservice/service/ProductService.java",
    "user-service/pom.xml",
    "user-service/src/main/java/com/ecommerce/userservice/controller/UserController.java",
    "user-service/src/main/java/com/ecommerce/userservice/entity/User.java",
    "user-service/src/main/java/com/ecommerce/userservice/service/UserService.java",
    "user-service/src/test/java/com/ecommerce/userservice/service/UserServiceTest.java"
)

# 1. Update pom.xml for java.version 25 and remove lombok
$parentPomPath = "pom.xml"
$parentPom = Get-Content -Path $parentPomPath -Raw
$parentPom = $parentPom -replace "<java.version>17</java.version>", "<java.version>25</java.version>"
$parentPom = $parentPom -replace "<maven.compiler.source>17</maven.compiler.source>", "<maven.compiler.source>25</maven.compiler.source>"
$parentPom = $parentPom -replace "<maven.compiler.target>17</maven.compiler.target>", "<maven.compiler.target>25</maven.compiler.target>"

# Removing lombok block from parent pom
$parentPom = $parentPom -replace "(?s)<!-- Lombok -->.*?</dependency>", ""
$parentPom = $parentPom -replace "(?s)<annotationProcessorPaths>.*?</annotationProcessorPaths>", ""

Set-Content -Path $parentPomPath -Value $parentPom

# Remove lombok from child poms
foreach ($f in $filesToRefactor) {
    if ($f.EndsWith("pom.xml") -and $f -ne "pom.xml") {
        $content = Get-Content -Path $f -Raw
        $content = $content -replace "(?s)<dependency>\s*<groupId>org.projectlombok</groupId>.*?<optional>true</optional>\s*</dependency>", ""
        Set-Content -Path $f -Value $content
    }
}

# 2. Refactor Java Files

# --- NotificationService ---
$f = "notification-service/src/main/java/com/ecommerce/notificationservice/entity/NotificationLog.java"
$content = @"
package com.ecommerce.notificationservice.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private String message;
    private LocalDateTime sentAt;

    public NotificationLog() {}
    public NotificationLog(Long orderId, String message, LocalDateTime sentAt) {
        this.orderId = orderId; this.message = message; this.sentAt = sentAt;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
"@
Set-Content -Path $f -Value $content

$f = "notification-service/src/main/java/com/ecommerce/notificationservice/event/PaymentProcessedEvent.java"
$content = @"
package com.ecommerce.notificationservice.event;

public class PaymentProcessedEvent {
    private Long orderId;
    private Long paymentId;
    private String status;

    public PaymentProcessedEvent() {}
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
"@
Set-Content -Path $f -Value $content

$f = "notification-service/src/main/java/com/ecommerce/notificationservice/service/NotificationService.java"
$content = @"
package com.ecommerce.notificationservice.service;
import com.ecommerce.notificationservice.entity.NotificationLog;
import com.ecommerce.notificationservice.event.PaymentProcessedEvent;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(topics = "payment-processed-topic", groupId = "notification-group")
    @Transactional
    public void consumePaymentEvent(PaymentProcessedEvent event) {
        log.info("Received Payment Event for Order ID: {} with Status: {}", event.getOrderId(), event.getStatus());
        
        NotificationLog notification = new NotificationLog(event.getOrderId(), "Your order has been placed and payment is " + event.getStatus(), LocalDateTime.now());
        notificationRepository.save(notification);
        
        log.info("Email simulated and recorded in notification_logs database.");
    }
}
"@
Set-Content -Path $f -Value $content

# --- PaymentService ---
$f = "payment-service/src/main/java/com/ecommerce/paymentservice/entity/Payment.java"
$content = @"
package com.ecommerce.paymentservice.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime processedAt;

    public Payment() {}
    public Payment(Long orderId, BigDecimal amount, String status, LocalDateTime processedAt) {
        this.orderId = orderId; this.amount = amount; this.status = status; this.processedAt = processedAt;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
"@
Set-Content -Path $f -Value $content

$f = "payment-service/src/main/java/com/ecommerce/paymentservice/event/OrderPlacedEvent.java"
$content = @"
package com.ecommerce.paymentservice.event;
import java.math.BigDecimal;

public class OrderPlacedEvent {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String status;

    public OrderPlacedEvent() {}
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
"@
Set-Content -Path $f -Value $content

$f = "payment-service/src/main/java/com/ecommerce/paymentservice/event/PaymentProcessedEvent.java"
$content = @"
package com.ecommerce.paymentservice.event;

public class PaymentProcessedEvent {
    private Long orderId;
    private Long paymentId;
    private String status;

    public PaymentProcessedEvent() {}
    public PaymentProcessedEvent(Long orderId, Long paymentId, String status) {
        this.orderId = orderId; this.paymentId = paymentId; this.status = status;
    }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
"@
Set-Content -Path $f -Value $content

$f = "payment-service/src/main/java/com/ecommerce/paymentservice/service/PaymentService.java"
$content = @"
package com.ecommerce.paymentservice.service;
import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.event.OrderPlacedEvent;
import com.ecommerce.paymentservice.event.PaymentProcessedEvent;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentService(PaymentRepository paymentRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order-placed-topic", groupId = "payment-group")
    @Transactional
    public void consumeOrderEvent(OrderPlacedEvent event) {
        Payment payment = new Payment(event.getOrderId(), event.getAmount(), "SUCCESS", LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        PaymentProcessedEvent outEvent = new PaymentProcessedEvent(saved.getOrderId(), saved.getId(), "SUCCESS");
        kafkaTemplate.send("payment-processed-topic", outEvent);
    }
}
"@
Set-Content -Path $f -Value $content

# --- OrderService ---
$f = "order-service/src/main/java/com/ecommerce/orderservice/controller/OrderController.java"
$content = Get-Content -Path $f -Raw
$content = $content -replace "import lombok.RequiredArgsConstructor;`r`n", ""
$content = $content -replace "@RequiredArgsConstructor`r`n", ""
$content = $content -replace "private final OrderService orderService;", "private final OrderService orderService;`r`n`r`n    public OrderController(OrderService orderService) {`r`n        this.orderService = orderService;`r`n    }"
Set-Content -Path $f -Value $content

$f = "order-service/src/main/java/com/ecommerce/orderservice/entity/Order.java"
$content = @"
package com.ecommerce.orderservice.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
    private List<OrderLineItem> orderLineItems;

    private String orderStatus;
    private BigDecimal totalPrice;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Order() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public List<OrderLineItem> getOrderLineItems() { return orderLineItems; }
    public void setOrderLineItems(List<OrderLineItem> orderLineItems) { this.orderLineItems = orderLineItems; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
"@
Set-Content -Path $f -Value $content

$f = "order-service/src/main/java/com/ecommerce/orderservice/entity/OrderLineItem.java"
$content = @"
package com.ecommerce.orderservice.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_line_items")
public class OrderLineItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private BigDecimal price;
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public OrderLineItem() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
}
"@
Set-Content -Path $f -Value $content

$f = "order-service/src/main/java/com/ecommerce/orderservice/event/OrderPlacedEvent.java"
$content = @"
package com.ecommerce.orderservice.event;
import java.math.BigDecimal;

public class OrderPlacedEvent {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String status;

    public OrderPlacedEvent() {}
    public OrderPlacedEvent(Long orderId, Long userId, BigDecimal amount, String status) {
        this.orderId = orderId; this.userId = userId; this.amount = amount; this.status = status;
    }
    public Long getOrderId() { return orderId; }
    public Long getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
}
"@
Set-Content -Path $f -Value $content

$f = "order-service/src/main/java/com/ecommerce/orderservice/service/OrderService.java"
$content = Get-Content -Path $f -Raw
$content = $content -replace "import lombok.RequiredArgsConstructor;`r`n", ""
$content = $content -replace "@RequiredArgsConstructor`r`n", ""
$content = $content -replace "private final KafkaTemplate<String, Object> kafkaTemplate;", "private final KafkaTemplate<String, Object> kafkaTemplate;`r`n`r`n    public OrderService(OrderRepository orderRepository, KafkaTemplate<String, Object> kafkaTemplate) {`r`n        this.orderRepository = orderRepository;`r`n        this.kafkaTemplate = kafkaTemplate;`r`n    }"
Set-Content -Path $f -Value $content


# --- ProductService ---
$f = "product-service/src/main/java/com/ecommerce/productservice/controller/ProductController.java"
$content = Get-Content -Path $f -Raw
$content = $content -replace "import lombok.RequiredArgsConstructor;`r`n", ""
$content = $content -replace "@RequiredArgsConstructor`r`n", ""
$content = $content -replace "private final ProductService productService;", "private final ProductService productService;`r`n`r`n    public ProductController(ProductService productService) {`r`n        this.productService = productService;`r`n    }"
Set-Content -Path $f -Value $content

$f = "product-service/src/main/java/com/ecommerce/productservice/entity/Product.java"
$content = @"
package com.ecommerce.productservice.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String description;
    @Column(nullable = false)
    private BigDecimal price;
    @Column(nullable = false)
    private Integer inventory;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Product() {}
    public Product(String name, String description, BigDecimal price, Integer inventory) {
        this.name = name; this.description = description; this.price = price; this.inventory = inventory;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getInventory() { return inventory; }
    public void setInventory(Integer inventory) { this.inventory = inventory; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
"@
Set-Content -Path $f -Value $content

$f = "product-service/src/main/java/com/ecommerce/productservice/service/ProductService.java"
$content = Get-Content -Path $f -Raw
$content = $content -replace "import lombok.RequiredArgsConstructor;`r`n", ""
$content = $content -replace "@RequiredArgsConstructor`r`n", ""
$content = $content -replace "private final ProductRepository productRepository;", "private final ProductRepository productRepository;`r`n`r`n    public ProductService(ProductRepository productRepository) {`r`n        this.productRepository = productRepository;`r`n    }"
$content = $content -replace "Product.builder\(\).*\.build\(\);", "new Product(request.name(), request.description(), request.price(), request.inventory());"
Set-Content -Path $f -Value $content


# --- UserService ---
$f = "user-service/src/main/java/com/ecommerce/userservice/controller/UserController.java"
$content = Get-Content -Path $f -Raw
$content = $content -replace "import lombok.RequiredArgsConstructor;`r`n", ""
$content = $content -replace "@RequiredArgsConstructor`r`n", ""
$content = $content -replace "private final UserService userService;", "private final UserService userService;`r`n`r`n    public UserController(UserService userService) {`r`n        this.userService = userService;`r`n    }"
Set-Content -Path $f -Value $content

$f = "user-service/src/main/java/com/ecommerce/userservice/entity/User.java"
$content = @"
package com.ecommerce.userservice.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String role;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public User() {}
    public User(String email, String password, String name, String role) {
        this.email = email; this.password = password; this.name = name; this.role = role;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
"@
Set-Content -Path $f -Value $content

$f = "user-service/src/main/java/com/ecommerce/userservice/service/UserService.java"
$content = Get-Content -Path $f -Raw
$content = $content -replace "import lombok.RequiredArgsConstructor;`r`n", ""
$content = $content -replace "@RequiredArgsConstructor`r`n", ""
$content = $content -replace "private final JwtUtil jwtUtil;", "private final JwtUtil jwtUtil;`r`n`r`n    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {`r`n        this.userRepository = userRepository;`r`n        this.passwordEncoder = passwordEncoder;`r`n        this.jwtUtil = jwtUtil;`r`n    }"
$content = $content -replace "User.builder\(\).*\.build\(\);", "new User(request.email(), passwordEncoder.encode(request.password()), request.name(), `"USER`");"
Set-Content -Path $f -Value $content

Write-Host "Lombok removed and JDK upgraded to 25."
