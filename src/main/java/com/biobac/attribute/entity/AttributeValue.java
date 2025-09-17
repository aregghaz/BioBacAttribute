package com.biobac.attribute.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "attribute_value",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"definition_id", "target_type", "target_id"})
        })
public class AttributeValue extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "definition_id")
    private AttributeDefinition definition;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private AttributeTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "value", length = 2048)
    private String value;
}
