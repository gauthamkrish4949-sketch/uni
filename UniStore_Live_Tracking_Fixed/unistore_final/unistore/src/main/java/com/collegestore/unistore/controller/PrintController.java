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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Controller
public class PrintController {

    @Autowired
    private OrderRepository orderRepository;

    private final String UPLOAD_DIR =
            System.getProperty("user.dir")
            + "/uploads/print_docs/";


    private Long userId(HttpSession session) {

        return (Long)
                session.getAttribute("user_id");
    }


    @GetMapping("/print")
    public String printPage(
            HttpSession session) {

        return userId(session) == null
                ? "redirect:/login"
                : "print_service";
    }


    @PostMapping("/upload_print_job")
    public String processPrintJob(

            @RequestParam String studentName,

            @RequestParam String userClass,

            @RequestParam int pages,

            @RequestParam String printType,

            @RequestParam MultipartFile printFile,

            @RequestParam(
                defaultValue = "PAY_AT_COUNTER"
            )
            String paymentMethod,

            HttpSession session,

            Model model)

            throws IOException {


        Long uid = userId(session);

        if (uid == null)
            return "redirect:/login";


        if (pages < 1 || pages > 1000) {

            model.addAttribute(
                "error",
                "Pages must be between 1 and 1000."
            );

            return "print_service";
        }


        if (printFile.isEmpty()) {

            model.addAttribute(
                "error",
                "Please upload a document."
            );

            return "print_service";
        }


        String original =
                new File(
                    printFile.getOriginalFilename()
                ).getName();


        String lower =
                original.toLowerCase(
                    Locale.ROOT
                );


        if (
            !lower.endsWith(".pdf")
            &&
            !lower.endsWith(".doc")
            &&
            !lower.endsWith(".docx")
        ) {

            model.addAttribute(
                "error",
                "Only PDF, DOC and DOCX files are allowed."
            );

            return "print_service";
        }


        double rate =
                "COLOR".equalsIgnoreCase(printType)
                ? 10.0
                : 2.0;


        double total =
                pages * rate;


        String normalizedPayment =
                paymentMethod == null
                ? "PAY_AT_COUNTER"
                : paymentMethod
                    .trim()
                    .toUpperCase(
                        Locale.ROOT
                    );


        String filename =
                UUID.randomUUID()
                + "_"
                + original;


        new File(UPLOAD_DIR).mkdirs();


        printFile.transferTo(
            new File(
                UPLOAD_DIR + filename
            )
        );


        /*
         * UPI PAYMENT
         */

        if (
            "UPI_PAYMENT"
            .equals(normalizedPayment)
        ) {

            session.setAttribute(
                "print_checkout_student_name",
                studentName.trim()
            );


            session.setAttribute(
                "print_checkout_user_class",
                userClass.trim()
            );


            session.setAttribute(
                "print_checkout_pages",
                pages
            );


            session.setAttribute(
                "print_checkout_type",
                printType.toUpperCase(
                    Locale.ROOT
                )
            );


            session.setAttribute(
                "print_checkout_file",
                filename
            );


            model.addAttribute(
                "studentName",
                studentName.trim()
            );


            model.addAttribute(
                "userClass",
                userClass.trim()
            );


            model.addAttribute(
                "pages",
                pages
            );


            model.addAttribute(
                "printType",
                printType.toUpperCase(
                    Locale.ROOT
                )
            );


            model.addAttribute(
                "total",
                total
            );


            return "print_payment";
        }


        /*
         * CAMPUS COUNTER
         */

        return createPrintOrder(

            uid,

            studentName,

            userClass,

            pages,

            printType,

            filename,

            "PAY_AT_COUNTER",

            null,

            session
        );
    }


