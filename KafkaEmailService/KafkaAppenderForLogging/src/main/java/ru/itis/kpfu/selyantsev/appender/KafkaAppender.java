package ru.itis.kpfu.selyantsev.appender;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.util.Properties;

@Plugin(name = "KafkaAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class KafkaAppender extends AbstractAppender {

    private final KafkaProducer<String, String> producer;
    private final String kafkaTopic;

    protected KafkaAppender(
            String name,
            Layout<? extends Serializable> layout,
            Filter filter,
            String kafkaTopic,
            Property[] properties,
            boolean ignoreExceptions
    ) {
        super(name, filter, layout, ignoreExceptions, properties);
        this.kafkaTopic = kafkaTopic;

        Properties kafkaProps = new Properties();
        if (properties != null) {
            for (Property property : properties) {
                kafkaProps.put(property.getName(), property.getValue());
            }
        }

        this.producer = new KafkaProducer<>(kafkaProps);
    }

    @PluginFactory
    public static KafkaAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginAttribute("topic") String kafkaTopic,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") Filter filter,
            @PluginElement("Property") Property[] properties
    ) {
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new KafkaAppender(name, layout, filter, kafkaTopic, properties, true);
    }

    @Override
    public void append(LogEvent event) {
        String key = event.getLevel().name();
        String message = new String(getLayout().toByteArray(event));
        ProducerRecord<String, String> actualMessage = new ProducerRecord<>(kafkaTopic, key, message);
        producer.send(actualMessage);
    }

    @Override
    public void stop() {
        super.stop();
        producer.close();
    }
}
