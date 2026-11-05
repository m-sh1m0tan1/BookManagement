package com.example.bookmanagement.controller;

import com.example.bookmanagement.auth.CustomUserDetails;
import com.example.bookmanagement.dto.DueDateRequest;
import com.example.bookmanagement.dto.LoanRequest;
import com.example.bookmanagement.dto.LoanWithBookDetail;
import com.example.bookmanagement.entity.Loan;
import com.example.bookmanagement.service.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public ResponseEntity<List<LoanWithBookDetail>> getLoans(@RequestParam(defaultValue = "false") boolean overdue) {
        return ResponseEntity.ok(loanService.getLoanWithBookDetails(overdue));
    }

    @GetMapping("/me")
    public ResponseEntity<List<LoanWithBookDetail>> getMyLoans(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(loanService.getLoanWithBookDetailByUserId(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable int id, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(loanService.getLoanById(id, principal.getId(), principal.isAdmin()));
    }

    @PostMapping
    public ResponseEntity<Loan> insertLoan(@RequestBody LoanRequest request,
                                           @AuthenticationPrincipal CustomUserDetails principal,
                                           UriComponentsBuilder uriComponentsBuilder) {
        Loan loan = new Loan();
        loan.setBorrowedUser(principal.getId());
        loan.setBorrowedBook(request.getBookId());
        Loan insertedLoan = loanService.insertLoan(loan, principal.getId());
        URI location = uriComponentsBuilder.path("/api/loans/{id}").buildAndExpand(insertedLoan.getId()).toUri();

        return ResponseEntity.created(location).body(insertedLoan);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Loan> returnLoan(@PathVariable int id, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(loanService.updateReturnedAtById(id, principal.getId()));
    }

    @PutMapping("/{id}/due-date")
    public ResponseEntity<Loan> extendLoan(@PathVariable int id,
                                           @RequestBody DueDateRequest request,
                                           @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(loanService.updateDueDateById(id, request.getDueDate(), principal.getId()));
    }
}
