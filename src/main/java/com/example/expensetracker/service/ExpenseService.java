package com.example.expensetracker.service;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {
    private final ExpenseRepository repo;

    public ExpenseService(ExpenseRepository repo) {
        this.repo = repo;
    }

    public Expense save(Expense e) { return repo.save(e); }
    public List<Expense> list() { return repo.findAll(); }
    public void delete(Long id) { repo.deleteById(id); }
    public double total() {
        return repo.findAll().stream().mapToDouble(Expense::getAmount).sum();
    }
}
