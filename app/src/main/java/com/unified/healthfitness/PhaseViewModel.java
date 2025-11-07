package com.unified.healthfitness;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class PhaseViewModel extends AndroidViewModel {

    private PhaseRepository mRepository;

    private final MutableLiveData<Phase> mPhase = new MutableLiveData<>();

    public PhaseViewModel(Application application) {
        super(application);
        mRepository = new PhaseRepository(application);
    }

    public LiveData<Phase> getPhase() {
        return mPhase;
    }

    public void findPhaseByDay(int day) {
        PeriodsAppDatabase.databaseWriteExecutor.execute(() -> {
            mPhase.postValue(mRepository.findByDay(day));
        });
    }
}
