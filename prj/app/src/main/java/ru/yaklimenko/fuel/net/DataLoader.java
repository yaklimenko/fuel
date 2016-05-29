package ru.yaklimenko.fuel.net;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.yaklimenko.fuel.Constants;
import ru.yaklimenko.fuel.db.entities.FillingStation;
import ru.yaklimenko.fuel.dto.FillingStationsWrapper;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Антон on 28.05.2016.
 * this class will load info from server
 */
public class DataLoader {

    public static final String TAG = DataLoader.class.getSimpleName();

    /**
     * request fillingStations from server
     * can be run in main thread
     * @return list of filling stations if success
     */
    @Nullable
    public void requestFillingStations(
            final OnFillingStationsGotListener fillingStationsGotListener
    ) {
        String url = Constants.SERVER_URL + "/" + Constants.PREFIX + "/" +
                FillingStation.REQUEST_URL;
        final Request req = new Request.Builder()
                .addHeader("Content-Type", "application/json")
                .url(url)
                .build();

        Observable.create(new Observable.OnSubscribe<String>(){
            OkHttpClient client = new OkHttpClient();
            @Override
            public void call(Subscriber<? super String> subscriber) {
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
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {/*empty*/}
                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String response) {
                        List<FillingStation> fillingStations = null;
                            fillingStations = parseStations(response);

                        fillingStationsGotListener.onFillingStationsGot(fillingStations);
                    }
                });
    }

    private List<FillingStation> parseStations(String string) {
        Gson gson = new Gson();
        FillingStationsWrapper stationsWrapper = gson
                .fromJson(string, FillingStationsWrapper.class);
        List<FillingStation> stations = new ArrayList<>();
        for (FillingStation station : stationsWrapper.objects) {
            stations.add(station);
        }
        return stations;
    }

    public interface OnFillingStationsGotListener {
        void onFillingStationsGot(List<FillingStation> fillingStations);
    }

}
