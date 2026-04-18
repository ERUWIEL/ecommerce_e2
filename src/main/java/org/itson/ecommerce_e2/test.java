package org.itson.ecommerce_e2;

import io.minio.ListObjectsArgs;
import io.minio.Result;
import io.minio.messages.Item;
import org.itson.ecommerce_e2.config.MinIOClientProducer;
import org.itson.ecommerce_e2.config.PersistenceProducer;

/**
 *
 * @author gatog
 */
public class test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
//        PersistenceProducer pd = new PersistenceProducer();
//        pd.init();
//        pd.destroy();

        MinIOClientProducer mp = new MinIOClientProducer();
        mp.init();
        
        try {
            Iterable<Result<Item>> results = mp.produceMinioClient().listObjects(
                    ListObjectsArgs.builder().bucket(mp.produceProductsBucket()).build());

            System.out.println("--- Archivos en el bucket ---");

            for (Result<Item> result : results) {
                Item item = result.get(); // Aquí se obtiene el objeto real
                System.out.println("Nombre: " + item.objectName() + " | Tamaño: " + item.size() + " bytes");
            }

        } catch (Exception e) {
            System.err.println("Error al listar: " + e.getMessage());
        }

        mp.destroy();
    }

}
