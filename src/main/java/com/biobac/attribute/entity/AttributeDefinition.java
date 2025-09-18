package com.biobac.attribute.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class AttributeDefinition extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttributeDataType dataType;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "attribute_group_definitions",
            joinColumns = @JoinColumn(name = "attribute_definition_id"),
            inverseJoinColumns = @JoinColumn(name = "attribute_group_id"))
    private Set<AttributeGroup> groups = new HashSet<>();

    @OneToMany(mappedBy = "attributeDefinition")
    private Set<OptionValue> options = new HashSet<>();
}
