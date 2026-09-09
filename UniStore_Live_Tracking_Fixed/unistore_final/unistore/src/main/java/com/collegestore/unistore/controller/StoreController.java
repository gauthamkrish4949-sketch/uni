package com.collegestore.unistore.controller;

import com.collegestore.unistore.model.*;
import com.collegestore.unistore.repository.*;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class StoreController {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private CartRepository cartRepo;

    @Autowired
    private ProductRepository productRepo;


    private Long userId(HttpSession s) {
        return (Long) s.getAttribute("user_id");
    }

    private boolean loggedIn(HttpSession s) {
        return userId(s) != null;
    }


    // =========================================================
    // STORE
    // =========================================================

    @GetMapping("/store")
    public String store(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long added,
            Model m,
            HttpSession s) {

        if (!loggedIn(s))
            return "redirect:/login";

        List<Product> products = productRepo.findAll();

        if (q != null && !q.isBlank()) {

            String x = q.trim().toLowerCase();

            products = products.stream()
                    .filter(p ->
                            (p.getName() != null &&
                                    p.getName()
                                            .toLowerCase()
                                            .contains(x))
                            ||
                            (p.getCategory() != null &&
                                    p.getCategory()
                                            .toLowerCase()
                                            .contains(x))
                            ||
                            (p.getDescription() != null &&
                                    p.getDescription()
                                            .toLowerCase()
                                            .contains(x))
                    )
                    .collect(Collectors.toList());
        }


        if (
                category != null
                &&
                !category.isBlank()
                &&
                !category.equalsIgnoreCase("ALL")
        ) {

            products = products.stream()
                    .filter(p ->
                            category.equalsIgnoreCase(
                                    p.getCategory()
                            )
                    )
                    .collect(Collectors.toList());
        }


        List<Cart> cartItems =
                cartRepo.findByUserId(userId(s));


        Set<Long> cartProductIds =
                cartItems.stream()
                        .map(Cart::getProductId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());


        m.addAttribute(
                "products",
                products
        );

        m.addAttribute(
                "query",
                q == null ? "" : q
        );

        m.addAttribute(
                "category",
                category == null ? "ALL" : category
        );

        m.addAttribute(
                "cart_count",
                cartItems.stream()
                        .mapToInt(Cart::getQuantity)
                        .sum()
        );

        m.addAttribute(
                "cartProductIds",
                cartProductIds
        );

        m.addAttribute(
                "addedProductId",
                added
        );


        return "store";
    }


    // =========================================================
    // PRODUCT
    // =========================================================

    @GetMapping("/product/{id}")
    public String product(
            @PathVariable Long id,
            Model m,
            HttpSession s) {

        if (!loggedIn(s))
            return "redirect:/login";


        return productRepo.findById(id)

                .map(p -> {

                    m.addAttribute(
                            "product",
                            p
                    );

                    return "product_detail";
                })

                .orElse(
                        "redirect:/store"
                );
    }


    // =========================================================
    // ADD TO CART
    // =========================================================

    @PostMapping("/add_to_cart/{id}")
    public String addToCart(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1")
            int quantity,
            HttpSession s) {

        Long uid = userId(s);

        if (uid == null)
            return "redirect:/login";


        quantity =
                Math.max(
                        1,
                        Math.min(quantity, 50)
                );


        Product p =
                productRepo
                        .findById(id)
                        .orElse(null);


        if (
                p == null
                ||
                p.getStock() <= 0
        )
            return "redirect:/store";


        Cart existing =
                cartRepo
                        .findByUserId(uid)
                        .stream()
                        .filter(
                                c ->
                                        id.equals(
                                                c.getProductId()
                                        )
                        )
                        .findFirst()
                        .orElse(null);


        if (existing == null) {

            cartRepo.save(
                    new Cart(
                            uid,
                            p.getId(),
                            p.getName(),
                            p.getPrice(),
                            Math.min(
                                    quantity,
                                    p.getStock()
                            )
                    )
            );

        } else {

            existing.setQuantity(
                    Math.min(
                            p.getStock(),
                            existing.getQuantity()
                                    + quantity
                    )
            );

            existing.setPrice(
                    p.getPrice()
            );

            existing.setProductName(
                    p.getName()
            );

            cartRepo.save(existing);
        }


        return "redirect:/store?added=" + id;
    }


    // =========================================================
    // UPDATE CART
    // =========================================================

    @PostMapping("/cart/update/{id}")
    public String update(
            @PathVariable Long id,
            @RequestParam int quantity,
            HttpSession s) {

        Long uid = userId(s);

        if (uid == null)
            return "redirect:/login";


        cartRepo.findById(id)
                .ifPresent(c -> {

                    if (uid.equals(c.getUserId())) {

                        if (quantity <= 0) {

                            cartRepo.delete(c);
                            return;
                        }


                        productRepo
                                .findById(
                                        c.getProductId()
                                )
                                .ifPresent(p -> {

                                    c.setQuantity(
                                            Math.min(
                                                    quantity,
                                                    p.getStock()
                                            )
                                    );

                                    c.setPrice(
                                            p.getPrice()
                                    );

                                    c.setProductName(
                                            p.getName()
                                    );

                                    cartRepo.save(c);
                                });
                    }
                });


        return "redirect:/cart";
    }


    // =========================================================
    // REMOVE CART ITEM
    // =========================================================

    @PostMapping("/cart/remove/{id}")
    public String remove(
            @PathVariable Long id,
            HttpSession s) {

        Long uid = userId(s);

        if (uid == null)
            return "redirect:/login";


        cartRepo.findById(id)
                .ifPresent(c -> {

                    if (
                            uid.equals(
                                    c.getUserId()
                            )
                    ) {
                        cartRepo.delete(c);
                    }
                });


        return "redirect:/cart";
    }


    // =========================================================
    // CART PAGE
    // =========================================================

    @GetMapping("/cart")
    public String cart(
            Model m,
            HttpSession s) {

        Long uid = userId(s);

        if (uid == null)
            return "redirect:/login";


        List<Cart> items =
                cartRepo.findByUserId(uid);


        double total =
                items.stream()
                        .mapToDouble(
                                i ->
                                        i.getPrice()
                                                * i.getQuantity()
                        )
                        .sum();


        m.addAttribute(
                "cart_items",
                items
        );

        m.addAttribute(
                "total",
                total
        );


        return "view_cart";
    }


    // =========================================================
    // UPI PAYMENT PAGE
    //
    // IMPORTANT:
    // We DO NOT reduce stock here.
    // We DO NOT clear the cart here.
    // Those happen only after payment screenshot upload.
    // =========================================================

    @PostMapping("/upi-payment")
    public String upiPayment(

            @RequestParam String studentName,

            @RequestParam String userClass,

            Model m,

            HttpSession s) {


        Long uid = userId(s);

        if (uid == null)
            return "redirect:/login";


        List<Cart> items =
                cartRepo.findByUserId(uid);


        if (items.isEmpty())
            return "redirect:/cart?error=Your+cart+is+empty";


        double total =
                items.stream()
                        .mapToDouble(
                                i ->
                                        i.getPrice()
                                                * i.getQuantity()
                        )
                        .sum();


        // Save checkout information in the session.
        s.setAttribute(
                "checkout_student_name",
                studentName == null
                        ? "Student"
                        : studentName.trim()
        );


        s.setAttribute(
                "checkout_user_class",
                userClass == null
                        ? "College"
                        : userClass.trim()
        );


        m.addAttribute(
                "studentName",
                studentName == null
                        ? "Student"
                        : studentName.trim()
        );


        m.addAttribute(
                "userClass",
                userClass == null
                        ? "College"
                        : userClass.trim()
        );


        m.addAttribute(
                "total",
                total
        );


        return "payment_checkout";
    }


    // =========================================================
    // STATIONERY CHECKOUT
    //
    // CAMPUS COUNTER:
    //     directly creates order
    //
    // UPI:
    //     requires payment screenshot
    //     then creates order
    // =========================================================

    @PostMapping("/checkout_stationery")
    @Transactional
    public String checkout(

            @RequestParam String studentName,

            @RequestParam String userClass,

            @RequestParam(
                    defaultValue = "PAY_AT_COUNTER"
            )
            String paymentMethod,

            @RequestParam(
                    required = false
            )
            MultipartFile paymentScreenshot,

            Model m,

            HttpSession s) {


        Long uid = userId(s);

        if (uid == null)
            return "redirect:/login";


        List<Cart> items =
                cartRepo.findByUserId(uid);


        if (items.isEmpty())
            return "redirect:/cart?error=Your+cart+is+empty";


        String normalizedPayment =
                paymentMethod == null
                        ? "PAY_AT_COUNTER"
                        : paymentMethod
                                .trim()
                                .toUpperCase(
                                        Locale.ROOT
                                );


        /*
         * =====================================================
         * UPI VALIDATION
         * =====================================================
         */

        if (
                "UPI_PAYMENT".equals(
                        normalizedPayment
                )
        ) {


            if (
                    paymentScreenshot == null
                    ||
                    paymentScreenshot.isEmpty()
            ) {

                return "redirect:/cart?error=Please+upload+your+UPI+payment+screenshot";
            }


            String original =
                    paymentScreenshot
                            .getOriginalFilename();


            if (original == null)
                original = "payment.jpg";


            String extension = "";

            int dot =
                    original.lastIndexOf('.');


            if (dot >= 0) {

                extension =
                        original
                                .substring(dot)
                                .toLowerCase(
                                        Locale.ROOT
                                );
            }


            if (
                    !List.of(
                            ".jpg",
                            ".jpeg",
                            ".png",
                            ".webp"
                    )
                    .contains(extension)
            ) {

                return "redirect:/cart?error=Please+upload+a+JPG,+PNG+or+WEBP+payment+screenshot";
            }
        }


        // =====================================================
        // VALIDATE COMPLETE CART BEFORE CHANGING STOCK
        // =====================================================

        for (Cart c : items) {

            Product p =
                    productRepo
                            .findById(
                                    c.getProductId()
                            )
                            .orElse(null);


            if (
                    p == null
                    ||
                    p.getStock()
                            < c.getQuantity()
            ) {

                return "redirect:/cart?error=Stock+changed.+Please+update+your+cart";
            }
        }


        // =====================================================
        // DECREASE STOCK AND BUILD ORDER DETAILS
        // =====================================================

        double total = 0;

        StringBuilder details =
                new StringBuilder();


        for (Cart c : items) {

            Product p =
                    productRepo
                            .findById(
                                    c.getProductId()
                            )
                            .orElseThrow();


            p.setStock(
                    p.getStock()
                            - c.getQuantity()
            );


            productRepo.save(p);


            total +=
                    p.getPrice()
                            * c.getQuantity();


            details
                    .append(
                            p.getName()
                    )
                    .append(" × ")
                    .append(
                            c.getQuantity()
                    )
                    .append("; ");
        }


        // =====================================================
        // CREATE ORDER
        // =====================================================

        Order o =
                new Order();


        o.setUserId(uid);


        o.setStudentName(
                studentName == null
                        ? "Student"
                        : studentName.trim()
        );


        o.setUserClass(
                userClass == null
                        ? "College"
                        : userClass.trim()
        );


        o.setOrderType(
                "STATIONERY"
        );


        o.setDetails(
                details.toString()
        );


        o.setTotalAmount(
                total
        );


        o.setPaymentMethod(
                normalizedPayment
        );


        o.setStatus(
                "PLACED"
        );


        // =====================================================
        // SAVE PAYMENT SCREENSHOT
        // =====================================================

        if (
                "UPI_PAYMENT".equals(
                        normalizedPayment
                )
        ) {

            String original =
                    paymentScreenshot
                            .getOriginalFilename();


            if (original == null)
                original = "payment.jpg";


            String extension = "";

            int dot =
                    original.lastIndexOf('.');


            if (dot >= 0) {

                extension =
                        original
                                .substring(dot)
                                .toLowerCase(
                                        Locale.ROOT
                                );
            }


            try {

                Path uploadDir =
                        Path.of(
                                "uploads",
                                "payments"
                        );


                Files.createDirectories(
                        uploadDir
                );


                String filename =
                        "payment_"
                                + uid
                                + "_"
                                + UUID.randomUUID()
                                + extension;


                Files.copy(

                        paymentScreenshot
                                .getInputStream(),

                        uploadDir.resolve(
                                filename
                        ),

                        StandardCopyOption
                                .REPLACE_EXISTING
                );


                o.setPaymentScreenshot(
                        filename
                );


            } catch (IOException ex) {

                throw new RuntimeException(
                        "Could not save payment screenshot",
                        ex
                );
            }
        }


        // =====================================================
        // SAVE ORDER
        // =====================================================

        o =
                orderRepo.saveAndFlush(o);


        // =====================================================
        // CLEAR CART
        // =====================================================

        cartRepo.deleteByUserId(uid);


        /*
         * Both payment methods now use the existing
         * order success page.
         */

        return "redirect:/order-success/"
                + o.getId();
    }


    // =========================================================
    // ORDER SUCCESS
    // =========================================================

    @GetMapping("/order-success/{id}")
    public String orderSuccess(
            @PathVariable Long id,
            Model m,
            HttpSession s) {


        Long uid = userId(s);

        if (uid == null)
            return "redirect:/login";


        Optional<Order> order =
                orderRepo.findById(id);


        if (
                order.isEmpty()
                ||
                !uid.equals(
                        order.get().getUserId()
                )
        ) {

            return "redirect:/orders";
        }


        m.addAttribute(
                "order",
                order.get()
        );


        return "order_success";
    }


    // =========================================================
    // ORDERS
    // =========================================================

    @GetMapping("/orders")
    public String orders(
            Model m,
            HttpSession s) {


        Long uid = userId(s);

        if (uid == null)
            return "redirect:/login";


        m.addAttribute(
                "orders",
                orderRepo
                        .findByUserIdOrderByIdDesc(
                                uid
                        )
        );


        return "orders";
    }


    // =========================================================
    // TRACK ORDER
    // =========================================================

    @GetMapping("/orders/{id}")
    public String track(
            @PathVariable Long id,
            Model m,
            HttpSession s) {


        Long uid = userId(s);

        if (uid == null)
            return "redirect:/login";


        Optional<Order> o =
                orderRepo.findById(id);


        if (
                o.isEmpty()
                ||
                !uid.equals(
                        o.get().getUserId()
                )
        ) {

            return "redirect:/orders";
        }


        m.addAttribute(
                "order",
                o.get()
        );


        m.addAttribute(
                "statusRank",
                statusRank(
                        o.get().getStatus()
                )
        );


        m.addAttribute(
                "statusLabel",
                statusLabel(
                        o.get().getStatus()
                )
        );


        return "track_order";
    }


    // =========================================================
    // ORDER STATUS API
    // =========================================================

    @GetMapping("/api/orders/{id}/status")
    @ResponseBody
    public Map<String, Object> orderStatus(
            @PathVariable Long id,
            HttpSession s) {


        Long uid = userId(s);


        if (uid == null) {

            return Map.of(
                    "authenticated",
                    false
            );
        }


        Optional<Order> o =
                orderRepo.findById(id);


        if (
                o.isEmpty()
                ||
                !uid.equals(
                        o.get().getUserId()
                )
        ) {

            return Map.of(
                    "authenticated",
                    true,
                    "found",
                    false
            );
        }


        return Map.of(

                "authenticated",
                true,

                "found",
                true,

                "status",
                o.get().getStatus() == null
                        ? "PLACED"
                        : o.get().getStatus(),

                "statusRank",
                statusRank(
                        o.get().getStatus()
                ),

                "statusLabel",
                statusLabel(
                        o.get().getStatus()
                )
        );
    }


    // =========================================================
    // STATUS RANK
    // =========================================================

    private int statusRank(
            String status) {

        if (status == null)
            return 1;


        return switch (
                status.toUpperCase(
                        Locale.ROOT
                )
        ) {

            case "PLACED" ->
                    1;

            case "CONFIRMED" ->
                    2;

            case "PACKED" ->
                    3;

            case "READY",
                 "READY_FOR_PICKUP" ->
                    4;

            case "COMPLETED" ->
                    5;

            case "CANCELLED" ->
                    0;

            default ->
                    1;
        };
    }


    // =========================================================
    // STATUS LABEL
    // =========================================================

    private String statusLabel(
            String status) {

        if (status == null)
            return "PLACED";


        return switch (
                status.toUpperCase(
                        Locale.ROOT
                )
        ) {

            case "READY_FOR_PICKUP" ->
                    "READY";

            default ->
                    status.toUpperCase(
                            Locale.ROOT
                    );
        };
    }
}
