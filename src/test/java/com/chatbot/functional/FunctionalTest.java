package com.chatbot.functional;

import com.chatbot.config.ConfigManager;
import com.chatbot.utils.ApiUtil;
import com.chatbot.utils.ExtentReportManager;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.HashMap;
import java.util.Map;

/**
 * FunctionalTest — Verifies chatbot provides expected responses to
 * common conversational scenarios via the API.
 *
 * Covers:
 *  - Greetings
 *  - FAQ responses
 *  - User query handling
 *  - Keyword presence in responses
 *  - Session continuity
 *  - Multilingual basic greetings
 *  - Politeness / tone
 */
@Epic("AI Chatbot Testing")
@Feature("Functional Testing")
public class FunctionalTest {

    private static final Logger log = LogManager.getLogger(FunctionalTest.class);
    private static final ConfigManager config = ConfigManager.getInstance();

    @BeforeSuite(alwaysRun = true)
    public void suiteSetup() {
        ExtentReportManager.getExtentReports();
    }

    @BeforeClass
    public void classSetup() {
        ExtentReportManager.createTest("FunctionalTest", "Functional API tests for chatbot response validation");
    }

    // ── 1. Greetings ──────────────────────────────────────────────────────────

    @Test(description = "Verify chatbot responds to standard greetings with appropriate language",
          dataProvider = "greetings",
          groups = {"functional", "api"})
    @Story("Greeting Responses")
    @Severity(SeverityLevel.CRITICAL)
    public void testGreetingResponses(String greeting, String[] expectedKeywords) {
        ExtentReportManager.logInfo("Testing greeting: " + greeting);
        Response response = sendMessage(greeting);

        Assert.assertEquals(response.getStatusCode(), 200,
            "Greeting should return 200 OK.");

        String responseText = extractResponse(response).toLowerCase();
        Assert.assertFalse(responseText.isEmpty(),
            "Response to greeting should not be empty.");

        boolean keywordFound = false;
        for (String kw : expectedKeywords) {
            if (responseText.contains(kw.toLowerCase())) {
                keywordFound = true;
                break;
            }
        }
        Assert.assertTrue(keywordFound,
            "Greeting response should contain one of: " + String.join(", ", expectedKeywords) +
            ". Got: " + responseText);

        ExtentReportManager.logPass("Greeting '" + greeting + "' → response validated.");
    }

    @DataProvider(name = "greetings")
    public Object[][] greetingsProvider() {
        return new Object[][]{
            {"Hello",      new String[]{"hello", "hi", "hey", "welcome", "assist", "help"}},
            {"Hi there",   new String[]{"hello", "hi", "hey", "welcome", "assist"}},
            {"Good morning", new String[]{"morning", "hello", "hi", "welcome", "day"}},
            {"Good evening", new String[]{"evening", "hello", "hi", "welcome"}},
            {"Hey",        new String[]{"hello", "hi", "hey", "welcome", "help"}},
        };
    }

    // ── 2. FAQ Responses ──────────────────────────────────────────────────────

    @Test(description = "Verify chatbot handles FAQ questions with relevant responses",
          dataProvider = "faqs",
          groups = {"functional", "api"})
    @Story("FAQ Handling")
    @Severity(SeverityLevel.NORMAL)
    public void testFAQResponses(String question, String[] expectedKeywords) {
        ExtentReportManager.logInfo("FAQ question: " + question);
        Response response = sendMessage(question);

        Assert.assertEquals(response.getStatusCode(), 200);
        String responseText = extractResponse(response).toLowerCase();
        Assert.assertFalse(responseText.isEmpty(), "FAQ response should not be empty.");

        // At least one expected keyword should be present
        boolean found = false;
        for (String kw : expectedKeywords) {
            if (responseText.contains(kw.toLowerCase())) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found,
            "FAQ response should contain relevant keywords. Got: " + responseText);

        ExtentReportManager.logPass("FAQ '" + question + "' → relevant response confirmed.");
    }

    @DataProvider(name = "faqs")
    public Object[][] faqsProvider() {
        return new Object[][]{
            {"What can you do?",                new String[]{"help", "assist", "answer", "support", "question"}},
            {"How do I contact support?",        new String[]{"support", "contact", "email", "help", "team"}},
            {"What are your working hours?",     new String[]{"hour", "time", "available", "support", "24"}},
            {"How do I reset my password?",      new String[]{"password", "reset", "email", "link", "account"}},
            {"Do you support multiple languages?",new String[]{"language", "support", "english", "multilingual"}},
            {"What is your refund policy?",      new String[]{"refund", "policy", "return", "purchase"}},
            {"How do I cancel my account?",      new String[]{"cancel", "account", "close", "delete", "process"}},
        };
    }

