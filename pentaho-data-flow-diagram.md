# Pentaho Data Flow Diagram

## Complete Data Flow Architecture

```mermaid
graph TB
    %% Client Layer
    subgraph "Client Layer"
        PDI[PDI Client/Spoon]
        SERVER[Pentaho Server]
        JDBC[JDBC Client]
        BI[BI Tools<br/>Tableau/PowerBI]
    end

    %% API Gateway Layer
    subgraph "API Gateway"
        REST[REST API]
        WS[WebSocket Endpoint]
        SQLAPI[SQL/JDBC Interface]
    end

    %% Orchestration Layer
    subgraph "Orchestration Layer"
        DAEMON[PDI WebSocket Daemon<br/>DaemonMain.java]
        EXEC_MGR[Execution Manager]
        ORCH_ENGINE[Orchestrator Engine]
    end

    %% Processing Layer
    subgraph "Processing Layer"
        DS_SERVER[Data Service Server]
        DS_EXEC[DataServiceExecutor]
        
        subgraph "Microservices"
            FILE_SVC[File Reader Service]
            MERGE_SVC[Merge Join Service]
            SORT_SVC[Sort Rows Service]
            TABLE_SVC[Table Output Service]
        end
    end

    %% Execution Engines
    subgraph "Execution Engines"
        KETTLE[Kettle Engine<br/>Traditional PDI]
        SPARK[Spark Engine<br/>Distributed Processing]
        FUTURE[Future Engines<br/>Flink, Mesos]
    end

    %% Data Storage & Messaging
    subgraph "Infrastructure"
        RABBITMQ[RabbitMQ<br/>Message Queue]
        POSTGRES[PostgreSQL<br/>Metadata Store]
        HAZELCAST[Hazelcast<br/>Distributed Cache]
        CACHE[Session Cache]
    end

    %% External Data Sources
    subgraph "Data Sources"
        FILES[File Systems]
        DATABASES[Databases]
        APIS[External APIs]
        STREAMS[Data Streams]
    end

    %% Data Flow Connections
    PDI --> REST
    SERVER --> REST
    JDBC --> SQLAPI
    BI --> SQLAPI

    REST --> DAEMON
    WS --> DAEMON
    SQLAPI --> DS_SERVER

    DAEMON --> EXEC_MGR
    EXEC_MGR --> ORCH_ENGINE
    DS_SERVER --> DS_EXEC

    ORCH_ENGINE --> RABBITMQ
    RABBITMQ --> FILE_SVC
    RABBITMQ --> MERGE_SVC
    RABBITMQ --> SORT_SVC
    RABBITMQ --> TABLE_SVC

    DAEMON --> KETTLE
    DAEMON --> SPARK
    DAEMON --> FUTURE

    DS_EXEC --> KETTLE
    DS_EXEC --> SPARK

    FILE_SVC --> HAZELCAST
    MERGE_SVC --> HAZELCAST
    SORT_SVC --> HAZELCAST
    TABLE_SVC --> HAZELCAST

    ORCH_ENGINE --> POSTGRES
    DAEMON --> CACHE
    DS_SERVER --> CACHE

    KETTLE --> FILES
    KETTLE --> DATABASES
    SPARK --> FILES
    SPARK --> DATABASES
    SPARK --> STREAMS

    FILE_SVC --> FILES
    TABLE_SVC --> DATABASES

    %% Response Flow (dotted lines)
    KETTLE -.-> DAEMON
    SPARK -.-> DAEMON
    DS_EXEC -.-> SQLAPI
    TABLE_SVC -.-> RABBITMQ
    SORT_SVC -.-> RABBITMQ
    MERGE_SVC -.-> RABBITMQ
    FILE_SVC -.-> RABBITMQ

    DAEMON -.-> WS
    WS -.-> PDI
    WS -.-> SERVER
    SQLAPI -.-> JDBC
    SQLAPI -.-> BI

    %% Styling
    classDef client fill:#e1f5fe
    classDef gateway fill:#f3e5f5
    classDef orchestration fill:#fff3e0
    classDef processing fill:#e8f5e8
    classDef execution fill:#fce4ec
    classDef infrastructure fill:#f1f8e9
    classDef datasource fill:#fff8e1

    class PDI,SERVER,JDBC,BI client
    class REST,WS,SQLAPI gateway
    class DAEMON,EXEC_MGR,ORCH_ENGINE orchestration
    class DS_SERVER,DS_EXEC,FILE_SVC,MERGE_SVC,SORT_SVC,TABLE_SVC processing
    class KETTLE,SPARK,FUTURE execution
    class RABBITMQ,POSTGRES,HAZELCAST,CACHE infrastructure
    class FILES,DATABASES,APIS,STREAMS datasource
```

