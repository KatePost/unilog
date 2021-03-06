package com.AK.unilog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.Period;

@Entity
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
@Table(name = "registered_courses")
public class RegisteredCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "payment_id", nullable = true)
    private PaymentRecord paymentRecord;

    @Column(nullable = false)
    @NotNull
    private LocalDate dueDate;

    public RegisteredCourse() {
    }

    public RegisteredCourse(Long id, Section section, User user, PaymentRecord paymentRecord, LocalDate dueDate) {
        this.id = id;
        this.section = section;
        this.user = user;
        this.paymentRecord = paymentRecord;
        this.dueDate = dueDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PaymentRecord getPaymentRecord() {
        return paymentRecord;
    }

    public void setPaymentRecord(PaymentRecord paymentRecord) {
        this.paymentRecord = paymentRecord;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isOverDue(){
        if(paymentRecord != null){
            return false;
        }
        return dueDate.isBefore(LocalDate.now());
    }

    public double getFee(){
        double initialPrice = section.getCourse().getPrice();
        //if it's paid, return 0
        if(paymentRecord != null){
            return 0.00;
        }
        //if it's not due yet, return the course price
        if(!this.isOverDue()){
            return initialPrice;
        }
        // if it's overdue, calculate the interest - 5%/month
        Period months = Period.between(dueDate, LocalDate.now());
        int monthsOverdue = months.getMonths();
        return initialPrice * (1 + (0.05 * (double) monthsOverdue));
    }
}
