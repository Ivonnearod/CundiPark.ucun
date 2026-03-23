package com.grupo0.cundipark.controllers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
    
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
    
            if (statusCode == 404) {
                return "error-404";
            }
    
            model.addAttribute("errorType", statusCode);
        }
    
        model.addAttribute("errorMessage", message != null ? message.toString() : "Mensaje no disponible");
    
        // Por seguridad, solo mostrar el stack trace en el perfil de desarrollo
        if ("dev".equals(activeProfile) && exception != null && exception instanceof Exception) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ((Exception) exception).printStackTrace(pw);
            model.addAttribute("stackTrace", sw.toString());
        }
    
        model.addAttribute("errorTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    
        return "error";
    }
}