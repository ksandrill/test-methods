package org.nsu.fit.tm_backend.manager;

import org.slf4j.Logger;
import org.nsu.fit.tm_backend.repository.IRepository;
import org.nsu.fit.tm_backend.repository.data.PlanPojo;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlanManager extends ParentManager {
    public PlanManager(IRepository dbService, Logger flowLog) {
        super(dbService, flowLog);
    }

    /**
     * Метод создает новый объект типа Plan. Ограничения:
     * name - длина не больше 128 символов и не меньше 2 включительно не содержит спец символов. Имена не пересекаются друг с другом;
     * details - длина не больше 1024 символов и не меньше 1 включительно;
     * fee - больше либо равно 0 но меньше либо равно 5000.
     */
    public PlanPojo createPlan(PlanPojo plan) {
        return dbService.createPlan(plan);
    }

    public void deletePlan(UUID id) {
        dbService.deletePlan(id);
    }

    /**
     * Возвращает список планов доступных для покупки или список всех планов в случае если customerId = null.
     */
    public List<PlanPojo> getPlans(UUID customerId) {
        List<UUID> usedPlanIds = customerId == null
                ? Collections.emptyList()
                : dbService.getSubscriptions(customerId).stream()
                    .map(s -> s.planId)
                    .collect(Collectors.toList());

        return dbService.getPlans().stream()
                .filter(plan -> !usedPlanIds.contains(plan.id))
                .collect(Collectors.toList());
    }
}
