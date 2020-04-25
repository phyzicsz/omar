/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.phyzicsz.omar;

import com.phyzicsz.omar.Introspecter.Visitor;
import com.phyzicsz.omar.testobjects.Address;
import com.phyzicsz.omar.testobjects.Person;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author phyzicsz <phyzics.z@gmail.com>
 */
public class OmarTest {

     Logger logger = LoggerFactory.getLogger(OmarTest.class);
     
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void testFieldVisitor() throws IOException {

        
        Person person = new Person();
        person.setFirstName("Alan");
        person.setLastName("Turing");
        
        Address address = new Address();
        address.setAddress("Bletchley Park");
        address.setCity("Bletchley");
        address.setZip("MK3 6EB");
        
        person.setAddress(address);
        
        Visitor visitor = new Visitor() {
            @Override
            public void onObject(Object o) {
                logger.info("onObject: {}", o.getClass().getTypeName());
            }

            @Override
            public void onField(Object o, Field field, Object value) {
                logger.info("onField: Parent:{} name:{} value:{}", o.getClass().getTypeName(), field.getName(), value.toString());
            }

           
           
        };

        Introspecter.traverse(person, visitor);

    }
}
