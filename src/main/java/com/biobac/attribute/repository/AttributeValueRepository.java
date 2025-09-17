package com.biobac.attribute.repository;

import com.biobac.attribute.entity.AttributeDefinition;
import com.biobac.attribute.entity.AttributeTargetType;
import com.biobac.attribute.entity.AttributeValue;
import com.biobac.attribute.entity.OptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    boolean existsByDefinition(AttributeDefinition definition);

    @Query("SELECT CASE WHEN COUNT(av) > 0 THEN true ELSE false END FROM AttributeValue av WHERE av.definition = :definition AND :option MEMBER OF av.definition.options")
    boolean existsByDefinitionAndOption(@Param("definition") AttributeDefinition definition, @Param("option") OptionValue option);


    List<AttributeValue> findByTargetTypeAndTargetId(AttributeTargetType resolvedType, Long targetId);

    Optional<AttributeValue> findByDefinitionIdAndTargetTypeAndTargetId(Long id, AttributeTargetType resolvedType, Long targetId);
}
