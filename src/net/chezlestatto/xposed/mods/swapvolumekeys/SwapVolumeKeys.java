package net.chezlestatto.xposed.mods.swapvolumekeys;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SwapVolumeKeys implements IXposedHookZygoteInit, IXposedHookLoadPackage {

	public static final String MY_PACKAGE_NAME = SwapVolumeKeys.class.getPackage().getName();
	private static XSharedPreferences prefs;

	@Override
	public void initZygote(StartupParam startupParam) {
		prefs = new XSharedPreferences(MY_PACKAGE_NAME);
	}
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

		prefs.reload();
		
		if (lpparam.packageName.equals("android")) {
			findAndHookMethod("android.media.AudioService", lpparam.classLoader, "adjustMasterVolume", int.class, int.class, AudioService_adjustMasterVolume);
			findAndHookMethod("android.media.AudioService", lpparam.classLoader, "adjustSuggestedStreamVolume", int.class, int.class, int.class, AudioService_adjustSuggestedStreamVolume);
		}

	}

	public final XC_MethodHook AudioService_adjustMasterVolume = new XC_MethodHook() {

		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			hook(param);
		}

	};

	public final XC_MethodHook AudioService_adjustSuggestedStreamVolume = new XC_MethodHook() {

		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			hook(param);
		}

	};

	private void hook(MethodHookParam param) {
		if ((Integer) param.args[0] != 0) {
			int orientation = getScreenOrientation(param.thisObject);
			if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
				param.args[0] = -1 * (Integer) param.args[0];
			}
		}
	}

	private int getScreenOrientation(Object object) {
		Context context = (Context) getObjectField(object, "mContext");
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		int orientation = Configuration.ORIENTATION_UNDEFINED;
		if (metrics.widthPixels <= metrics.heightPixels) {
			orientation = Configuration.ORIENTATION_PORTRAIT;
		} else {
			orientation = Configuration.ORIENTATION_LANDSCAPE;
		}
		return orientation;
	}

}