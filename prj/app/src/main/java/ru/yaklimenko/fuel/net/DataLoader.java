package ru.yaklimenko.fuel.net;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.j256.ormlite.table.TableUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.yaklimenko.fuel.Constants;
import ru.yaklimenko.fuel.FuelApplicationPreferences;
import ru.yaklimenko.fuel.db.DbHelperManager;
import ru.yaklimenko.fuel.db.dao.FillingStationDao;
import ru.yaklimenko.fuel.db.dao.FuelCategoryDao;
import ru.yaklimenko.fuel.db.dao.FuelDao;
import ru.yaklimenko.fuel.db.entities.FillingStation;
import ru.yaklimenko.fuel.db.entities.Fuel;
import ru.yaklimenko.fuel.db.entities.FuelCategory;
import ru.yaklimenko.fuel.dto.FillingStationsWrapper;
import ru.yaklimenko.fuel.dto.FuelCategoriesWrapper;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Антон on 28.05.2016.
 * this class will load info from server
 */
public class DataLoader {

    public static final String TAG = DataLoader.class.getSimpleName();
    Context context;


    public void getStations (
            Context context,
            final OnDataGotListener dataGotListener
    ) {
        //decide what to do
        this.context = context;

        if (!new FuelApplicationPreferences(context).areStationsActual()) {
            //get from server
            requestFillingStations(dataGotListener);
        } else {
            //or just read fom db
            dataGotListener.onDataLoaded(FillingStationDao.getInstance().queryForAll());
        }
    }

    /**
     * request fillingStations from server
     * must be run in main thread
     */
    @Nullable
    private void requestFillingStations(
            final OnDataGotListener dataGotListener
    ) {
        String url = Constants.SERVER_URL + "/" + Constants.PREFIX + "/" +
                FillingStation.REQUEST_URL;
        final Request stationsReq = new Request.Builder()
                .addHeader("Content-Type", "application/json").url(url).build();
        url = Constants.SERVER_URL + "/" + Constants.PREFIX + "/" +
                FuelCategory.REQUEST_URL;
        final Request categoriesReq = new Request.Builder()
                .addHeader("Content-Type", "application/json").url(url).build();


        Observable.create(new Observable.OnSubscribe<String>(){
            @Override
            public void call(Subscriber<? super String> subscriber) {
                fireRequest(categoriesReq, subscriber);
                fireRequest(stationsReq, subscriber);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    private int responsesReceived = 0;

                    @Override
                    public void onCompleted() {
                        new FuelApplicationPreferences(context).updateStationsLastCheckTime();
                    }
                    @Override
                    public void onError(Throwable e) {
                        dataGotListener.onError(e);
                    }

                    @Override
                    public void onNext(String response) {
                        if (responsesReceived == 0) {
                            rewriteCategories(response);
                            responsesReceived++;
                        } else if (responsesReceived == 1) {
                            rewriteFillingStationsInDb(response, new OnFillingStationsStoredListener() {
                                @Override
                                public void onStationsStored(List<FillingStation> fillingStations) {
                                    new FuelApplicationPreferences(context).updateStationsLastUpdateTime();
                                    dataGotListener.onDataLoaded(fillingStations);
                                }
                            });

                        }

                    }
                });
    }

    /**
     * must be run NOT in main thread
     */
    private void fireRequest(Request req, Subscriber<? super String> subscriber) {
        OkHttpClient client = new OkHttpClient();
        Response response = null;
        try {
            response = client.newCall(req).execute();
        } catch (IOException e) {
            Log.e(TAG, "call: seems we have connection problems", e);
            subscriber.onError(e);
        }
        if (response != null && response.isSuccessful()) {
            try {
                subscriber.onNext(response.body().string());
            } catch (IOException e) {
                Log.e(TAG, "call: ", e);
            }
        }
    }

    private void rewriteFillingStationsInDb(
            final String stationsResponse, final OnFillingStationsStoredListener listener
    ) {
        Action1<List<FillingStation>> onNextAction = new Action1<List<FillingStation>>() {
            @Override
            public void call(List<FillingStation> fillingStations) {
                listener.onStationsStored(fillingStations);
            }
        };

        Observable.create(new Observable.OnSubscribe<List<FillingStation>>() {
            @Override
            public void call(Subscriber<? super List<FillingStation>> subscriber) {
                List<FillingStation> fillingSTations = parseStations(stationsResponse);
                rewriteFillingStationsInDb(fillingSTations);
                subscriber.onNext(fillingSTations);
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNextAction);
    }

    private void rewriteFillingStationsInDb(List<FillingStation> fillingStations) {
        clearStationAndFuelsTable();

        List<Fuel> fuelsToStore = new ArrayList<>();
        for (FillingStation stationsToStore : fillingStations) {
            if (stationsToStore.name != null) {
                stationsToStore.name = stationsToStore.name.trim();
            }
            if (stationsToStore.address != null) {
                stationsToStore.address = stationsToStore.address.trim();
            }
            if (stationsToStore.fuels != null && stationsToStore.fuels.length > 0) {
                for (Fuel fuelToStore : stationsToStore.fuels) {
                    fuelToStore.stationId = stationsToStore.id;
                    fuelsToStore.add(fuelToStore);
                }
            }
        }
        FillingStationDao.getInstance().createAll(fillingStations);
        FuelDao.getInstance().createAll(fuelsToStore);

    }

    private void clearStationAndFuelsTable () {
        try {
            TableUtils.clearTable(DbHelperManager.getHelper().getConnectionSource(), FillingStation.class);
            TableUtils.clearTable(DbHelperManager.getHelper().getConnectionSource(), Fuel.class);
        } catch (SQLException e) {
            throw new IllegalStateException("cannot clear stations");
        }
    }

    private List<FillingStation> parseStations(String string) {
        Gson gson = new Gson();
        FillingStationsWrapper stationsWrapper = gson
                .fromJson(string, FillingStationsWrapper.class);
        List<FillingStation> stations = new ArrayList<>();
        Collections.addAll(stations, stationsWrapper.objects);
        return stations;
    }

    private void rewriteCategories(final String catResponse) {
        Observable.create(new Observable.OnSubscribe<List<FuelCategory>>() {
            @Override
            public void call(Subscriber<? super List<FuelCategory>> subscriber) {
                List<FuelCategory> fuelCategories = parseCategories(catResponse);
                rewriteCategories(fuelCategories);
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private void rewriteCategories(List<FuelCategory> fuelCategories) {
        clearCategoriesTable();
        for (FuelCategory fuelCategory : fuelCategories) {
            if (fuelCategory.name != null) {
                fuelCategory.name = fuelCategory.name.trim();
            }
        }
        FuelCategoryDao.getInstance().createAll(fuelCategories);
    }

    private void clearCategoriesTable() {
        try {
            TableUtils.clearTable(
                    DbHelperManager.getHelper().getConnectionSource(), FuelCategory.class
            );
        } catch (SQLException e) {
            throw new IllegalStateException("cannot clear Fuel category table", e);
        }
    }

    private List<FuelCategory> parseCategories(String string) {
        Gson gson = new Gson();
        FuelCategoriesWrapper catWrapper = gson
                .fromJson(string, FuelCategoriesWrapper.class);
        List<FuelCategory> categories = new ArrayList<>();
        Collections.addAll(categories, catWrapper.objects);
        return categories;
    }




    public interface OnDataGotListener {
        void onDataLoaded(List<FillingStation> fillingStations);
        void onError(Throwable e);
    }

    public interface OnFillingStationsStoredListener {
        void onStationsStored(List<FillingStation> fillingStations);
    }
}
