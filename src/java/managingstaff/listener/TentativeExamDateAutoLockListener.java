package managingstaff.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import managingstaff.service.TentativeExamDateAutoLockService;

/** Chạy lúc ứng dụng khởi động và mỗi giờ để tự khóa ngày thi dự kiến đến hạn. */
@WebListener
public class TentativeExamDateAutoLockListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(TentativeExamDateAutoLockListener.class.getName());
    private final TentativeExamDateAutoLockService autoLockService = new TentativeExamDateAutoLockService();
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable, "tentative-exam-date-auto-lock");
            thread.setDaemon(true);
            return thread;
        };
        scheduler = Executors.newSingleThreadScheduledExecutor(factory);
        scheduler.scheduleWithFixedDelay(this::lockSafely, 0, 1, TimeUnit.HOURS);
    }

    private void lockSafely() {
        try {
            int locked = autoLockService.lockDueDates();
            if (locked > 0) {
                LOG.log(Level.INFO, "Đã tự động khóa {0} ngày thi dự kiến.", locked);
            }
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Tự động khóa ngày thi dự kiến thất bại.", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
