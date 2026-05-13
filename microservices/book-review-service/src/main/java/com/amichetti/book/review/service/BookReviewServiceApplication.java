package com.amichetti.book.review.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@ComponentScan("com.amichetti")
@SpringBootApplication
public class BookReviewServiceApplication {

    private static final Logger LOG = LoggerFactory.getLogger(BookReviewServiceApplication.class);
    private final Integer threadPoolSize;
    private final Integer taskQueueSize;

    /*
    Como utliza JPA que es bloqueante, podemos ejecutar el código bloqueante utilizando un Scheduler,
    el cual es capaz de ejecutar dicho código en un hilo proveniente de un pool de hilos dedicado
    con un número limitado de hilos.

    El uso de un pool de hilos para el código bloqueante evita agotar los hilos disponibles
    del microservicio y evita afectar el procesamiento concurrente no bloqueante en el microservicio,
    si lo hubiera.
     */
    public BookReviewServiceApplication(
            @Value("${app.threadPoolSize:10}") Integer threadPoolSize,
            @Value("${app.taskQueueSize:100}") Integer taskQueueSize
    ) {
        this.threadPoolSize = threadPoolSize;
        this.taskQueueSize = taskQueueSize;
    }

    @Bean
    public Scheduler jdbcScheduler() {
        LOG.info("Creates a jdbcScheduler with thread pool size = {}", threadPoolSize);
        return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "jdbc-pool");
    }


    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(BookReviewServiceApplication.class, args);

        // Lee la propiedad spring.datasource.url desde el entorno o properties
        String mysqlUri = ctx.getEnvironment().getProperty("spring.datasource.url");
        // Loguea la URL de conexión a MySQL
        LOG.info("Connected to MySQL: " + mysqlUri);

        //Solo hacemos esto para loguear información
	}

}
