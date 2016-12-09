package com.zaprii.myapplication;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()) // parsing using Gson
                .build();

        showProgressDialog();
        GitHubService service = retrofit.create(GitHubService.class);
        service.listRepos("alexeyzapriy")
                .doOnError(throwable -> showErrorDialog("Connection Failed"))
                .doOnCompleted(this::hideProgressDialog)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(repos -> {
                    List<Repo> ownRepos = Stream.of(repos)
                            .filter(repo -> !repo.fork)
                            .collect(Collectors.toList());
                });
    }

    private void showProgressDialog() {
    }

    private void hideProgressDialog() {
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(MainActivity.this)
                .setPositiveButton("Retry", (dialogInterface, i) -> { /*retry*/ })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .setMessage(message)
                .show();
    }
}
