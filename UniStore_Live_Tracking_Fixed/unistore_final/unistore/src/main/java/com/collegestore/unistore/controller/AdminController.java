package com.collegestore.unistore.controller;

import com.collegestore.unistore.model.Order;
import com.collegestore.unistore.model.Product;
import com.collegestore.unistore.repository.OrderRepository;
import com.collegestore.unistore.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminController {
    @Autowired private OrderRepository orderRepo;
    @Autowired private ProductRepository productRepo;

    private boolean isAdmin(HttpSession session) {
        return "ADMIN".equalsIgnoreCase((String) session.getAttribute("user_role"));
    }

    @GetMapping({"/admin", "/admin/dashboard"})
    public String showAdminDashboard(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("orders", orderRepo.findAll());
        model.addAttribute("products", productRepo.findAll());
        model.addAttribute("statuses", java.util.List.of("PLACED","CONFIRMED","PACKED","READY","COMPLETED","CANCELLED"));
        model.addAttribute("pendingCount", orderRepo.findAll().stream().filter(o -> !"COMPLETED".equals(o.getStatus()) && !"CANCELLED".equals(o.getStatus())).count());
        return "admin_dashboard";
    }

    @PostMapping("/admin/products/add")
    public String addProduct(@RequestParam String name, @RequestParam Double price,
                             @RequestParam Integer stock, @RequestParam String imageUrl,
                             @RequestParam(defaultValue="Stationery") String category,
                             @RequestParam(defaultValue="College stationery item") String description,
                             HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        productRepo.save(new Product(name, price, stock, imageUrl, category, description));
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/products/{id}/stock")
    public String updateStock(@PathVariable Long id, @RequestParam Integer stock, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        productRepo.findById(id).ifPresent(p -> { p.setStock(Math.max(0, stock)); productRepo.save(p); });
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/orders/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        if (!java.util.Set.of("PLACED","CONFIRMED","PACKED","READY","COMPLETED","CANCELLED").contains(status)) {
            return "redirect:/admin/dashboard";
        }
        orderRepo.findById(id).ifPresent(o -> { o.setStatus(status); orderRepo.save(o); });
        return "redirect:/admin/dashboard";
    }
}
