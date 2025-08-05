package com.dmob.launcher.activity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.dmob.launcher.Preferences;
import com.dmob.Settings;
import com.dmob.launcher.adapter.FaqInfo;
import com.dmob.launcher.model.News;
import com.dmob.launcher.model.Servers;
import com.dmob.launcher.model.ServersResponse;
import com.dmob.launcher.network.Interface;
import com.dmob.launcher.network.Lists;
import com.dmob.launcher.utils.LogHelper;
import com.dmob.launcher.utils.Utils;
import com.dmob.cr.BuildConfig;
import com.dmob.cr.gui.util.Damp;
import com.dmob.cr.R;

import java.util.Timer;
import java.util.TimerTask;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SplashActivity extends AppCompatActivity{
    public static ArrayList<Servers> slist;
    private ConstraintLayout splash;
    private LinearLayout privacy;
    private TextView privacyLink;
    private Button privacyAccept;
    private Button privacyDecline;
    private View disclaimerView;

    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Инициализируем компоненты
        disclaimerView = findViewById(R.id.disclaimer_include);
        LinearLayout privacy = (LinearLayout) findViewById(R.id.brp_launcher_privacy);
        Button privacyOk = (Button) privacy.findViewById(R.id.brp_launcher_privacy_ok);
        Button privacyDecline = (Button) privacy.findViewById(R.id.brp_launcher_privacy_decline);
        TextView privacyLink = (TextView) privacy.findViewById(R.id.brp_launcher_privacy_link);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.btn_click);

        // Инициализируем логгер
        LogHelper.init(getApplicationContext());
        LogHelper.i(LogHelper.TAG_MAIN, "Приложение запущено");
        LogHelper.i(LogHelper.TAG_MAIN, "Powered by " + BuildConfig.AUTHOR);

        // Показываем дисклеймер с анимацией
        if (disclaimerView != null) {
            // Показываем дисклеймер с анимацией
            disclaimerView.setAlpha(0f);
            disclaimerView.setVisibility(View.VISIBLE);
            disclaimerView.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setListener(null);
            
            // Устанавливаем таймер для скрытия дисклеймера и показа основных элементов
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        // Скрываем дисклеймер с анимацией
                        disclaimerView.animate()
                                .alpha(0f)
                                .setDuration(500)
                                .setListener(new android.animation.AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(android.animation.Animator animation) {
                                        disclaimerView.setVisibility(View.GONE);
                                        
                                        // Показываем элементы сплэш-экрана
                                        showSplashElements();
                                        
                                        // Проверяем, первый ли запуск приложения
                                        if (Preferences.getString(getApplicationContext(), Preferences.FIRST_START).isEmpty()) {
                                            Utils.ShowView(privacy, true);
                                        } else if(isOnline(SplashActivity.this)) {
                                            Timer t = new Timer();
                                            t.schedule(new TimerTask(){
                                                @Override
                                                public void run() {
                                                    DoIt();
                                                }
                                            }, 500L);
                                        } else {
                                            ConstraintLayout notInternetConnection = (ConstraintLayout) findViewById(R.id.brp_launcher_not_internet);
                                            Utils.ShowView(notInternetConnection, true);
                                        }
                                    }
                                });
                    });
                }
            }, 3000); // Показываем дисклеймер 3 секунды
        } else {
            // Если по какой-то причине view дисклеймера не найден
            showSplashElements();
            
            // Стандартная логика запуска
        if (Preferences.getString(getApplicationContext(), Preferences.FIRST_START).isEmpty()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.ShowView(privacy, true);
                }
            });
        } else if(isOnline(this)) {
            runOnUiThread(() -> {
                Timer t = new Timer();
                t.schedule(new TimerTask(){
                    @Override
                    public void run() {
                        DoIt();
                    }
                }, 500L);
            });
        } else {
            ConstraintLayout notInternetConnection = (ConstraintLayout) findViewById(R.id.brp_launcher_not_internet);
            Utils.ShowView(notInternetConnection, true);
        }
        }
        
        // Существующие обработчики...
        privacyOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Preferences.putString(getApplicationContext(), Preferences.FIRST_START, "true");
                Utils.HideView(privacy, true);

                Timer t = new Timer();
                t.schedule(new TimerTask(){
                    @Override
                    public void run() {
                        DoIt();
                    }
                }, 500L);
            }
        });
        privacyDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.HideView(privacy, true);
                finish();
            }
        });
        privacyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse(Settings.privacyLink)));
            }
        });
    }
    
    /**
     * Показывает элементы сплэш-экрана с анимацией
     */
    private void showSplashElements() {
        // Находим элементы сплэш-экрана
        View logoBg = findViewById(R.id.logo_bg);
        View logo = findViewById(R.id.imageView7);
        View loadingText = findViewById(R.id.textView14);
        View progressBar = findViewById(R.id.progressBar);
        
        // Показываем каждый элемент с анимацией
        if (logoBg != null) {
            logoBg.setVisibility(View.VISIBLE);
            logoBg.setAlpha(0f);
            logoBg.animate().alpha(0.7f).setDuration(700).setStartDelay(100);
        }
        
        if (logo != null) {
            logo.setVisibility(View.VISIBLE);
            logo.setAlpha(0f);
            logo.animate().alpha(1f).setDuration(700).setStartDelay(300);
        }
        
        if (loadingText != null) {
            loadingText.setVisibility(View.VISIBLE);
            loadingText.setAlpha(0f);
            loadingText.animate().alpha(1f).setDuration(700).setStartDelay(500);
        }
        
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setAlpha(0f);
            progressBar.animate().alpha(1f).setDuration(700).setStartDelay(600);
        }
    }

    private void DoIt() {
        Lists.slist = new ArrayList<>();
        Lists.nlist = new ArrayList<>();
        Lists.flist = new ArrayList<>();

        LogHelper.addSeparator(LogHelper.TAG_MAIN, "ИНИЦИАЛИЗАЦИЯ ДАННЫХ");
        LogHelper.i(LogHelper.TAG_MAIN, "Начало инициализации данных приложения");
        
        // Используем новый метод с логированием
        Interface sInterface = Interface.createWithLogs("http://vbd.fdv.dd/");
        
        LogHelper.i(LogHelper.TAG_API, "Запрос списка серверов...");
        Call<ServersResponse> scall = sInterface.getServers();

        scall.enqueue(new Callback<ServersResponse>() {
            @Override
            public void onResponse(Call<ServersResponse> call, Response<ServersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Servers> servers = response.body().getServersList();
                    LogHelper.i(LogHelper.TAG_API, "Успешно получен список серверов: " + servers.size() + " серверов");
                    
                    for (Servers server : servers) {
                        LogHelper.d(LogHelper.TAG_API, "Сервер: " + server.getname() + " | IP: " + server.getIP() + 
                                " | Онлайн: " + server.getOnline() + "/" + server.getmaxOnline() + 
                                " | Тех. работы: " + (server.getMaintenance() ? "Да" : "Нет") +
                                " | Тип: " + server.getServerType());
                        
                        Lists.slist.add(new Servers(
                            server.getIP(), 
                            server.getPORT(), 
                            server.getx2(), 
                            server.getname(), 
                            server.getOnline(), 
                            server.getmaxOnline(),
                            server.getMaintenance(),
                            server.getMaintenanceText(),
                            server.getServerType(),
                            server.getServerNameColor(),
                            server.getServerBackgroundColor()
                        ));
                    }
                } else {
                    LogHelper.e(LogHelper.TAG_API, "Ошибка получения списка серверов: " + response.code() + " " + response.message());
                    Damp.isCorrupted = true;
                    Toasty.warning(getApplicationContext(), "Не удалось получить список серверов. Код: " + response.code(), 
                            Toast.LENGTH_SHORT, true).show();
                }
            }

            @Override
            public void onFailure(Call<ServersResponse> call, Throwable t) {
                Damp.isCorrupted = true;
                LogHelper.e(LogHelper.TAG_API, "Ошибка при запросе к serversn.php: " + t.getMessage(), t);
                Toasty.warning(getApplicationContext(), "Не удалось подключиться к серверам: " + t.getMessage(), 
                        Toast.LENGTH_SHORT, true).show();
            }
        });

        LogHelper.i(LogHelper.TAG_API, "Запрос новостей...");
        Call<List<News>> ncall = sInterface.getNews();
        ncall.enqueue(new Callback<List<News>>() {
            @Override
            public void onResponse(Call<List<News>> call, Response<List<News>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<News> news = response.body();
                    LogHelper.i(LogHelper.TAG_API, "Успешно получены новости: " + news.size() + " новостей");
                    
                    for (News storie : news) {
                        LogHelper.d(LogHelper.TAG_API, "Новость: " + storie.getTitle() + " | URL изображения: " + storie.getImageUrl() +
                                   " | Кнопка: " + storie.getButton() + " | Ссылка: " + storie.getLink());
                        Lists.nlist.add(new News(storie.getImageUrl(), storie.getTitle(), storie.getButton(), storie.getLink()));
                    }
                } else {
                    LogHelper.e(LogHelper.TAG_API, "Ошибка получения новостей: " + response.code() + " " + response.message());
                    Damp.isCorrupted = true;
                    Toasty.warning(getApplicationContext(), "Не удалось получить новости. Код: " + response.code(), 
                            Toast.LENGTH_SHORT, true).show();
                }
            }

            @Override
            public void onFailure(Call<List<News>> call, Throwable t) {
                Damp.isCorrupted = true;
                LogHelper.e(LogHelper.TAG_API, "Ошибка при запросе к news.php: " + t.getMessage(), t);
                Toasty.warning(getApplicationContext(), "Не удалось загрузить новости: " + t.getMessage(), 
                        Toast.LENGTH_SHORT, true).show();
            }
        });
        
        // Загрузка FAQ с сервера
        LogHelper.i(LogHelper.TAG_API, "Запрос FAQ...");
        Call<List<FaqInfo>> fcall = sInterface.getFaq();
        fcall.enqueue(new Callback<List<FaqInfo>>() {
            @Override
            public void onResponse(Call<List<FaqInfo>> call, Response<List<FaqInfo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FaqInfo> faqList = response.body();
                    LogHelper.i(LogHelper.TAG_API, "Успешно получен FAQ: " + faqList.size() + " вопросов");
                    
                    for (FaqInfo faq : faqList) {
                        LogHelper.d(LogHelper.TAG_API, "FAQ: " + faq.getCaption());
                        Lists.flist.add(new FaqInfo(faq.getCaption(), faq.getText()));
                    }
                } else {
                    LogHelper.e(LogHelper.TAG_API, "Ошибка получения FAQ: " + response.code() + " " + response.message());
                    // Не отображаем ошибку пользователю, так как FAQ не критичен для работы приложения
                }
            }

            @Override
            public void onFailure(Call<List<FaqInfo>> call, Throwable t) {
                LogHelper.e(LogHelper.TAG_API, "Ошибка при запросе к faq.php: " + t.getMessage(), t);
                // Не отображаем ошибку пользователю, так как FAQ не критичен для работы приложения
            }
        });

        runOnUiThread(() -> {
            Timer t = new Timer();
            t.schedule(new TimerTask(){
                @Override
                public void run() {
                    // Удалена проверка отключения лаунчера
                    LogHelper.i(LogHelper.TAG_MAIN, "Таймер запуска: запуск основной активности");
                    if(Utils.isGameInstalled())
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    else
                        startActivity(new Intent(getApplicationContext(), LoaderActivity.class));
                    finish();
                }
            }, 3000L); // Увеличиваем задержку до 3 секунд для уверенности, что проверка статуса завершится
        });
    }

    public static boolean isOnline(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
        {
            return true;
        }
        return false;
    }
}