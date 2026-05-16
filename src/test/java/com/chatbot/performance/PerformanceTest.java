package com.chatbot.performance;

import com.chatbot.config.ConfigManager;
import com.chatbot.utils.ApiUtil;
import com.chatbot.utils.ExtentReportManager;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PerformanceTest — Simulates concurrent user load against the chatbot API
 * and measures response time, error rate, and throughput.
 *
 * Uses Java's ExecutorService to spin up virtual concurrent users.
 *
 * For heavy load testing, pair with the JMeter plan (jmeter/chatbot-load-test.jmx).
 */
@Epic("AI Chatbot Testing")
@Feature("Performance & Stress Testing")
public class PerformanceTest {

    private static final Logger log = LogManager.getLogger(PerformanceTest.class);
    private static final ConfigManager config = ConfigManager.getInstance();

    @BeforeSuite(alwaysRun = true)
    public void suiteSetup() {
        ExtentReportManager.getExtentReports();
    }

    @BeforeClass
    public void classSetup() {
        ExtentReportManager.createTest("PerformanceTest", "Performance and stress tests for chatbot API");
    }

    // ── 1. Baseline Response Time ─────────────────────────────────────────────

    @Test(description = "Baseline: single request response time must be within threshold",
          groups = {"performance", "api"}, priority = 1)
    @Story("Baseline Performance")
    @Severity(SeverityLevel.NORMAL)
    public void testBaselineResponseTime() {
        Map<String, Object> body = buildChatRequest("baseline test message");
        Response response = ApiUtil.post(config.getChatEndpoint(), body);

        long responseTime = ApiUtil.getResponseTimeMs(response);
        long threshold    = config.getAcceptableResponseMs();

        log.info("Baseline response time: {}ms | Threshold: {}ms", responseTime, threshold);
        ExtentReportManager.logInfo("Baseline: " + responseTime + "ms (threshold: " + threshold + "ms)");

        Assert.assertEquals(response.getStatusCode(), 200,
            "Baseline request should return 200.");
        Assert.assertTrue(responseTime <= threshold,
            String.format("Baseline response %dms exceeds threshold %dms", responseTime, threshold));

        ExtentReportManager.logPass("Baseline response time: " + responseTime + "ms — PASS");
    }

    // ── 2. Concurrent Users (Light Load) ──────────────────────────────────────

    @Test(description = "10 concurrent users: all requests succeed within threshold",
          groups = {"performance", "api"}, priority = 2)
    @Story("Concurrent Load")
    @Severity(SeverityLevel.NORMAL)
    public void testLightConcurrentLoad() throws InterruptedException {
        runConcurrentLoadTest(10, "light load");
    }

    // ── 3. Concurrent Users (Medium Load) ────────────────────────────────────

    @Test(description = "25 concurrent users: error rate stays below 5%",
          groups = {"performance", "api"}, priority = 3)
    @Story("Concurrent Load")
    @Severity(SeverityLevel.NORMAL)
    public void testMediumConcurrentLoad() throws InterruptedException {
        runConcurrentLoadTest(25, "medium load");
    }

    // ── 4. Concurrent Users (Heavy Load) ──────────────────────────────────────

    @Test(description = "50 concurrent users: system remains stable",
          groups = {"performance", "stress", "api"}, priority = 4)
    @Story("Stress Testing")
    @Severity(SeverityLevel.NORMAL)
    public void testHeavyConcurrentLoad() throws InterruptedException {
        runConcurrentLoadTest(50, "heavy load");
    }

    // ── 5. Spike Test ─────────────────────────────────────────────────────────

