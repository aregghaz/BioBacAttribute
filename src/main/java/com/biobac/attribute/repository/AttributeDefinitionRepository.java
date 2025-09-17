package com.biobac.attribute.repository;

import com.biobac.attribute.entity.AttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, Long>, JpaSpecificationExecutor<AttributeDefinition> {
    AttributeDefinition findAttributeDefinitionByName(String name);

    @Query("SELECT a FROM AttributeDefinition a JOIN a.groups g WHERE g.id IN :groupIds")
    List<AttributeDefinition> findByGroups(@Param("groupIds") List<Long> groupIds);
}
