package com.solo.ptmatch.trainer.domain;

import com.solo.ptmatch.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "trainer_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainerImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trainer_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_profile_id", nullable = false)
    private TrainerProfile trainerProfile;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    private TrainerImage(TrainerProfile trainerProfile, String imageUrl, int displayOrder) {
        this.trainerProfile = trainerProfile;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }

    public static TrainerImage create(TrainerProfile trainerProfile, String imageUrl, int displayOrder) {
        return new TrainerImage(trainerProfile, imageUrl, displayOrder);
    }

    public void assignTrainerProfile(TrainerProfile trainerProfile) {
        this.trainerProfile = trainerProfile;
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
