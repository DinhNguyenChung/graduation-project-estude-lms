package org.example.estudebackendspring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Test Report Generator - Tạo báo cáo chi tiết về kết quả test
 * 
 * Chức năng:
 * - Phân tích kết quả test từ Gradle report HTML
 * - Tính tỷ lệ thành công/thất bại
 * - In báo cáo console với format đẹp
 * - Xuất báo cáo ra file text
 * 
 * Sử dụng:
 * Chạy sau khi đã execute: ./gradlew test --tests "ApiTestSuite"
 */
public class TestReportGenerator {

    private static class TestResult {
        String testName;
        String displayName;
        String status; // PASSED, FAILED, SKIPPED
        String errorMessage;
        String duration;

        public TestResult(String displayName, String status) {
            this.displayName = displayName;
            this.status = status;
            this.duration = "";
        }
    }

    /**
     * Tạo và in báo cáo chi tiết từ HTML report
     */
    public static void generateReportFromHTML() {
        String reportPath = "build/reports/tests/test/classes/org.example.estudebackendspring.ApiTestSuite.html";
        File reportFile = new File(reportPath);
        
        if (!reportFile.exists()) {
            System.err.println("❌ Không tìm thấy file báo cáo: " + reportPath);
            System.err.println("   Vui lòng chạy: ./gradlew test --tests \"ApiTestSuite\" trước");
            return;
        }

        System.out.println("\n" + "=".repeat(100));
        System.out.println("🧪 BÁO CÁO CHI TIẾT TEST SUITE: ApiTestSuite");
        System.out.println("=".repeat(100));

        // Parse HTML report
        TestSummary summary = parseHTMLReport(reportFile);

        // In báo cáo console
        printConsoleReport(summary);

        // Xuất báo cáo ra file
        exportReportToFile("ApiTestSuite", summary);
    }

    private static class TestSummary {
        int totalTests = 0;
        int passedTests = 0;
        int failedTests = 0;
        int skippedTests = 0;
        String duration = "";
        List<TestResult> testResults = new ArrayList<>();
    }

