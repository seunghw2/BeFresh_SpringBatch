# 🥦 Be Fresh - Spring Batch 리팩터링 프로젝트

**[기존 Be Fresh 프로젝트](https://github.com/seunghw2/BeFresh)의 Spring Batch를 추가 학습 및 리팩터링하기 위한 프로젝트**


- 👪 기관: 삼성 청년 SW 아카데미
- 📆 개발 기간: 2024.04.08 ~ 2024.05.20
- 📆 리팩터링 기간: 2024.07 (약 3주)

---

# **💡 리팩터링 내용**

## 1. 병렬 처리


### 이유

FCM 서버로 API를 호출하고 응답을 기다릴 때 동기적으로 과정이 진행되기 때문에, 너무 긴 대기시간이 소요되었다.

이를 해결하기 위하여, `멀티스레드 스텝`으로 병렬 처리를 구현했다.

### 고려 사항

**[멀티스레드 스텝 vs 파티셔닝]**

병렬 처리 과정에서 멀티스레드 스텝 방식과 파티셔닝 방식 사이에서 고민을 했다.

파티셔닝 방식을 채택하지 않은 이유는 해당 알림 데이터의 자연스러운 분할이 어렵다고 보았기 때문이다.

파티셔닝 방식의 경우, 데이터셋이 특정 기준(ex. 날짜, 지역, 고객 ID 등)에 따라 자연스럽게 분할될 수 있을 때 효과적이라고 한다.

하지만 알림 데이터셋의 경우, 특정 기준에 따라 분할되지는 않는다고 판단하여 멀티스레드 스텝 방식을 채택했다.

### 코드

```java
@Bean
public TaskExecutor simpleTaskExecutor() {
    SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
    return taskExecutor;
}

@Bean
public TaskExecutor threadPoolTaskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(4);
    taskExecutor.setMaxPoolSize(10);
    taskExecutor.setQueueCapacity(30);
    taskExecutor.initialize();
    return taskExecutor;
}
```

**[`CorePoolSize`, `MaxPoolSize`, `QueueCapacity`]**

- 스레드 수가 `corePoolSize`보다 적으면, 새로운 작업을 실행하기 위해 새 스레드를 생성합니다.
- 스레드 수가 `corePoolSize`와 같거나 많으면, 작업을 대기열(`Queue`)에 넣습니다.
- 이때 대기열의 크기가 `QueueCapacity`
- 대기열이 가득 차고, 스레드 수가 `maxPoolSize`보다 적다면, 작업을 실행하기 위해 새 스레드를 생성한다.
- 대기열이 가득 차고, 스레드 수가 `maxPoolSize`와 같거나 많으면, 작업을 거부합니다.

**[`CorePoolSize`, `ThrottleLimit`]**

- `CorePoolSize`는 `threadPoolTaskExecutor`가 `N`개의 스레드로 시작할 것임을 의미합니다.
- `ThrottleLimit` `T`는, 스레드 풀에 사용할 수 있는 스레드 수와 상관없이, 특정 작업(tasklet)을 처리하는 데 `T`개의 스레드만 사용하라는 의미입니다.
- 따라서, 코어 풀 사이즈가 8인 스레드 풀을 가지고 두 개의 작업(tasklet)을 각각 제한값 4로 설정하면 스레드 풀을 모두 활용하게 됩니다.
- 하지만 제한값이 4인 작업(tasklet) 하나만 있을 경우 스레드 풀의 절반만 활용하게 됩니다.

### 성능 비교

- FCM Network I/O 대기 시간 : 평균 약 90ms
- Chunk Size : 1000

| 데이터 개수 | **1000개** | **5000개** | 25000개 |
| --- | --- | --- | --- |
| 기본 | 93941 |  |  |
| `SimpleAsyncTaskExecutor` (throttle limit : 4) | 24136 | 139434 | 507692 (8m27s699ms) |
| `ThreadPoolTaskExecutor**` | 26409 | 134571 |  |

---

## 2. Bulk Insert

### 적용한 이유

기존 구현했던 `notificationRepository.saveAll(notifications)` 방식은 내부적으로 N개의 Entity들을 하나씩 순회하며 `save()` 하는 것과 동일하다는 것을 깨달았다.

즉, 1000개의 `Notification` Entity가 있다면, 한 트랜잭션 안에서 100번 `save()` 하는 것과 동일하고, 이는 내가 예상했던 Bulk Insert가 아니었다.

### 고려 사항

`JPAItemWriter` 에서 `Auto Increment` ID 방식으로는 Batch Insert를 사용할 수 없었다.

- 이유
    
    Hibernate는 기본적으로 데이터를 하나씩 저장하면서, 그 때마다 자동 생성된 ID를 추적한다.
    
    즉, 데이터가 DB에 저장되고 나서야 그 ID를 알 수 있기 때문에, Hibernate는 일단 데이터를 하나씩 저장하고, 그 때마다 ID를 확인해서 내부적으로 관리한다.
    
    하지만, Batch Insert는 한꺼번에 데이터를 넣어버리기 때문에 그 ID를 즉시 알 수가 없다.
    

또한, `Table (Sequence)` 전략은 성능상 이슈와 DeadLock에 대한 이슈로 권장하지 않는다고 한다.

따라서 `JDBC Batch Insert` 를 적용했다.

### 기존 코드

```java
@Bean
public JpaItemWriter<Notification> notiJpaWriter() {
    return new JpaItemWriterBuilder<Notification>()
        .entityManagerFactory(emf)
        .build();
}
```

### 수정된 코드

```java
@Bean
public JdbcBatchItemWriter<Notification> notiJDBCBatchWriter() {
    return new JdbcBatchItemWriterBuilder<Notification>()
        .sql("insert into notification (category, title, message, refrigerator_id) values (:category, :title, :message, :refrigerator.id)")
        .dataSource(dataSource)
        .beanMapped()
        .build();
}
```

### 성능 비교

`BatchSize` = `ChunkSize` = 1000

| 데이터 개수 | 1000 | 5000 | 25000 |
| --- | --- | --- | --- |
| `JPA saveAll` | 490 | 1803 | 18431 |
| `JDBC Bulk Insert`  | 288 | 1008 | 12853 |

---

## 3. Tasklet → Chunk Oriented Processing

<aside>
💡 기존의 `Tasklet` 방식으로 구현된 `Step` 을 `Chunk-Oriented Processing` 방식으로 변경

</aside>

### 변경한 이유

**`TaskletStep`** 은 기본적으로 기본적으로 간단한 단일 작업을 수행하는 인터페이스이다.

따라서 데이터베이스에서 대량의 데이터를 처리하지 않고, 비교적 짧은 시간이 걸리는 단순 작업에 적합하다.

이에 반해, **`Chunk-Oriented Processing`** 은 ****데이터를 청크 단위로 나누어 처리하고 커밋하는 인터페이스로서 대량의 데이터를 처리할 때 유리하다.

기존 로직은 다중 작업에 가깝고, 1000만개 이상의 음식을 관리할 수 있는 로직을 구현하는 것이 목표였기 때문에 **`Chunk-Oriented Processing`** 방식으로 변경했다. 이로 인해 처리 중 실패 시 특정 청크에서만 롤백되므로 안정적인 트랜잭션 관리도 가능하다.

**[내 로직]**

**Step1**

1. DB에서 유통기한이 지난 음식(`Food`)을 조회 → `Reader`
2. 해당 음식의 신선도(`Freshness`)를 `BAD` 로 update → `Processor`
3. DB에 음식(`Food`) 저장 → `Writer`

**Step2**

1. DB에서 신선도가 안 좋은 음식(`Food`) 조회 → `Reader`
2. 해당 음식(`Food`)에 대해 알림 엔티티(`Notification`)으로 변환 → `Processor`
3. 알림 전송 및 DB에 알림 엔티티(`Notification`) 저장 → `Writer`


### 고려 사항

- **페이징 기반 Item Reader** vs **커서 기반 Item Reader**

| **기준** | **페이징 기반 Item Reader** | **커서 기반 Item Reader** |
| --- | --- | --- |
| **읽기 방식** | 페이지 단위로 데이터를 읽어옴 | 커서를 사용하여 데이터를 순차적으로 읽어옴 |
| **메모리 사용량** | 페이지 단위로 데이터를 읽기 때문에 적음 | 커서를 통해 순차적으로 데이터를 읽기 때문에 적음 |
| **데이터 변경 감지** | 데이터 변경에 민감하며, 중복이나 누락이 발생할 수 있음 | 데이터 변경에 덜 민감하며, 실시간 데이터 처리에 유리 |
| **성능** | 큰 데이터셋에서 페이지네이션 쿼리 성능이 저하될 수 있음 | 대량의 데이터를 안정적으로 처리 가능 |
| **재시작 용이성** | 실패한 페이지부터 다시 시작할 수 있음 | 중간에 실패하면 다시 시작하기 어려움 |
| **데이터베이스 연결** | 각 페이지마다 독립적인 쿼리를 실행 | 데이터베이스 연결이 커서를 통해 유지됨 |

요약하자면, Batch의 수행시간이 오래 걸리는 경우 `PagingItemReader`가 좋다. 

**Paging의 경우 한 페이지를 읽을 때마다 Connection을 맺고 끊기 때문에 아무리 많은 데이터라도 Timeout과 부하없이 수행될 수 있다.**

따라서 상당히 많은 음식을 관리해주는 Batch 작업의 경우에는 `PagingItemReader` 가 더 적합하다고 판단했다.

(또한 JPA에서는 Cursor 기반 Database 접근을 지원하지 않는다.)

### 기존 코드

```java
@Bean
public Step findExpireFoodStep() {
    return new StepBuilder("findExpireFoodStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
		        // ...
            **List<Long> expireFoodIdList = foodRepository.findExpireFood();**
            // ...
            return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
}

@Bean
public Step updateFoodFreshnessStep() {
    return new StepBuilder("updateFoodFreshnessStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
            // ...
            List<Food> expireFoodList = foodRepository.findUpdateFood(expireFoodIdList);
            for (Food food : expireFoodList) {
                foodRepository.save(food);
            }

            return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
}
```

### 수정된 코드

```java
public class FoodExpireBatchConfig {
		// ...

		@Bean
    public Step manageExpiredFoodStep() {
        return new StepBuilder("manageExpiredFoodStep", jobRepository)
            .<Food, Food>chunk(chunkSize, transactionManager)
            **.reader(expiredFoodReader())
            .processor(expiredFoodProcessor())
            .writer(customItemWriter)**
            .build();
    }

    @Bean
    public JpaPagingItemReader<Food> expiredFoodReader() {
        return new JpaPagingItemReaderBuilder<Food>()
            .name(BEAN_PREFIX + "Reader")
            .entityManagerFactory(emf)
            .pageSize(chunkSize)
            .queryString(
                "SELECT f FROM Food f WHERE f.expirationDate < CURRENT_DATE")
            .build();
    }

    @Bean
    public ItemProcessor<Food, Food> expiredFoodProcessor() {
        return food -> {
            food.setFreshness(Freshness.BAD);
            return food;
        };
    }
}
    
public class CustomItemWriter implements ItemWriter<Food> {
    private final FoodRepository foodRepository;

    public void write(Chunk<? extends Food> foods) throws Exception {
        foodRepository.saveAll(foods);
    }
}
```

---

## 4. Retry Logic 구현

### 이유

FCM 알림을 전송하는 과정에서 발생할 수 있는 Error 중 `Internal Server Error` 가 있다.

해당 Error의 경우 FCM 내부 서버 에러로 인해 발생하는 것이며, 대부분의 경우 재시도하면 해결된다.

이러한 에러를 대비하여 Spring Batch에서 `Retry` 를 사용하여 재시도 로직을 구현했다.

### 구현 코드

```java
try {
    String response = FirebaseMessaging.getInstance().send(message);
    log.info("[FCM send] " + response);
} catch (FirebaseMessagingException e) {
    log.info("[FCM except]" + e.getMessage());
    
    if (e.getMessagingErrorCode().equals(MessagingErrorCode.INTERNAL)) {
		    throw e;
		}
}
```

```java
@Bean
public Step sendDangerNotificationStep() {
    return new StepBuilder("sendDangerNotificationStep", jobRepository)
        .<Food, Notification>chunk(chunkSize, transactionManager)
        .reader(notiReader())
        .processor(notiProcessor())
        .writer(notiItemWriter)
        **.faultTolerant()
        .retryLimit(5)
        .retry(InternalServerError.class)**
        .build();
}
```

---

## 5. JobExcution → StepExcution

### 이유

기존에는 `Job ExcutionContext` 에 데이터를 저장했다.

하지만 해당 방법에는 `Job` 과 `Step` 간 강한 결합이 생긴다는 것을 파악했다.

→ 만약 다른 `Job` 에서 `Step` 구현체를 재사용하는 경우, 해당 데이터가 필요 없는 경우가 생길 수 있다.

즉, 이 경우 `Step` 구현체 코드를 수정해야한다.

이는 `Job` 과 `Step` 간 강한 결합을 뜻한다.

이러한 문제로 인해 Spring 공식 문서에서는 다음과 같은 방식을 권장한다

- `Step ExecutionContext`에 데이터 저장 + `ExecutionContextPromotionListener`

`Listener` 는 배치 흐름 중에 Job, Step, Chunk 실행 전후에 어떤 동작을 하도록 하는 클래스다.

`ExcutionContextPromotionListener` 는 설정된 키에 대해 `Step ExcutionContext` 의 데이터들을 Step이 완료되는 시점에 자동으로 `Job ExcutionContext` 로 승격(Promotion)해주는 Listener이다.

### 기존 코드

```java
public class FoodExpireBatchConfig {
		@Bean
		public Step manageExpiredFoodStep() {
	    return new StepBuilder("manageExpiredFoodStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
		            // ...
		            
                ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
                jobExecutionContext.put("foods", foods);

                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
		}
}
```

### 수정된 코드

```java
public class FoodExpireBatchConfig {
		@Bean
		public Step manageExpiredFoodStep() {
		    return new StepBuilder("manageExpiredFoodStep", jobRepository)
		        .<Food, Food>chunk(chunkSize, transactionManager)
		        .reader(expiredFoodReader())
		        .processor(expiredFoodProcessor())
		        .writer(customItemWriter)
		        **.listener(promotionListener())**
		        .build();
		}
		
		@Bean
		public ExecutionContextPromotionListener promotionListener() {
		    **ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
		    listener.setKeys(new String[]{"foods"}); // 자동으로 승격시킬 키 목록
		    return listener;**
		}
}

@Component
@RequiredArgsConstructor
public class CustomItemWriter implements ItemWriter<Food> {

    private StepExecution stepExecution;
    private final FoodRepository foodRepository;

    public void write(Chunk<? extends Food> foods) throws Exception {
        foodRepository.saveAllAndFlush(foods);

        List<Long> foodIdList = new ArrayList<>();
        foods.forEach(food -> foodIdList.add(food.getId()));

        **ExecutionContext stepContext = this.stepExecution.getExecutionContext();
        stepContext.put("foods", foodIdList);**
    }

    **@BeforeStep
    public void saveStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }**
}
```

(그러나 비즈니스 로직의 변경으로 인해 `Step` 간 데이터 공유 로직은 사라졌다.)