## Detailed Data Flow Scenarios

### Scenario 1: Traditional PDI Transformation
```mermaid
sequenceDiagram
    participant Client as PDI Client
    participant Daemon as WebSocket Daemon
    participant Kettle as Kettle Engine
    participant Data as Data Sources

    Client->>Daemon: Submit Transformation (.ktr)
    Daemon->>Daemon: Parse Transformation
    Daemon->>Kettle: Execute Transformation
    Kettle->>Data: Read Data
    Data-->>Kettle: Data Rows
    Kettle->>Kettle: Transform Data
    Kettle-->>Daemon: Status Updates
    Daemon-->>Client: Real-time Progress
    Kettle-->>Daemon: Final Results
    Daemon-->>Client: Completion Status
```

### Scenario 2: Adaptive Spark Execution
```mermaid
sequenceDiagram
    participant Client as Pentaho Server
    participant Daemon as WebSocket Daemon
    participant ExecMgr as Execution Manager
    participant Spark as Spark Engine
    participant Cluster as Spark Cluster

    Client->>Daemon: Submit for Spark Execution
    Daemon->>ExecMgr: Convert Kettle → Spark
    ExecMgr->>ExecMgr: Generate Spark Job
    ExecMgr->>Spark: Submit Spark Application
    Spark->>Cluster: Distribute Processing
    Cluster-->>Spark: Processed Data
    Spark-->>ExecMgr: Results & Metrics
    ExecMgr-->>Daemon: Execution Status
    Daemon-->>Client: Real-time Updates
```

### Scenario 3: Microservices Data Processing
```mermaid
sequenceDiagram
    participant Client as API Client
    participant Orchestrator as Orchestrator Engine
    participant Queue as RabbitMQ
    participant FileService as File Reader Service
    participant MergeService as Merge Join Service
    participant TableService as Table Output Service
    participant Cache as Hazelcast

    Client->>Orchestrator: Submit Transformation
    Orchestrator->>Queue: Publish Step Messages
    Queue->>FileService: File Reading Request
    FileService->>Cache: Store Data Chunks
    FileService->>Queue: Completion Message
    
    Queue->>MergeService: Merge Request
    MergeService->>Cache: Retrieve Data
    MergeService->>Cache: Store Merged Data
    MergeService->>Queue: Completion Message
    
    Queue->>TableService: Output Request
    TableService->>Cache: Retrieve Final Data
    TableService->>TableService: Write to Database
    TableService-->>Orchestrator: Final Status
```

### Scenario 4: SQL Data Service Query
```mermaid
sequenceDiagram
    participant BI as BI Tool
    participant JDBC as JDBC Interface
    participant DataService as Data Service Server
    participant Executor as DataServiceExecutor
    participant ServiceTrans as Service Transformation
    participant GenTrans as Generated Transformation

    BI->>JDBC: SQL Query
    JDBC->>DataService: Parse SQL
    DataService->>Executor: Create Execution Plan
    Executor->>Executor: Generate SQL Transformation
    Executor->>ServiceTrans: Start Service Trans
    Executor->>GenTrans: Start Generated Trans
    
    ServiceTrans-->>GenTrans: Data Stream
    GenTrans->>GenTrans: Apply SQL Operations
    GenTrans-->>Executor: Result Rows
    Executor-->>DataService: Formatted Results
    DataService-->>JDBC: SQL Result Set
    JDBC-->>BI: Query Response
```

## Key Data Flow Characteristics

### 1. **Multi-Protocol Support**
- WebSocket for real-time communication
- REST APIs for standard HTTP requests
- JDBC for SQL-based access
- Message queues for asynchronous processing

### 2. **Adaptive Execution**
- Dynamic engine selection (Kettle vs Spark)
- Automatic workload distribution
- Resource-aware scheduling

### 3. **Distributed Processing**
- Microservices architecture
- Horizontal scalability
- Fault tolerance and recovery

### 4. **Real-time Capabilities**
- Live progress updates
- Streaming data processing
- Event-driven architecture

### 5. **Caching Strategy**
- Session state management
- Data chunk caching
- Metadata caching
- Performance optimization
