package org.nsu.fit.tm_backend.manager;

import org.slf4j.Logger;
import org.nsu.fit.tm_backend.repository.IRepository;

public class ParentManager {
    protected IRepository dbService;
    protected Logger log;

    public ParentManager(IRepository dbService, Logger log) {
        this.dbService = dbService;
        this.log = log;
    }
}
