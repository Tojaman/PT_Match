package com.solo.ptmatch.trainer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import com.solo.ptmatch.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "certifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certification  extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_profile_id", nullable = false)
    private TrainerProfile trainerProfile;

    @Column(nullable = false)
    private String name;

    @Column(name = "issuing_organization", nullable = false)
    private String issuingOrganization;

    @Column(name = "acquisition_date", nullable = false)
    private LocalDate acquisitionDate;

    private Certification(TrainerProfile trainerProfile, String name, String issuingOrganization, LocalDate acquisitionDate) {
        this.trainerProfile = trainerProfile;
        this.name = name;
        this.issuingOrganization = issuingOrganization;
        this.acquisitionDate = acquisitionDate;
    }

    public static Certification create(
            TrainerProfile trainerProfile,
            String name,
            String issuingOrganization,
            LocalDate acquisitionDate
    ) {
        return new Certification(trainerProfile, name, issuingOrganization, acquisitionDate);
    }
}
