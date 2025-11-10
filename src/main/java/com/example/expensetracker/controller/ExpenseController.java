package com.example.expensetracker.controller;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.service.ExpenseService;
import com.opencsv.CSVWriter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class ExpenseController {

    private final ExpenseService service;
    private final MeterRegistry registry;

    public ExpenseController(ExpenseService service, MeterRegistry registry) {
        this.service = service;
        this.registry = registry;
    }

    @GetMapping({"/", "/expenses"})
    public String expenses(Model model) {
        List<Expense> all = service.list();
        model.addAttribute("expenses", all);
        model.addAttribute("total", service.total());
        return "expenses";
    }

    @PostMapping("/expenses/add")
    public String addExpense(@RequestParam String name, @RequestParam double amount) {
        service.save(new Expense(name, amount));
        return "redirect:/expenses";
    }

    @PostMapping("/expenses/delete/{id}")
    public String deleteExpense(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/expenses";
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() throws Exception {
        List<Expense> all = service.list();
        StringWriter sw = new StringWriter();
        CSVWriter cw = new CSVWriter(sw);
        cw.writeNext(new String[] { "id", "name", "amount", "createdAt" });
        for (Expense e : all) {
            cw.writeNext(new String[] { String.valueOf(e.getId()), e.getName(), String.valueOf(e.getAmount()), e.getCreatedAt().toString() });
        }
        cw.close();
        byte[] out = sw.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expenses.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(out);
    }

    @GetMapping("/export/xlsx")
    public ResponseEntity<byte[]> exportXlsx() throws Exception {
        List<Expense> all = service.list();
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet s = wb.createSheet("expenses");
        Row header = s.createRow(0);
        header.createCell(0).setCellValue("id");
        header.createCell(1).setCellValue("name");
        header.createCell(2).setCellValue("amount");
        header.createCell(3).setCellValue("createdAt");
        int r = 1;
        for (Expense e : all) {
            Row row = s.createRow(r++);
            row.createCell(0).setCellValue(e.getId());
            row.createCell(1).setCellValue(e.getName());
            row.createCell(2).setCellValue(e.getAmount());
            row.createCell(3).setCellValue(e.getCreatedAt().toString());
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        wb.close();
        byte[] out = bos.toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expenses.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(out);
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }
}
