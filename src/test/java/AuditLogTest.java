import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Тесты для класса AuditLog.
 */
class AuditLogTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("log - добавление записи")
    void testLog() {
        AuditLog auditLog = new AuditLog();
        
        auditLog.log("CREATE_USER", "admin", "john_doe", "New user created");
        
        assertEquals(1, auditLog.size());
        
        List<AuditLog.AuditEntry> entries = auditLog.getAll();
        assertEquals("CREATE_USER", entries.get(0).action());
        assertEquals("admin", entries.get(0).performer());
        assertEquals("john_doe", entries.get(0).target());
        assertEquals("New user created", entries.get(0).details());
        assertNotNull(entries.get(0).timestamp());
    }

    @Test
    @DisplayName("getAll - получение всех записей")
    void testGetAll() {
        AuditLog auditLog = new AuditLog();
        
        auditLog.log("CREATE_USER", "admin", "user1", "Details 1");
        auditLog.log("DELETE_ROLE", "admin", "role1", "Details 2");
        auditLog.log("ASSIGN_ROLE", "manager", "user2", "Details 3");
        
        List<AuditLog.AuditEntry> entries = auditLog.getAll();
        
        assertEquals(3, entries.size());
    }

    @Test
    @DisplayName("getByPerformer - фильтрация по исполнителю")
    void testGetByPerformer() {
        AuditLog auditLog = new AuditLog();
        
        auditLog.log("CREATE_USER", "admin", "user1", "Details 1");
        auditLog.log("DELETE_ROLE", "admin", "role1", "Details 2");
        auditLog.log("ASSIGN_ROLE", "manager", "user2", "Details 3");
        
        List<AuditLog.AuditEntry> adminEntries = auditLog.getByPerformer("admin");
        
        assertEquals(2, adminEntries.size());
        assertTrue(adminEntries.stream().allMatch(e -> e.performer().equals("admin")));
    }

    @Test
    @DisplayName("getByPerformer - нечувствительность к регистру")
    void testGetByPerformer_caseInsensitive() {
        AuditLog auditLog = new AuditLog();
        
        auditLog.log("CREATE_USER", "Admin", "user1", "Details 1");
        auditLog.log("DELETE_ROLE", "ADMIN", "role1", "Details 2");
        
        List<AuditLog.AuditEntry> entries = auditLog.getByPerformer("admin");
        
        assertEquals(2, entries.size());
    }

    @Test
    @DisplayName("getByPerformer - null и пустая строка")
    void testGetByPerformer_empty() {
        AuditLog auditLog = new AuditLog();
        auditLog.log("CREATE_USER", "admin", "user1", "Details");
        
        assertTrue(auditLog.getByPerformer(null).isEmpty());
        assertTrue(auditLog.getByPerformer("").isEmpty());
        assertTrue(auditLog.getByPerformer("   ").isEmpty());
    }

    @Test
    @DisplayName("getByAction - фильтрация по действию")
    void testGetByAction() {
        AuditLog auditLog = new AuditLog();
        
        auditLog.log("CREATE_USER", "admin", "user1", "Details 1");
        auditLog.log("CREATE_USER", "manager", "user2", "Details 2");
        auditLog.log("DELETE_ROLE", "admin", "role1", "Details 3");
        
        List<AuditLog.AuditEntry> createEntries = auditLog.getByAction("CREATE_USER");
        
        assertEquals(2, createEntries.size());
        assertTrue(createEntries.stream().allMatch(e -> e.action().equals("CREATE_USER")));
    }

    @Test
    @DisplayName("getByAction - нечувствительность к регистру")
    void testGetByAction_caseInsensitive() {
        AuditLog auditLog = new AuditLog();
        
        auditLog.log("CREATE_USER", "admin", "user1", "Details");
        auditLog.log("create_user", "admin", "user2", "Details");
        
        List<AuditLog.AuditEntry> entries = auditLog.getByAction("CREATE_USER");
        
        assertEquals(2, entries.size());
    }

    @Test
    @DisplayName("getByDateRange - фильтрация по диапазону дат")
    void testGetByDateRange() {
        AuditLog auditLog = new AuditLog();
        
        // Записи добавляются с текущей датой, поэтому тестируем базовую функциональность
        auditLog.log("CREATE_USER", "admin", "user1", "Details 1");
        auditLog.log("DELETE_ROLE", "admin", "role1", "Details 2");
        
        // Пустой диапазон
        List<AuditLog.AuditEntry> emptyRange = auditLog.getByDateRange("2099-01-01 00:00:00", "2099-12-31 23:59:59");
        assertTrue(emptyRange.isEmpty());
    }

    @Test
    @DisplayName("clear - очистка журнала")
    void testClear() {
        AuditLog auditLog = new AuditLog();
        
        auditLog.log("CREATE_USER", "admin", "user1", "Details");
        assertEquals(1, auditLog.size());
        
        auditLog.clear();
        assertEquals(0, auditLog.size());
        assertTrue(auditLog.getAll().isEmpty());
    }

    @Test
    @DisplayName("saveToFile - сохранение в файл")
    void testSaveToFile() throws IOException {
        AuditLog auditLog = new AuditLog();
        
        auditLog.log("CREATE_USER", "admin", "user1", "Test details");
        auditLog.log("DELETE_ROLE", "admin", "role1", "Another detail");
        
        Path logFile = tempDir.resolve("audit.log");
        auditLog.saveToFile(logFile.toString());
        
        assertTrue(Files.exists(logFile));
        
        String content = Files.readString(logFile);
        assertTrue(content.contains("AUDIT LOG"));
        assertTrue(content.contains("CREATE_USER"));
        assertTrue(content.contains("admin"));
        assertTrue(content.contains("user1"));
    }

    @Test
    @DisplayName("printLog - вывод в консоль (без ошибок)")
    void testPrintLog() {
        AuditLog auditLog = new AuditLog();
        
        auditLog.log("CREATE_USER", "admin", "user1", "Details");
        
        // Просто проверяем, что метод не выбрасывает исключений
        assertDoesNotThrow(() -> auditLog.printLog());
    }

    @Test
    @DisplayName("printLog - пустой журнал")
    void testPrintLog_empty() {
        AuditLog auditLog = new AuditLog();
        
        assertDoesNotThrow(() -> auditLog.printLog());
    }
}
