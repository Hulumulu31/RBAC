import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Тесты для класса ReportGenerator.
 */
class ReportGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("generateUserReport - отчёт с пользователями")
    void testGenerateUserReport() {
        UserManager userManager = new UserManager();
        AssignmentManager assignmentManager = new AssignmentManager(userManager, new RoleManager());
        ReportGenerator generator = new ReportGenerator();

        // Пустой отчёт
        String emptyReport = generator.generateUserReport(userManager, assignmentManager);
        assertTrue(emptyReport.contains("USER REPORT"));
        assertTrue(emptyReport.contains("No users found") || emptyReport.contains("Total users: 0"));

        // С пользователями
        try {
            userManager.add(User.validate("john_doe", "John Doe", "john@test.com"));
            userManager.add(User.validate("jane_smith", "Jane Smith", "jane@test.com"));
        } catch (Exception e) {
            fail("Failed to create test users");
        }

        String report = generator.generateUserReport(userManager, assignmentManager);
        
        assertNotNull(report);
        assertTrue(report.contains("USER REPORT"));
        assertTrue(report.contains("john_doe"));
        assertTrue(report.contains("jane_smith"));
        assertTrue(report.contains("Total users: 2"));
    }

    @Test
    @DisplayName("generateRoleReport - отчёт с ролями")
    void testGenerateRoleReport() {
        RoleManager roleManager = new RoleManager();
        AssignmentManager assignmentManager = new AssignmentManager(new UserManager(), roleManager);
        ReportGenerator generator = new ReportGenerator();

        // Пустой отчёт
        String emptyReport = generator.generateRoleReport(roleManager, assignmentManager);
        assertTrue(emptyReport.contains("ROLE REPORT"));

        // С ролями
        try {
            Role admin = new Role("Administrator", "Full access");
            admin.addPermission(new Permission("READ", "users", "Can read users"));
            roleManager.add(admin);
        } catch (Exception e) {
            fail("Failed to create test role");
        }

        String report = generator.generateRoleReport(roleManager, assignmentManager);
        
        assertNotNull(report);
        assertTrue(report.contains("ROLE REPORT"));
        assertTrue(report.contains("Administrator"));
    }

    @Test
    @DisplayName("generatePermissionMatrix - матрица прав")
    void testGeneratePermissionMatrix() {
        UserManager userManager = new UserManager();
        RoleManager roleManager = new RoleManager();
        AssignmentManager assignmentManager = new AssignmentManager(userManager, roleManager);
        ReportGenerator generator = new ReportGenerator();

        // Пустая матрица
        String emptyMatrix = generator.generatePermissionMatrix(userManager, assignmentManager);
        assertTrue(emptyMatrix.contains("PERMISSION MATRIX"));

        // С данными
        try {
            User user = User.validate("test_user", "Test User", "test@test.com");
            userManager.add(user);

            Role role = new Role("TestRole", "Test role");
            role.addPermission(new Permission("READ", "users", "Read access"));
            roleManager.add(role);

            var metadata = AssignmentMetadata.now("admin", "Test");
            var assignment = new PermanentAssignment(user, role, metadata);
            assignmentManager.add(assignment);
        } catch (Exception e) {
            fail("Failed to create test data: " + e.getMessage());
        }

        String matrix = generator.generatePermissionMatrix(userManager, assignmentManager);
        
        assertNotNull(matrix);
        assertTrue(matrix.contains("PERMISSION MATRIX"));
    }

    @Test
    @DisplayName("generateSummaryReport - сводный отчёт")
    void testGenerateSummaryReport() {
        UserManager userManager = new UserManager();
        RoleManager roleManager = new RoleManager();
        AssignmentManager assignmentManager = new AssignmentManager(userManager, roleManager);
        ReportGenerator generator = new ReportGenerator();

        String report = generator.generateSummaryReport(userManager, roleManager, assignmentManager);
        
        assertNotNull(report);
        assertTrue(report.contains("SYSTEM SUMMARY REPORT"));
        assertTrue(report.contains("Total Users"));
        assertTrue(report.contains("Total Roles"));
        assertTrue(report.contains("Total Assignments"));
    }

    @Test
    @DisplayName("exportToFile - экспорт в файл")
    void testExportToFile() throws IOException {
        ReportGenerator generator = new ReportGenerator();
        
        String report = "Test Report Content\nLine 2\nLine 3";
        Path reportFile = tempDir.resolve("report.txt");
        
        generator.exportToFile(report, reportFile.toString());
        
        assertTrue(Files.exists(reportFile));
        
        String content = Files.readString(reportFile);
        assertEquals(report, content);
    }

    @Test
    @DisplayName("exportToFile - экспорт пустого отчёта")
    void testExportToFile_empty() throws IOException {
        ReportGenerator generator = new ReportGenerator();
        
        Path reportFile = tempDir.resolve("empty_report.txt");
        generator.exportToFile("", reportFile.toString());
        
        assertTrue(Files.exists(reportFile));
        assertEquals("", Files.readString(reportFile));
    }

    @Test
    @DisplayName("generateUserReport - форматирование таблицы")
    void testGenerateUserReport_tableFormat() {
        UserManager userManager = new UserManager();
        AssignmentManager assignmentManager = new AssignmentManager(userManager, new RoleManager());
        ReportGenerator generator = new ReportGenerator();

        try {
            userManager.add(User.validate("admin", "Admin User", "admin@company.com"));
        } catch (Exception e) {
            fail("Failed to create test user");
        }

        String report = generator.generateUserReport(userManager, assignmentManager);
        
        // Проверка наличия разделителей таблицы
        assertTrue(report.contains("---") || report.contains("===") || report.contains("|"));
    }

    @Test
    @DisplayName("generateRoleReport - детализация прав")
    void testGenerateRoleReport_permissionsDetail() {
        RoleManager roleManager = new RoleManager();
        AssignmentManager assignmentManager = new AssignmentManager(new UserManager(), roleManager);
        ReportGenerator generator = new ReportGenerator();

        try {
            Role role = new Role("Manager", "Management role");
            role.addPermission(new Permission("READ", "users", "Read users"));
            role.addPermission(new Permission("WRITE", "users", "Write users"));
            roleManager.add(role);
        } catch (Exception e) {
            fail("Failed to create test role");
        }

        String report = generator.generateRoleReport(roleManager, assignmentManager);
        
        assertTrue(report.contains("Permissions"));
    }
}
