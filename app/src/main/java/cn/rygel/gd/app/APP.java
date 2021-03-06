package cn.rygel.gd.app;

import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.ftinc.scoop.Scoop;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.mmkv.MMKV;

import cn.rygel.gd.BuildConfig;
import cn.rygel.gd.R;
import cn.rygel.gd.db.boxstore.BoxStoreHolder;
import cn.rygel.gd.db.entity.MyObjectBox;
import io.objectbox.android.AndroidObjectBrowser;
import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.unit.Subunits;

// TODO: 2019/1/29 编辑事件以及删除事件
// TODO: 2019/1/29 修改图标
public class APP extends MultiDexApplication {

    private static APP sInstance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        // 初始化Logger
        initLogger();
        // 初始化BoxStore
        initBoxStore();
        // 初始化内存泄漏工具
        initLeakCanary();
        // 初始化换肤框架
        initScoop();
        // 初始化屏幕适配规则
        initAutoSize();
        // 初始化MMKV
        initMMKV();
        initMultiDex();
    }

    /**
     * 初始化Logger
     */
    private void initLogger(){
        Logger.addLogAdapter(
                new AndroidLogAdapter(
                        PrettyFormatStrategy.newBuilder()
                                .methodCount(5)
                                .tag("Rygel")
                                .build()
                )
        );
    }

    /**
     * 初始化BoxStore
     */
    private void initBoxStore(){
        BoxStoreHolder.getInstance()
                .init(MyObjectBox.builder()
                        .androidContext(this)
                        .build()
                );
        if(BuildConfig.DEBUG){
            new AndroidObjectBrowser(
                    BoxStoreHolder
                            .getInstance()
                            .getBoxStore()
            ).start(this);
        }
    }

    /**
     * 初始化内存泄漏检测工具
     */
    private void initLeakCanary(){
        if(BuildConfig.DEBUG){
            if (LeakCanary.isInAnalyzerProcess(this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return;
            }
            LeakCanary.install(this);
        }
    }

    /**
     * 初始化换肤工具
     */
    private void initScoop(){
        Scoop.waffleCone()
                .addFlavor("Default", R.style.Theme_Scoop,true)
                .addFlavor("Light",R.style.Theme_Scoop_Light)
                .addDayNightFlavor("DayNight",R.style.Theme_Scoop_DayNight)
                .setSharedPreferences(PreferenceManager.getDefaultSharedPreferences(this))
                .initialize();
    }

    /**
     * 初始化MMVKV
     */
    private void initMMKV(){
        String dir = MMKV.initialize(this);
        Logger.i("MMKV root : " + dir);
    }

    /**
     * 初始化AutoSize
     */
    private void initAutoSize(){
        AutoSizeConfig.getInstance().getUnitsManager()
                .setSupportDP(false)
                .setSupportSP(false)
                .setSupportSubunits(Subunits.MM);
    }

    /**
     * 初始化MultiDex，避免在5.0以下机型崩溃
     */
    private void initMultiDex(){
        MultiDex.install(this);
    }

    /**
     * 获取APP的实例
     * @return 返回Application的实例
     */
    public static APP getInstance(){
        return sInstance;
    }

}
