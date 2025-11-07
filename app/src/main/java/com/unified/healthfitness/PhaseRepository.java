package com.unified.healthfitness;

import android.app.Application;

class PhaseRepository {

    private PhaseDao mPhaseDao;

    PhaseRepository(Application application) {
        PeriodsAppDatabase db = PeriodsAppDatabase.getDatabase(application);
        mPhaseDao = db.phaseDao();
    }

    Phase findByDay(int day) {
        return mPhaseDao.findByDay(day);
    }

    void insert(Phase phase) {
        PeriodsAppDatabase.databaseWriteExecutor.execute(() -> {
            mPhaseDao.insertAll(phase);
        });
    }
}
