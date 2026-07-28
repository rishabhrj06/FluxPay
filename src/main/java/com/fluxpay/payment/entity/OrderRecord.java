package com.fluxpay.payment.entity;

import com.fluxpay.common.entity.BaseEntity;
import com.fluxpay.common.entity.Money;
import com.fluxpay.common.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "order_record",
        indexes = {
                @Index(name = "idx_order_record_id_merchant_id", columnList = "id, merchant_id"),
                @Index(name = "idx_order_record_merchant_id", columnList = "merchant_id")
        }
)
public class OrderRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "merchant_id")
    private UUID merchantId;

    @Column(length = 100)
    private String receipt; //order id from merchant's backend

    @Embedded
    private Money amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @JdbcTypeCode((SqlTypes.JSON))
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> notes;

    private LocalDateTime expiresAt;
}
