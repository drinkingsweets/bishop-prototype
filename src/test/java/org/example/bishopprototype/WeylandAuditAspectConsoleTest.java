package org.example.bishopprototype;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.example.bishopprototype.controller.BishopController;
import org.example.synthetichumancorestarter.audit.AuditMode;
import org.example.synthetichumancorestarter.audit.AuditProperties;
import org.example.synthetichumancorestarter.audit.WeylandAuditAspect;
import org.example.synthetichumancorestarter.command.CommandQueueManager;
import org.example.synthetichumancorestarter.command.CriticalCommandProcessor;
import org.example.synthetichumancorestarter.monitoring.TaskMetricsService;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


public class WeylandAuditAspectConsoleTest {

    @Test
    void testWeylandAspectLogsToConsole() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(WeylandAuditAspect.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        AuditProperties auditProperties = new AuditProperties();
        auditProperties.setMode(AuditMode.CONSOLE);

        TaskMetricsService taskMetricsService = mock(TaskMetricsService.class);
        when(taskMetricsService.getQueueSize()).thenReturn(0);
        when(taskMetricsService.getCompletedByAuthor()).thenReturn(Map.of("mark", 42));

        BishopController target = new BishopController(
                mock(CommandQueueManager.class),
                taskMetricsService,
                mock(CriticalCommandProcessor.class)
        );

        WeylandAuditAspect aspect = new WeylandAuditAspect(auditProperties, null);
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        BishopController proxy = factory.getProxy();

        proxy.getMetrics();

        List<ILoggingEvent> logsList = listAppender.list;
        boolean found = logsList.stream()
                .anyMatch(event ->
                        event.getLevel().equals(Level.INFO) &&
                                event.getFormattedMessage().contains("Аудит: метод getMetrics вызван"));

        assertTrue(found, "Ожидалось сообщение аудита в логах");
    }
}
