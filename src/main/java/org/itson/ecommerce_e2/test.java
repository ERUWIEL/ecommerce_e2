
package org.itson.ecommerce_e2;

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
        PersistenceProducer pd = new PersistenceProducer();
        pd.init();
        
        MinIOClientProducer mp = new MinIOClientProducer();
        mp.init();
    }
    
}