    @PostMapping("/checkout_print_upi")
    public String checkoutPrintUpi(

            @RequestParam MultipartFile paymentScreenshot,

            HttpSession session,

            Model model) {


        Long uid =
                userId(session);


        if (uid == null)
            return "redirect:/login";


        String studentName =
                (String)
                session.getAttribute(
                    "print_checkout_student_name"
                );


        String userClass =
                (String)
                session.getAttribute(
                    "print_checkout_user_class"
                );


        Integer pages =
                (Integer)
                session.getAttribute(
                    "print_checkout_pages"
                );


        String printType =
                (String)
                session.getAttribute(
                    "print_checkout_type"
                );


        String documentFile =
                (String)
                session.getAttribute(
                    "print_checkout_file"
                );


        if (
            studentName == null
            ||
            userClass == null
            ||
            pages == null
            ||
            printType == null
            ||
            documentFile == null
        ) {

            return "redirect:/print";
        }


        if (
            paymentScreenshot == null
            ||
            paymentScreenshot.isEmpty()
        ) {

            addPrintPaymentModel(
                model,
                studentName,
                userClass,
                pages,
                printType
            );

            model.addAttribute(
                "error",
                "Please upload your UPI payment screenshot."
            );

            return "print_payment";
        }


        String original =
                paymentScreenshot
                    .getOriginalFilename();


        if (original == null)
            original = "payment.jpg";


        String extension = "";

        int dot =
                original.lastIndexOf('.');


        if (dot >= 0)
            extension =
                original
                    .substring(dot)
                    .toLowerCase(
                        Locale.ROOT
                    );


        if (
            !List.of(
                ".jpg",
                ".jpeg",
                ".png",
                ".webp"
            ).contains(extension)
        ) {

            addPrintPaymentModel(
                model,
                studentName,
                userClass,
                pages,
                printType
            );

            model.addAttribute(
                "error",
                "Please upload a JPG, PNG or WEBP payment screenshot."
            );

            return "print_payment";
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


            String paymentFilename =
                    "payment_print_"
                    + uid
                    + "_"
                    + UUID.randomUUID()
                    + extension;


            Files.copy(

                paymentScreenshot
                    .getInputStream(),

                uploadDir.resolve(
                    paymentFilename
                ),

                StandardCopyOption
                    .REPLACE_EXISTING
            );


            return createPrintOrder(

                uid,

                studentName,

                userClass,

                pages,

                printType,

                documentFile,

                "UPI_PAYMENT",

                paymentFilename,

                session
            );


        } catch (IOException ex) {

            addPrintPaymentModel(
                model,
                studentName,
                userClass,
                pages,
                printType
            );

            model.addAttribute(
                "error",
                "Could not save the payment screenshot. Please try again."
            );

            return "print_payment";
        }
    }


    private String createPrintOrder(

            Long uid,

            String studentName,

            String userClass,

            int pages,

            String printType,

            String filename,

            String paymentMethod,

            String paymentScreenshot,

            HttpSession session) {


        double rate =
                "COLOR".equalsIgnoreCase(
                    printType
                )
                ? 10.0
                : 2.0;


        Order order =
                new Order();


        order.setUserId(uid);

        order.setStudentName(
            studentName.trim()
        );

        order.setUserClass(
            userClass.trim()
        );

        order.setOrderType(
            "PRINT"
        );

        order.setDetails(
            "Pages: "
            + pages
            + " | Type: "
            + printType
        );

        order.setDocumentFile(
            filename
        );

        order.setTotalAmount(
            pages * rate
        );

        order.setPaymentMethod(
            paymentMethod
        );

        order.setPaymentScreenshot(
            paymentScreenshot
        );

        order.setStatus(
            "PLACED"
        );


        orderRepository.save(order);


        clearPrintCheckout(
            session
        );


        return "redirect:/order-success/"
                + order.getId();
    }


    private void addPrintPaymentModel(

            Model model,

            String studentName,

            String userClass,

            int pages,

            String printType) {


        model.addAttribute(
            "studentName",
            studentName
        );

        model.addAttribute(
            "userClass",
            userClass
        );

        model.addAttribute(
            "pages",
            pages
        );

        model.addAttribute(
            "printType",
            printType
        );

        model.addAttribute(
            "total",

            pages *
            (
                "COLOR".equalsIgnoreCase(
                    printType
                )
                ? 10.0
                : 2.0
            )
        );
    }


    private void clearPrintCheckout(
            HttpSession session) {

        session.removeAttribute(
            "print_checkout_student_name"
        );

        session.removeAttribute(
            "print_checkout_user_class"
        );

        session.removeAttribute(
            "print_checkout_pages"
        );

        session.removeAttribute(
            "print_checkout_type"
        );

        session.removeAttribute(
            "print_checkout_file"
        );
    }
}