    @Test(description = "Spike test: sudden burst of 30 concurrent users",
          groups = {"performance", "stress", "api"}, priority = 5)
    @Story("Spike Testing")
    @Severity(SeverityLevel.NORMAL)
    public void testSpikeLoad() throws InterruptedException {
        log.info("Spike test: bursting 30 threads simultaneously");
        ExtentReportManager.logInfo("Running spike test with 30 simultaneous threads...");

        int threadCount = 30;
        CountDownLatch startLatch  = new CountDownLatch(1);
        CountDownLatch doneLatch   = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount    = new AtomicInteger(0);
        List<Long> responseTimes   = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // All threads start at exactly the same time
                    long start = System.currentTimeMillis();
                    Response response = ApiUtil.post(
                        config.getChatEndpoint(),
                        buildChatRequest("Spike test from user " + userId)
                    );
                    long elapsed = System.currentTimeMillis() - start;
                    responseTimes.add(elapsed);

                    if (response.getStatusCode() == 200) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                        log.warn("Spike user {} failed with HTTP {}", userId, response.getStatusCode());
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.error("Spike user {} threw exception: {}", userId, e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Fire!
        doneLatch.await(120, TimeUnit.SECONDS);
        executor.shutdown();

        logAndAssertResults("Spike Test", threadCount, successCount, failCount, responseTimes);
    }

    // ── 6. Sustained Load Test ────────────────────────────────────────────────

    @Test(description = "Sustained: 10 users sending 5 requests each sequentially",
          groups = {"performance", "api"}, priority = 6)
    @Story("Sustained Load")
    @Severity(SeverityLevel.MINOR)
    public void testSustainedLoad() throws InterruptedException {
        int userCount    = 10;
        int requestsEach = 5;
        int totalRequests = userCount * requestsEach;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount    = new AtomicInteger(0);
        List<Long> responseTimes   = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch doneLatch   = new CountDownLatch(userCount);

        ExecutorService executor = Executors.newFixedThreadPool(userCount);

        for (int u = 0; u < userCount; u++) {
            final int userId = u;
            executor.submit(() -> {
                for (int r = 0; r < requestsEach; r++) {
                    try {
                        long start = System.currentTimeMillis();
                        Response response = ApiUtil.post(
                            config.getChatEndpoint(),
                            buildChatRequest("Sustained request " + r + " from user " + userId)
                        );
                        long elapsed = System.currentTimeMillis() - start;
                        responseTimes.add(elapsed);

                        if (response.getStatusCode() == 200) {
                            successCount.incrementAndGet();
                        } else {
                            failCount.incrementAndGet();
                        }
                        Thread.sleep(200); // Small delay between requests
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                    }
                }
                doneLatch.countDown();
            });
        }

        doneLatch.await(180, TimeUnit.SECONDS);
        executor.shutdown();

        logAndAssertResults("Sustained Load", totalRequests, successCount, failCount, responseTimes);
    }

    // ── Core Concurrent Runner ────────────────────────────────────────────────

    private void runConcurrentLoadTest(int threadCount, String label) throws InterruptedException {
        log.info("Running concurrent load test: {} threads ({})", threadCount, label);
        ExtentReportManager.logInfo("Concurrent load: " + threadCount + " threads (" + label + ")");

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount    = new AtomicInteger(0);
        List<Long> responseTimes   = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch doneLatch   = new CountDownLatch(threadCount);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    long start = System.currentTimeMillis();
                    Response response = ApiUtil.post(
                        config.getChatEndpoint(),
                        buildChatRequest("Load test message from thread " + userId)
                    );
                    long elapsed = System.currentTimeMillis() - start;
                    responseTimes.add(elapsed);

                    if (response.getStatusCode() == 200) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                        log.warn("Thread {} got HTTP {}", userId, response.getStatusCode());
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.error("Thread {} error: {}", userId, e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        doneLatch.await(120, TimeUnit.SECONDS);
        executor.shutdown();

        logAndAssertResults(label, threadCount, successCount, failCount, responseTimes);
    }

    // ── Stats & Assertions ────────────────────────────────────────────────────

    private void logAndAssertResults(String label, int total,
                                     AtomicInteger success, AtomicInteger fail,
                                     List<Long> times) {

        long avgTime  = times.isEmpty() ? 0 : times.stream().mapToLong(Long::longValue).sum() / times.size();
        long maxTime  = times.isEmpty() ? 0 : Collections.max(times);
        long minTime  = times.isEmpty() ? 0 : Collections.min(times);
        double errorRate = (double) fail.get() / total * 100.0;

        // p95 response time
        List<Long> sorted = new ArrayList<>(times);
        Collections.sort(sorted);
        long p95 = sorted.isEmpty() ? 0 : sorted.get((int) (sorted.size() * 0.95));

        String report = String.format(
            "[%s] Total: %d | Success: %d | Fail: %d | Error Rate: %.1f%% | " +
            "Avg: %dms | Min: %dms | Max: %dms | p95: %dms",
            label, total, success.get(), fail.get(), errorRate, avgTime, minTime, maxTime, p95);

        log.info(report);
        ExtentReportManager.logInfo(report);

        // Assertions
        int maxErrorRate = config.getMaxErrorRatePercent();
        Assert.assertTrue(errorRate <= maxErrorRate,
            String.format("[%s] Error rate %.1f%% exceeds max %d%%", label, errorRate, maxErrorRate));
        Assert.assertTrue(avgTime <= config.getAcceptableResponseMs() * 1.5,
            String.format("[%s] Avg response time %dms exceeds 1.5x threshold", label, avgTime));

        ExtentReportManager.logPass(label + " PASSED — Error rate: " +
            String.format("%.1f%%", errorRate) + " | Avg: " + avgTime + "ms | p95: " + p95 + "ms");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Map<String, Object> buildChatRequest(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        body.put("sessionId", "perf-session-" + Thread.currentThread().getId());
        body.put("userId", "perf-user-" + Thread.currentThread().getId());
        return body;
    }
}
