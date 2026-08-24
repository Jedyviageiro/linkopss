package com.linkops.media.domain;

import com.linkops.common.domain.BaseEntity;
import com.linkops.service.domain.ServiceOffering;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "service_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_offering_id", nullable = false)
    private ServiceOffering serviceOffering;

    @Column(nullable = false, length = 2048)
    private String url;

    public ServiceImage(ServiceOffering serviceOffering, String url) {
        this.serviceOffering = serviceOffering;
        this.url = url;
    }
}