    /**
     * Parse HTML report để lấy thông tin test
     */
    private static TestSummary parseHTMLReport(File reportFile) {
        TestSummary summary = new TestSummary();
        Set<String> failedTests = new HashSet<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(reportFile))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                // Lấy tổng quan - số lượng tests
                if (line.contains("<div class=\"infoBox\" id=\"tests\">")) {
                    String nextLine = reader.readLine(); // <div class="counter">30</div>
                    if (nextLine != null) {
                        Pattern pattern = Pattern.compile("<div class=\"counter\">(\\d+)</div>");
                        Matcher matcher = pattern.matcher(nextLine);
                        if (matcher.find()) {
                            summary.totalTests = Integer.parseInt(matcher.group(1));
                        }
                    }
                }
                // Số lượng failures
                else if (line.contains("<div class=\"infoBox\" id=\"failures\">")) {
                    String nextLine = reader.readLine(); // <div class="counter">19</div>
                    if (nextLine != null) {
                        Pattern pattern = Pattern.compile("<div class=\"counter\">(\\d+)</div>");
                        Matcher matcher = pattern.matcher(nextLine);
                        if (matcher.find()) {
                            summary.failedTests = Integer.parseInt(matcher.group(1));
                        }
                    }
                }
                // Duration
                else if (line.contains("<div class=\"infoBox\" id=\"duration\">")) {
                    String nextLine = reader.readLine();
                    if (nextLine != null) {
                        Pattern pattern = Pattern.compile("<div class=\"counter\">(.*?)</div>");
                        Matcher matcher = pattern.matcher(nextLine);
                        if (matcher.find()) {
                            summary.duration = matcher.group(1);
                        }
                    }
                }
                // Parse test names - tìm các dòng có <h3 class="failures"> hoặc <h3 class="success">
                else if (line.contains("<h3 class=\"failures\">")) {
                    Pattern pattern = Pattern.compile("<h3 class=\"failures\">(.*?)</h3>");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String testName = matcher.group(1).trim();
                        TestResult result = new TestResult(testName, "FAILED");
                        summary.testResults.add(result);
                        failedTests.add(testName);
                    }
                }
                else if (line.contains("<h3 class=\"success\">")) {
                    Pattern pattern = Pattern.compile("<h3 class=\"success\">(.*?)</h3>");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String testName = matcher.group(1).trim();
                        TestResult result = new TestResult(testName, "PASSED");
                        summary.testResults.add(result);
                    }
                }
            }
            
            summary.passedTests = summary.totalTests - summary.failedTests;
            
        } catch (IOException e) {
            System.err.println("❌ Lỗi khi đọc file báo cáo: " + e.getMessage());
        }
        
        return summary;
    }

    /**
     * In báo cáo ra console với format đẹp
     */
    private static void printConsoleReport(TestSummary summary) {
        List<TestResult> results = summary.testResults;
        
        System.out.println("\n📊 TỔNG QUAN KẾT QUẢ:");
        System.out.println("-".repeat(100));
        
        int total = summary.totalTests;
        int passed = summary.passedTests;
        int failed = summary.failedTests;
        int skipped = summary.skippedTests;
        
        double passRate = total > 0 ? (passed * 100.0 / total) : 0;
        double failRate = total > 0 ? (failed * 100.0 / total) : 0;
        
        System.out.printf("   ✅ Tổng số tests: %d%n", total);
        System.out.printf("   ✅ Passed: %d (%.2f%%)%n", passed, passRate);
        System.out.printf("   ❌ Failed: %d (%.2f%%)%n", failed, failRate);
        System.out.printf("   ⏭️  Skipped: %d%n", skipped);
        
        // Hiển thị progress bar
        System.out.println("\n📈 TIẾN ĐỘ:");
        printProgressBar(passRate);
        
        System.out.println("\n📋 CHI TIẾT TỪNG TEST CASE:");
        System.out.println("-".repeat(100));
        System.out.printf("%-5s %-60s %-10s %-10s%n", "STT", "TÊN TEST CASE", "KẾT QUẢ", "THỜI GIAN");
        System.out.println("-".repeat(100));
        
        int index = 1;
        for (TestResult result : results) {
            String statusColor = getStatusWithColor(result.status);
            
            System.out.printf("%-5d %-60s %-10s%n", 
                index++, 
                truncate(result.displayName, 58),
                statusColor
            );
            
            if ("FAILED".equals(result.status) && result.errorMessage != null) {
                System.out.printf("      ↳ Lỗi: %s%n", truncate(result.errorMessage, 85));
            }
        }
        
        System.out.println("-".repeat(100));
        
        // Phân loại tests theo kết quả
        printTestsByCategory(results, "✅ TESTS PASSED", "PASSED");
        printTestsByCategory(results, "❌ TESTS FAILED", "FAILED");
        
        System.out.println("\n" + "=".repeat(100));
        System.out.printf("📊 TỶ LỆ THÀNH CÔNG: %.2f%% (%d/%d)%n", passRate, passed, total);
        System.out.println("=".repeat(100) + "\n");
    }

    /**
     * In progress bar cho tỷ lệ pass
     */
    private static void printProgressBar(double percentage) {
        int barLength = 50;
        int filled = (int) (barLength * percentage / 100);
        
        StringBuilder bar = new StringBuilder("   [");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        bar.append(String.format("] %.2f%%", percentage));
        
        System.out.println(bar.toString());
    }

    /**
     * In danh sách tests theo category (PASSED/FAILED)
     */
    private static void printTestsByCategory(List<TestResult> results, String title, String status) {
        List<TestResult> filtered = results.stream()
                .filter(r -> status.equals(r.status))
                .toList();
        
        if (!filtered.isEmpty()) {
            System.out.println("\n" + title + " (" + filtered.size() + "):");
            System.out.println("-".repeat(100));
            
            int index = 1;
            for (TestResult result : filtered) {
                System.out.printf("   %d. %s%n", index++, result.displayName);
                if ("FAILED".equals(status) && result.errorMessage != null) {
                    System.out.printf("      ↳ %s%n", truncate(result.errorMessage, 90));
                }
            }
        }
    }

    /**
     * Xuất báo cáo ra file text
     */
    private static void exportReportToFile(String testClassName, TestSummary summary) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("test-report-%s-%s.txt", testClassName, timestamp);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("=".repeat(100));
            writer.println("BÁO CÁO CHI TIẾT TEST SUITE: " + testClassName);
            writer.println("Thời gian: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            writer.println("=".repeat(100));
            
            int total = summary.totalTests;
            int passed = summary.passedTests;
            int failed = summary.failedTests;
            double passRate = total > 0 ? (passed * 100.0 / total) : 0;
            
            writer.println("\nTỔNG QUAN:");
            writer.printf("- Tổng số tests: %d%n", total);
            writer.printf("- Passed: %d (%.2f%%)%n", passed, passRate);
            writer.printf("- Failed: %d (%.2f%%)%n", failed, total > 0 ? (failed * 100.0 / total) : 0);
            
            writer.println("\n" + "-".repeat(100));
            writer.println("CHI TIẾT TỪNG TEST CASE:");
            writer.println("-".repeat(100));
            
            int index = 1;
            for (TestResult result : summary.testResults) {
                writer.printf("%d. %s - %s%n", 
                    index++, 
                    result.displayName,
                    result.status
                );
                
                if ("FAILED".equals(result.status) && result.errorMessage != null) {
                    writer.printf("   Error: %s%n", result.errorMessage);
                }
            }
            
            writer.println("\n" + "=".repeat(100));
            writer.printf("TỶ LỆ THÀNH CÔNG: %.2f%% (%d/%d)%n", passRate, passed, total);
            writer.println("=".repeat(100));
            
            System.out.println("\n💾 Báo cáo đã được xuất ra file: " + filename);
            
        } catch (IOException e) {
            System.err.println("❌ Lỗi khi xuất báo cáo: " + e.getMessage());
        }
    }

    /**
     * Helper methods
     */
    private static String getStatusIcon(String status) {
        return switch (status) {
            case "PASSED" -> "✅";
            case "FAILED" -> "❌";
            case "SKIPPED" -> "⏭️";
            default -> "❓";
        };
    }

    private static String getStatusWithColor(String status) {
        return switch (status) {
            case "PASSED" -> "✅ PASS";
            case "FAILED" -> "❌ FAIL";
            case "SKIPPED" -> "⏭️ SKIP";
            default -> "❓ UNKNOWN";
        };
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Main method để tạo báo cáo từ HTML report
     */
    public static void main(String[] args) {
        System.out.println("🚀 Đang tạo báo cáo từ test report HTML...\n");
        
        // Đường dẫn tới HTML report
        String htmlReportPath = "build/reports/tests/test/classes/org.example.estudebackendspring.ApiTestSuiteNew.html";
        File reportFile = new File(htmlReportPath);
        
        if (!reportFile.exists()) {
            System.err.println("❌ Không tìm thấy file báo cáo: " + htmlReportPath);
            System.err.println("   Vui lòng chạy: ./gradlew test --tests \"ApiTestSuiteNew\" trước");
            return;
        }
        
        try {
            // Parse HTML report
            TestSummary summary = parseHTMLReport(reportFile);
            
            // In báo cáo ra console
            printConsoleReport(summary);
            
            // Xuất báo cáo ra file
            exportReportToFile("ApiTestSuiteNew", summary);
            
            System.out.println("\n✅ Hoàn tất!");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo báo cáo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
