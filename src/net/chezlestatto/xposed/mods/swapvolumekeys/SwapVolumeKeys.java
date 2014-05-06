package net.chezlestatto.xposed.mods.swapvolumekeys;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import android.content.Context;
import android.view.Surface;
import android.view.WindowManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SwapVolumeKeys implements IXposedHookZygoteInit, IXposedHookLoadPackage {

	public static final String MY_PACKAGE_NAME = SwapVolumeKeys.class.getPackage().getName();
	public static final String MY_CLASS_NAME = SwapVolumeKeys.class.getSimpleName();
	private static XSharedPreferences prefs;

	@Override
	public void initZygote(StartupParam startupParam) {
		prefs = new XSharedPreferences(MY_PACKAGE_NAME);
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

		prefs.reload();

		if (lpparam.packageName.equals("android")) {
			int currentApiVersion = android.os.Build.VERSION.SDK_INT;
			if (currentApiVersion < 19) {
				findAndHookMethod("android.media.AudioService", lpparam.classLoader, "adjustMasterVolume", int.class, int.class, AudioService_adjustMasterVolume);
				findAndHookMethod("android.media.AudioService", lpparam.classLoader, "adjustSuggestedStreamVolume", int.class, int.class, int.class, AudioService_adjustSuggestedStreamVolume);
			} else {
				findAndHookMethod("android.media.AudioService", lpparam.classLoader, "adjustMasterVolume", int.class, int.class, String.class, AudioService_adjustMasterVolume);
				findAndHookMethod("android.media.AudioService", lpparam.classLoader, "adjustSuggestedStreamVolume", int.class, int.class, int.class, String.class, AudioService_adjustSuggestedStreamVolume);
			}
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
			log("Original direction = " + param.args[0]);
			int orientation = getScreenOrientation(param.thisObject);
			param.args[0] = orientation * (Integer) param.args[0];
			log("Modified direction = " + param.args[0]);
		}
	}

	private int getScreenOrientation(Object object) {
		boolean landscapeAsDefault = prefs.getBoolean("pref_landscape_mode_as_default", false);
		log("Landscape as default = " + landscapeAsDefault);
		Context context = (Context) getObjectField(object, "mContext");
		int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		int orientation = 1;
		switch (rotation) {
			case Surface.ROTATION_0:
				log("Rotation = 0");
				break;
			case Surface.ROTATION_90:
				log("Rotation = 90");
				orientation = (landscapeAsDefault ? 1 : -1);
				break;
			case Surface.ROTATION_180:
				log("Rotation = 180");
				orientation = -1;
				break;
			default: // Surface.ROTATION_270 
				log("Rotation = 270");
				orientation = (landscapeAsDefault ? -1 : 1);
				break;
		}
		return orientation;
	}

	private void log(String message) {
		boolean enableLogging = prefs.getBoolean("pref_enable_logging", false);
		if (enableLogging) {
			XposedBridge.log(MY_CLASS_NAME + ": " + message);
		}
	}

}
