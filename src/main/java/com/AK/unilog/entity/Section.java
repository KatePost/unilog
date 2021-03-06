package com.AK.unilog.entity;

import com.AK.unilog.service.Semester;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sections")
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Semester semester;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    @Min(value = 10, message = "At least 10 seats must be available")
    @Max(value = 50, message = "No more than 50 seats may be available")
    private int seatsAvailable;

    @Column(nullable = false)
    @Min(2021)
    @Max(2100)
    private int year;

    @Column(nullable = false)
    private boolean disabled = false;


    public LocalDate getStartDate(){
        int month = switch (semester){
            case WINTER -> 1;
            case SPRING -> 4;
            case SUMMER -> 7;
            case FALL -> 10;
        };
        return LocalDate.of(this.year, month, 1);
    }

}
