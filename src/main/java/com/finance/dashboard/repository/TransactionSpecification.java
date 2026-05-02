package com.finance.dashboard.repository;
import com.finance.dashboard.model.Transaction;
import com.finance.dashboard.model.enums.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
public class TransactionSpecification {
    private TransactionSpecification() {}
    public static Specification<Transaction> withFilters(TransactionType type, String category, LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (type != null) predicates.add(cb.equal(root.get("type"), type));
            if (category != null && !category.isBlank()) predicates.add(cb.like(cb.lower(root.get("category")), "%" + category.toLowerCase() + "%"));
            if (startDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("date"), startDate));
            if (endDate != null) predicates.add(cb.lessThanOrEqualTo(root.get("date"), endDate));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
