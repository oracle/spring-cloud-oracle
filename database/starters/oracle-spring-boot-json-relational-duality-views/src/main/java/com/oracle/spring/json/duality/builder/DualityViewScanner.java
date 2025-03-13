package com.oracle.spring.json.duality.builder;

import java.util.Set;

import com.oracle.spring.json.duality.annotation.JsonRelationalDualityView;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
final public class DualityViewScanner {
    private final DualityViewBuilder dualityViewBuilder;
    private final EntityManager entityManager;

    public DualityViewScanner(DualityViewBuilder dualityViewBuilder, EntityManager entityManager) {
        this.dualityViewBuilder = dualityViewBuilder;
        this.entityManager = entityManager;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void scan() {
        Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
        for (EntityType<?> entityType : entities) {
            Class<?> javaType = entityType.getJavaType();
            JsonRelationalDualityView dvAnnotation = javaType.getAnnotation(JsonRelationalDualityView.class);
            if (dvAnnotation != null) {
                dualityViewBuilder.apply(javaType);
            }
        }
    }
}
