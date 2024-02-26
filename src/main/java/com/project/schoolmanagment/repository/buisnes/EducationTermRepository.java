package com.project.schoolmanagment.repository.buisnes;

import com.project.schoolmanagment.entity.concretes.buisnes.EducationTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationTermRepository extends JpaRepository<EducationTerm, Long> {
}