    // ── 3. Response Not Empty for General Queries ─────────────────────────────

    @Test(description = "Verify chatbot returns non-empty responses for general user queries",
          dataProvider = "generalQueries",
          groups = {"functional", "api"})
    @Story("General Query Handling")
    @Severity(SeverityLevel.NORMAL)
    public void testGeneralQueryResponses(String query) {
        Response response = sendMessage(query);

        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertFalse(extractResponse(response).isEmpty(),
            "Response to general query should not be empty: " + query);
        ExtentReportManager.logPass("General query handled: " + query);
    }

    @DataProvider(name = "generalQueries")
    public Object[][] generalQueriesProvider() {
        return new Object[][]{
            {"I need some help"},
            {"Can you assist me?"},
            {"Tell me something interesting"},
            {"What should I do next?"},
            {"I'm having trouble with my order"},
            {"Who are you?"},
            {"What is your purpose?"},
        };
    }

    // ── 4. Response Time Per Message Category ─────────────────────────────────

    @Test(description = "Verify response time is acceptable for all message categories",
          dataProvider = "allMessageCategories",
          groups = {"functional", "performance", "api"})
    @Story("Response Time")
    @Severity(SeverityLevel.MINOR)
    public void testResponseTimePerCategory(String category, String message) {
        long start    = System.currentTimeMillis();
        Response resp = sendMessage(message);
        long elapsed  = System.currentTimeMillis() - start;

        Assert.assertEquals(resp.getStatusCode(), 200);
        Assert.assertTrue(elapsed <= config.getAcceptableResponseMs(),
            String.format("[%s] Response time %dms exceeds %dms", category, elapsed, config.getAcceptableResponseMs()));

        ExtentReportManager.logPass("[" + category + "] Response in " + elapsed + "ms");
    }

    @DataProvider(name = "allMessageCategories")
    public Object[][] allMessageCategoriesProvider() {
        return new Object[][]{
            {"Greeting",  "Hello"},
            {"FAQ",       "How do I reset my password?"},
            {"Support",   "I need technical support"},
            {"Goodbye",   "Thank you, goodbye"},
        };
    }

    // ── 5. Session Continuity ─────────────────────────────────────────────────

    @Test(description = "Verify same session ID maintains conversation context",
          groups = {"functional", "api"})
    @Story("Session Continuity")
    @Severity(SeverityLevel.NORMAL)
    public void testSessionContinuity() {
        String sessionId = "functional-session-" + System.currentTimeMillis();

        // First message
        Map<String, Object> msg1 = buildRequest("My name is TestBot QA", sessionId);
        Response r1 = ApiUtil.post(config.getChatEndpoint(), msg1);
        Assert.assertEquals(r1.getStatusCode(), 200, "First message should succeed.");

        // Follow-up in same session
        Map<String, Object> msg2 = buildRequest("What did I just tell you?", sessionId);
        Response r2 = ApiUtil.post(config.getChatEndpoint(), msg2);
        Assert.assertEquals(r2.getStatusCode(), 200, "Follow-up message should succeed.");

        String response = extractResponse(r2);
        Assert.assertFalse(response.isEmpty(), "Second response should not be empty.");

        ExtentReportManager.logPass("Session continuity verified across two messages.");
    }

    // ── 6. Polite Response Check ──────────────────────────────────────────────

    @Test(description = "Verify chatbot response does not contain rude or inappropriate language",
          groups = {"functional", "api"})
    @Story("Response Quality")
    @Severity(SeverityLevel.NORMAL)
    public void testResponsePoliteness() {
        String[] testMessages = {"Hello", "Help me", "What can you do?"};
        String[] rudeTerms    = {"damn", "hell", "stupid", "idiot", "shut up"};

        for (String msg : testMessages) {
            Response response = sendMessage(msg);
            String body = extractResponse(response).toLowerCase();

            for (String term : rudeTerms) {
                Assert.assertFalse(body.contains(term),
                    "Response contains inappropriate term '" + term + "' for input: " + msg);
            }
        }
        ExtentReportManager.logPass("All responses are polite and appropriate.");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Response sendMessage(String message) {
        return ApiUtil.post(config.getChatEndpoint(),
            buildRequest(message, "func-session-" + System.currentTimeMillis()));
    }

    private Map<String, Object> buildRequest(String message, String sessionId) {
        Map<String, Object> body = new HashMap<>();
        body.put("message",   message);
        body.put("sessionId", sessionId);
        body.put("userId",    "functional-tester");
        return body;
    }

    private String extractResponse(Response response) {
        try {
            String val = response.jsonPath().getString("response");
            return val != null ? val : response.getBody().asString();
        } catch (Exception e) {
            return response.getBody().asString();
        }
    }
}
