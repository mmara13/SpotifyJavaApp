package services;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditService {
    private static final String FILE_PATH = "audit_log.csv";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    //singleton instance
    private static AuditService instance = null;

    private AuditService() {
        //private constructor to restrict instantiation
    }

    public static AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    public void logAction(String actionName) {
        String timestamp = LocalDateTime.now().format(formatter);
        try (FileWriter fw = new FileWriter(FILE_PATH, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(actionName + "," + timestamp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
