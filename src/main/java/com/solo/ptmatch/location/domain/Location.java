package com.solo.ptmatch.location.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "locations", indexes = @Index(name = "idx_location_name", columnList = "name"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType type;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 30)
    private String hint;

    @Column(length = 100)
    private String address;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    private Location(LocationType type, String name, String hint, String address, double latitude,
            double longitude) {
        this.type = type;
        this.name = name;
        this.hint = hint;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Location createSubway(String name, String hint, String address, double latitude,
            double longitude) {
        return new Location(LocationType.SUBWAY, name, hint, address, latitude, longitude);
    }

    public static Location createAddress(String name, String address, double latitude, double longitude) {
        return new Location(LocationType.ADDRESS, name, null, address, latitude, longitude);
    }
}
