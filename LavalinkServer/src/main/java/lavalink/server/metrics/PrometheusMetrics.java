package lavalink.server.metrics;

import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.logback.InstrumentedAppender;
import lavalink.server.logging.ConsoleLogging;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.management.NotificationEmitter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

/**
 * Created by napster on 08.05.18.
 */
@Component
@ConditionalOnProperty("metrics.prometheus.enabled")
public class PrometheusMetrics {

    public PrometheusMetrics() {

        InstrumentedAppender prometheusAppender = new InstrumentedAppender();
        //log metrics
        prometheusAppender.start();

        //jvm (hotspot) metrics
        DefaultExports.initialize();

        //gc pause buckets
        final GcNotificationListener gcNotificationListener = new GcNotificationListener();
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (gcBean instanceof NotificationEmitter)
                ((NotificationEmitter) gcBean).addNotificationListener(gcNotificationListener, null, gcBean);
        }

        ConsoleLogging.LogUpdate("Prometheus metrics set up");
    }
}
