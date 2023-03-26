package com.esife.Batch.Single.Transaction.configuration;

import com.esife.Batch.Single.Transaction.model.Customer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;

public class CustomerItemProcessor implements ItemProcessor<Customer, Customer> {


    private static final Logger logger = LogManager.getLogger(CustomerItemProcessor.class);

    @Override
    public Customer process(Customer customer) throws Exception {
        // Do any data processing you need here
        // System.out.println(customer.getId());
        return customer;
    }
}