package com.collegestore.unistore;

import com.collegestore.unistore.model.Product;
import com.collegestore.unistore.model.User;
import com.collegestore.unistore.repository.ProductRepository;
import com.collegestore.unistore.repository.UserRepository;
import org.springframework.boot.*;import org.springframework.boot.autoconfigure.SpringBootApplication;import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UnistoreApplication {
 public static void main(String[] args){SpringApplication.run(UnistoreApplication.class,args);}
 @Bean CommandLineRunner initData(UserRepository users,ProductRepository products){return args->{
   if(users.findByUsername("admin").isEmpty())users.save(new User("admin","admin123","ADMIN",true));
   if(users.findByUsername("student").isEmpty())users.save(new User("student","student123","STUDENT",true));
   if(products.count()==0){
    products.save(new Product("A4 Assignment Sheets",60.0,50,"📄","Paper","Premium 50-sheet A4 assignment paper pack."));
    products.save(new Product("Blue Gel Pen",10.0,100,"🖊️","Writing","Smooth-flow blue gel pen for notes, records and exams."));
    products.save(new Product("Lab Record Book",45.0,30,"📘","Books","College lab record notebook with quality ruled pages."));
    products.save(new Product("Sticky Notes",35.0,40,"📝","Accessories","Compact sticky notes for reminders and study planning."));
    products.save(new Product("Graph Book",30.0,45,"📒","Books","Graph paper notebook for engineering drawing and maths."));
    products.save(new Product("Black Marker",25.0,35,"🖍️","Writing","Bold black marker for labels, charts and presentations."));
    products.save(new Product("File Folder",40.0,25,"📁","Accessories","Durable folder for assignments and important documents."));
    products.save(new Product("Exam Pad",55.0,20,"📋","Accessories","Strong writing pad suitable for examinations."));
   }
 };}
}
