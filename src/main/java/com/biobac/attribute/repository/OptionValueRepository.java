package com.biobac.attribute.repository;

import com.biobac.attribute.entity.AttributeDefinition;
import com.biobac.attribute.entity.OptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface OptionValueRepository extends JpaRepository<OptionValue, Long>, JpaSpecificationExecutor<OptionValue> {
    List<OptionValue> findAllByAttributeDefinition(AttributeDefinition def);

    boolean existsByIdAndAttributeDefinitionId(Long optionId, Long id);
}
