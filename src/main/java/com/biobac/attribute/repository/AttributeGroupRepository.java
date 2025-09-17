package com.biobac.attribute.repository;

import com.biobac.attribute.entity.AttributeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AttributeGroupRepository extends JpaRepository<AttributeGroup, Long>, JpaSpecificationExecutor<AttributeGroup> {
}
