package com.collegestore.unistore.controller;

import com.collegestore.unistore.model.Order;
import com.collegestore.unistore.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
public class PrintController {
    @Autowired private OrderRepository orderRepository;
    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/print_docs/";

    private Long userId(HttpSession session) { return (Long) session.getAttribute("user_id"); }

    @GetMapping("/print")
    public String printPage(HttpSession session) {
        return userId(session) == null ? "redirect:/login" : "print_service";
    }

    @PostMapping("/upload_print_job")
    public String processPrintJob(@RequestParam String studentName,
                                  @RequestParam String userClass,
                                  @RequestParam int pages,
                                  @RequestParam String printType,
                                  @RequestParam MultipartFile printFile,
                                  HttpSession session, Model model) throws IOException {
        Long uid = userId(session);
        if (uid == null) return "redirect:/login";
        if (pages < 1 || pages > 1000) {
            model.addAttribute("error", "Pages must be between 1 and 1000.");
            return "print_service";
        }
        if (printFile.isEmpty()) {
            model.addAttribute("error", "Please upload a document.");
            return "print_service";
        }

        String original = new File(printFile.getOriginalFilename()).getName();
        String lower = original.toLowerCase();
        if (!(lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx"))) {
            model.addAttribute("error", "Only PDF, DOC and DOCX files are allowed.");
            return "print_service";
        }

        double rate = "COLOR".equalsIgnoreCase(printType) ? 10.0 : 2.0;
        String filename = UUID.randomUUID() + "_" + original;
        new File(UPLOAD_DIR).mkdirs();
        printFile.transferTo(new File(UPLOAD_DIR + filename));

        Order order = new Order();
        order.setUserId(uid);
        order.setStudentName(studentName);
        order.setUserClass(userClass);
        order.setOrderType("PRINT");
        order.setDetails("Pages: " + pages + " | Type: " + printType);
        order.setDocumentFile(filename);
        order.setTotalAmount(pages * rate);
        order.setStatus("PLACED");
        orderRepository.save(order);

        return "redirect:/order-success/" + order.getId();
    }
}
