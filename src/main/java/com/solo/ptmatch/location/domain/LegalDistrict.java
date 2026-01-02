package com.solo.ptmatch.location.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * 법정동 경계 데이터 엔티티
 * 국토정보플랫폼(http://data.nsdi.go.kr/)에서 다운로드한 법정동 경계 데이터를 저장
 */
@Entity
@Getter
@Table(name = "legal_districts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LegalDistrict {

    @Id
    @Column(length = 10)
    private String code; // 법정동 코드 (예: 1168010100)

    @Column(nullable = false, length = 100)
    private String name; // 법정동 이름 (예: 서울특별시 강남구 역삼1동)

    @Column(columnDefinition = "geometry(MultiPolygon, 4326)")
    private MultiPolygon boundary; // 법정동 경계 폴리곤

    public LegalDistrict(String code, String name, MultiPolygon boundary) {
        this.code = code;
        this.name = name;
        this.boundary = boundary;
    }

    public static LegalDistrict create(String code, String name, MultiPolygon boundary) {
        return new LegalDistrict(code, name, boundary);
    }

    public Double getCentroidLatitude() {
        if (boundary == null)
            return null;
        return boundary.getCentroid().getY();
    }

    public Double getCentroidLongitude() {
        if (boundary == null)
            return null;
        return boundary.getCentroid().getX();
    }
}
