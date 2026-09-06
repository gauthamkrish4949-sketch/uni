package com.collegestore.unistore.controller;

import com.collegestore.unistore.model.User;
import com.collegestore.unistore.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {
    @Autowired private UserRepository userRepo;

    @GetMapping("/")
    public String home() { return "redirect:/login"; }

    @GetMapping("/login")
    public String showLoginForm() { return "login"; }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username,
                              @RequestParam String password,
                              HttpSession session, Model model) {
        Optional<User> userOpt = userRepo.findByUsername(username.trim());
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            User user = userOpt.get();
            if (!user.isAuthorized()) {
                model.addAttribute("error", "Your account is pending admin approval.");
                return "login";
            }
            session.setAttribute("currentUser", user);
            session.setAttribute("user_id", user.getId());
            session.setAttribute("user_role", user.getRole());
            return "ADMIN".equalsIgnoreCase(user.getRole())
                    ? "redirect:/admin/dashboard" : "redirect:/store";
        }
        model.addAttribute("error", "Invalid username or password.");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
