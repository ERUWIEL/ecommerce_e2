package org.itson.ecommerce_e2.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

@ApplicationScoped
public class MinIOClientProducer {

    private MinioClient minioClient;
    private String productsBucket;

    @PostConstruct
    public void init() {
        Dotenv dotenv = Dotenv.load();

        String endpoint = dotenv.get("MINIO_ENDPOINT");
        String accessKey = dotenv.get("MINIO_ACCESS_KEY");
        String secretKey = dotenv.get("MINIO_SECRET_KEY");
        productsBucket = dotenv.get("MINIO_BUCKET_PRODUCTS");

        try {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            ensureBucketExists(productsBucket);

            System.out.println("conexion con MinIO");
        } catch (Exception e) {
            minioClient = null;
        }

    }

    @PreDestroy
    public void destroy() {
        minioClient = null;
    }

    @Produces
    @ApplicationScoped
    public MinioClient produceMinioClient() {
        if (minioClient == null) {
            throw new IllegalStateException(
                    "[MinIO] El cliente no está disponible. "
                    + "Revisa las variables MINIO_* en tu .env y que MinIO esté corriendo."
            );
        }
        return minioClient;
    }

    @Produces
    @Named("productsBucket")
    public String produceProductsBucket() {
        return productsBucket;
    }

    private void ensureBucketExists(String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
        );
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        } else {

        }
    }
}
