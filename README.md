# Spring Batch ETL Pipeline

A demonstration of Spring Batch for chunk-based data processing, featuring CSV-to-database ETL with job listeners and transaction management.

## Problem Statement

Enterprise systems frequently need to:

- Process large datasets without overwhelming memory
- Maintain transactional integrity across batch operations
- Provide visibility into job execution status
- Support restartability after failures

This project demonstrates the core patterns of batch processing using Spring Batch's proven infrastructure.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                          Job                                     │
│  importUserJob                                                   │
│  • RunIdIncrementer for unique executions                        │
│  • JobCompletionNotificationListener for status callbacks        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                          Step                                    │
│  step1 (chunk-oriented)                                          │
│  • Chunk size: 10 records                                        │
│  • Transaction per chunk                                         │
└─────────────────────────────────────────────────────────────────┘
           │                  │                    │
           ▼                  ▼                    ▼
┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
│   ItemReader     │ │  ItemProcessor   │ │   ItemWriter     │
│                  │ │                  │ │                  │
│ FlatFileReader   │ │ CustomerProcessor│ │ JdbcBatchWriter  │
│ • CSV parsing    │ │ • Transformation │ │ • Bulk INSERT    │
│ • Bean mapping   │ │ • Validation     │ │ • Batch commits  │
└──────────────────┘ └──────────────────┘ └──────────────────┘
```

## Data Flow

```
CSV File                    Processing                    Database
┌─────────┐                ┌──────────┐                ┌──────────┐
│ id      │    Read 10     │ Transform│    Write 10   │ customers│
│ fname   │ ──────────────>│ Validate │ ─────────────>│ table    │
│ lname   │    records     │ Enrich   │   (1 TX)      │          │
│ email   │                │          │               │          │
└─────────┘                └──────────┘                └──────────┘
    │                                                       │
    └───────────────────── Repeat ──────────────────────────┘
```

## Tech Stack

| Technology | Justification |
|------------|---------------|
| Spring Batch | De-facto standard for Java batch processing |
| Spring Boot | Auto-configuration, embedded container |
| H2 / JDBC | Lightweight persistence for demo |

## Key Design Decisions

### 1. Chunk-Oriented Processing

Items are read and processed one at a time but written in chunks. This balances memory efficiency with database performance:

```java
.<Customer, Customer>chunk(10)  // Process 10 items per transaction
    .reader(reader())
    .processor(processor())
    .writer(writer())
```

**Why 10?** Small enough to limit memory footprint, large enough to amortize transaction overhead.

### 2. Job Completion Listener

Decouples execution monitoring from job logic. Enables notifications, metrics, or cleanup actions:

```java
@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {
    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            // Log success, send notification, trigger downstream jobs
        }
    }
}
```

### 3. RunIdIncrementer for Idempotency

Each job execution gets a unique ID, allowing the same job to run multiple times with different parameters:

```java
.incrementer(new RunIdIncrementer())
```

### 4. BeanPropertyItemSqlParameterSourceProvider

Maps POJO properties directly to SQL parameters without manual extraction:

```java
.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
.sql("INSERT INTO customers (...) VALUES (:id, :firstName, :lastName, :email)")
```

## Trade-offs

| Decision | Benefit | Cost |
|----------|---------|------|
| Single step | Simple flow | Complex pipelines need step chaining |
| Synchronous | Predictable ordering | Parallel chunks would improve throughput |
| In-memory H2 | Zero setup | Data not persisted between runs |

## What This Demonstrates

- **Batch processing fundamentals**: Chunk-oriented processing with configurable commit intervals
- **Transaction management**: Automatic rollback on chunk failure, no partial writes
- **Separation of concerns**: Reader/Processor/Writer pattern for testable, swappable components
- **Observability**: Job listeners for monitoring and integration
- **Spring ecosystem**: Integration with Spring Boot auto-configuration

## Quick Start

```bash
./mvnw spring-boot:run
```

The job runs automatically on startup, processing `customer-data.csv` into the embedded database.

## Extending This Pattern

For production workloads, consider:

- **Parallel steps**: Process independent data streams concurrently
- **Partitioning**: Split large files across multiple threads
- **Skip/Retry policies**: Handle transient failures gracefully
- **Remote chunking**: Distribute processing across nodes

---

*Foundation for enterprise batch processing with Spring Batch.*
