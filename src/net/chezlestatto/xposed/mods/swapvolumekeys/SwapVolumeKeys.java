package net.chezlestatto.xposed.mods.swapvolumekeys;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.app.Instrumentation;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SwapVolumeKeys implements IXposedHookLoadPackage {

	private static final int FLAG_FROM_XPOSED = 0x2000000;
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

		if (lpparam.packageName.equals("android")) {
			findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", lpparam.classLoader, "interceptKeyBeforeQueueing", android.view.KeyEvent.class, Integer.TYPE, Boolean.TYPE, PhoneWindowManager_interceptKeyBeforeQueueing);
		}

	}

	public final XC_MethodHook PhoneWindowManager_interceptKeyBeforeQueueing = new XC_MethodHook() {

    	@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

			if (getScreenOrientation(param.thisObject) == Configuration.ORIENTATION_LANDSCAPE) {

				KeyEvent keyEvent = (KeyEvent) param.args[0];
				// If already sent by Xposed, do nothing...
				if (!keyEventIsFromXposed(keyEvent)) {

					int keyCode = keyEvent.getKeyCode(); 
					switch (keyCode) {
					
						case KeyEvent.KEYCODE_VOLUME_DOWN: 
						case KeyEvent.KEYCODE_VOLUME_UP:
							sendKeyEvent(keyEvent.getAction(), (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ? KeyEvent.KEYCODE_VOLUME_UP : KeyEvent.KEYCODE_VOLUME_DOWN), keyEvent.getFlags());
							param.setResult(0);
							break;
					
					}
				}

			}
		
		}

    };

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
    
	private final boolean keyEventIsFromXposed(KeyEvent keyEvent) {
		return (keyEvent.getFlags() & FLAG_FROM_XPOSED) != 0;
	}    
    
    private void sendKeyEvent(int action, int keyCode, int flags) {
		KeyEvent keyEvent = new KeyEvent(action, keyCode);
		// Flagged the key event as sent by Xposed
		keyEvent = KeyEvent.changeFlags(keyEvent, flags | FLAG_FROM_XPOSED);
		Instrumentation instrumentation = new Instrumentation();
		instrumentation.sendKeySync(keyEvent);
    }
    
}
