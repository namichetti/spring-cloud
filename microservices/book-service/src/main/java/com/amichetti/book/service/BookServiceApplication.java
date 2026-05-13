package com.amichetti.book.service;

import com.amichetti.book.service.persistence.BookEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

@SpringBootApplication
@ComponentScan("com.amichetti")
public class BookServiceApplication {

    private static final Logger LOG = LoggerFactory.getLogger(BookServiceApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(BookServiceApplication.class, args);

        //Lee propiedades desde:
        //application.properties
        //application-docker.properties
        String mongodDbHost = ctx.getEnvironment().getProperty("spring.data.mongodb.host");
        String mongodDbPort = ctx.getEnvironment().getProperty("spring.data.mongodb.port");

        //Muy útil para verificar si estás en Docker o local
        LOG.info("Connected to MongoDb: " + mongodDbHost + ":" + mongodDbPort);
    }

    @Autowired
    MongoOperations mongoTemplate;

    //Creación de índices
    //Este método se ejecuta automáticamente cuando: La aplicación termina de arrancar
    @EventListener(ContextRefreshedEvent.class)
    public void initIndicesAfterStartup() {

        //Spring analiza las clases
        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext
                = mongoTemplate.getConverter().getMappingContext();

        //Crea un resolver de índices. Busca anotaciones como @Indexed
        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);

        //Aplica índices a Mongo. Esto:
        //Detecta índices definidos en la entidad
        //Los crea automáticamente en MongoDB
        IndexOperations indexOps = mongoTemplate.indexOps(BookEntity.class);
        resolver.resolveIndexFor(BookEntity.class).forEach(
                e -> indexOps.createIndex(e));

        //Sin esto:
        //Mongo NO crea índices automáticamente. Podríamos tener:
        //queries lentas
        //duplicados
    }
}

