package com.dmob.cr.gui;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.dmob.launcher.model.Servers;
import com.dmob.launcher.network.Lists;
import com.dmob.cr.R;
import com.dmob.cr.gui.util.Utils;

import java.util.ArrayList;

public class Welcome {
    public Activity activity;
    public Animation animation;

    public ConstraintLayout constraintLayout;
    public Button mPlay;

    public TextView mTitle;
    public TextView mDescription;
    ArrayList<Servers> servers;

    public Welcome(Activity aactivity){
        activity = aactivity;
        animation = AnimationUtils.loadAnimation(aactivity, R.anim.button_click);
        servers = Lists.slist;
        
        try {
            constraintLayout = aactivity.findViewById(R.id.brp_welcome_main);
            
            if (constraintLayout != null) {
                mTitle = aactivity.findViewById(R.id.brp_welcome_title);
                mDescription = aactivity.findViewById(R.id.brp_welcome_desc);
                mPlay = aactivity.findViewById(R.id.brp_welcome_btn);
                
                if (mPlay != null) {
                    mPlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            hide(view);
                        }
                    });
                }
                
                if (mTitle != null) mTitle.animate().setDuration(0).translationXBy(-2000.0f).start();
                if (mDescription != null) mDescription.animate().setDuration(0).translationXBy(-2000.0f).start();
                if (mPlay != null) mPlay.animate().setDuration(0).translationXBy(-2000.0f).start();
                
                Utils.HideLayout(constraintLayout, false);
            }
        } catch (Exception e) {
            // Игнорируем ошибки инициализации
        }
    }

    public void show(boolean isRegister) {
        if (constraintLayout == null) return;
        
        Utils.ShowLayout(constraintLayout, true);
        
        if (mTitle != null && servers != null && !servers.isEmpty()) {
            mTitle.setText("ДОБРО ПОЖАЛОВАТЬ НА " + servers.get(0).getname());
            mTitle.animate().setDuration(1500).translationXBy(2000.0f).start();
        }
        
        if (mDescription != null) {
            mDescription.animate().setDuration(1500).setStartDelay(250).translationXBy(2000.0f).start();
        }
        
        if (mPlay != null) {
            mPlay.animate().setDuration(1500).setStartDelay(500).translationXBy(2000.0f).start();
        }
    }

    public void hide(View v) {
        if (constraintLayout == null) return;
        
        Utils.HideLayout(constraintLayout, true);
        
        if (v != null) {
            v.startAnimation(animation);
        }
        
        if (mPlay != null) {
            mPlay.animate().setDuration(1500).translationXBy(-2000.0f).start();
        }
        
        if (mDescription != null) {
            mDescription.animate().setDuration(1500).setStartDelay(250).translationXBy(-2000.0f).start();
        }
        
        if (mTitle != null) {
            mTitle.animate().setDuration(1500).setStartDelay(500).translationXBy(-2000.0f).start();
        }
    }
}