package org.itson.ecommerce_e2.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class PersistenceProducer {
    private static final String PERSISTENCE_UNIT = "ecommerce_pu";
    private EntityManagerFactory emf;

    @PostConstruct
    public void init() {
        try {
            Dotenv dotenv = Dotenv.load();
            
            String endpoint = dotenv.get("DB_ENDPOINT");
            String user = dotenv.get("DB_USER");
            String password = dotenv.get("DB_PASSWORD");

            String url = "jdbc:mysql://" + endpoint + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            Map<String, String> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.url", url);
            props.put("jakarta.persistence.jdbc.user", user);
            props.put("jakarta.persistence.jdbc.password", password);

            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, props);
            
            System.out.println("conexion con mysql");
        } catch (Exception e) {
            throw e;
        }
    }

    @PreDestroy
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        if (emf == null || !emf.isOpen()) {
            throw new IllegalStateException(
                    "[JPA] EntityManagerFactory no disponible. "
                    + "Verifica que @PostConstruct se ejecutó sin errores."
            );
        }
        return emf.createEntityManager();
    }

    public void closeEntityManager(@Disposes EntityManager em) {
        if (em != null && em.isOpen()) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }
}
