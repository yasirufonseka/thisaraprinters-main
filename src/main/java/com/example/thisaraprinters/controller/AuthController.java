package com.example.thisaraprinters.controller;

import com.example.thisaraprinters.model.QuotationModel;
import com.example.thisaraprinters.repository.CustomerRepo;
import com.example.thisaraprinters.repository.ProductionRepo;
import com.example.thisaraprinters.repository.QuotationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private QuotationRepo quotationRepo;

    @Autowired
    private ProductionRepo productionRepo;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/dashboard")
    public ModelAndView dashboard(Authentication authentication) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("dashboard");

        if (authentication != null) {
            mav.addObject("username", authentication.getName());
            mav.addObject("authorities", authentication.getAuthorities());
        }

        // Real stats from DB
        long totalOrders = quotationRepo.count();
        double totalRevenue = quotationRepo.sumTotalRevenue();
        long activeJobs = productionRepo.count();
        long totalCustomers = customerRepo.count();

        // Build a simple revenue trend for the last 7 days
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        List<QuotationModel> recentQuotes = quotationRepo.findByQuotationdateBetween(startDate, endDate);

        Map<LocalDate, Double> revenueByDate = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = startDate.plusDays(i);
            revenueByDate.put(day, 0.0);
        }
        for (QuotationModel quote : recentQuotes) {
            if (quote.getQuotationdate() != null) {
                LocalDate date = quote.getQuotationdate();
                if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                    revenueByDate.put(date, revenueByDate.getOrDefault(date, 0.0) + quote.getQuotationamount());
                }
            }
        }

        List<String> revenueChartLabels = new ArrayList<>();
        List<Double> revenueChartData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        revenueByDate.forEach((date, value) -> {
            revenueChartLabels.add(date.format(formatter));
            revenueChartData.add(value);
        });

        mav.addObject("totalOrders", totalOrders);
        mav.addObject("totalRevenue", totalRevenue);
        mav.addObject("activeJobs", activeJobs);
        mav.addObject("totalCustomers", totalCustomers);
        mav.addObject("revenueChartLabels", revenueChartLabels);
        mav.addObject("revenueChartData", revenueChartData);

        return mav;
    }
}
