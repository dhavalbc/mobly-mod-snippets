
package com.google.android.mobly.snippet.bundled;

import static org.junit.Assert.assertEquals;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import com.google.android.mobly.snippet.Snippet;
import com.google.android.mobly.snippet.rpc.Rpc;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Demonstrates how to drive an app using UIAutomator without access to the app's source code or
 * classpath.
 *
 * <p>Drives the Espresso example app from ex2 without instrumenting it.
 */
public class UiAutomatorSnippet implements Snippet {
    private static final class UiAutomatorSnippetException extends Exception {
        private static final long serialVersionUID = 1;

        public UiAutomatorSnippetException(String message) {
            super(message);
        }
    }

    private static final String MAIN_PACKAGE = "com.google.android.mobly.snippet.example2";
    private static final int LAUNCH_TIMEOUT = 5000;

    private final Context mContext;
    private final UiDevice mDevice;

    public UiAutomatorSnippet() {
        mContext = InstrumentationRegistry.getContext();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }
/*
    @Rpc(description="Opens the main activity of the app")
    public void startMainActivity() throws UiAutomatorSnippetException {
        // Send the launch intent
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(MAIN_PACKAGE);
        if (intent == null) {
            throw new UiAutomatorSnippetException(
                "Unable to create launch intent for " + MAIN_PACKAGE + "; is the app installed?");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(intent);

        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(MAIN_PACKAGE).depth(0)), LAUNCH_TIMEOUT);
    }
*/

    @Rpc(description="Pushes the main app button, and checks the label if this is the first time.")
    public void pushMainButton(boolean checkFirstRun) {
        if (checkFirstRun) {
            assertEquals(
                "Hello World!",
                // Example of finding object by id.
                mDevice.findObject(By.res(MAIN_PACKAGE, "main_text_view")).getText());
        }
        // Example of finding a button by text. Finding by ID is also possible, as above.
        UiObject2 button = mDevice.findObject(By.text("PUSH THE BUTTON!"));
        button.click();
        if (checkFirstRun) {
            assertEquals(
                "Button pressed 1 times",
                mDevice.findObject(By.res(MAIN_PACKAGE, "main_text_view")).getText());
        }
    }

    @Rpc(description="Perform a UIAutomator dump")
    public String uiautomatorDump() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            mDevice.dumpWindowHierarchy(baos);
            byte[] dumpBytes = baos.toByteArray();
            String dumpStr = new String(dumpBytes, Charset.forName("UTF-8"));
            return dumpStr;
        } finally {
            baos.close();
        }
    }

    @Rpc(description="Click Pair Accept Button")
    public void pairAccept() throws IOException {
        UiObject2 button = mDevice.findObject(By.text("PAIR"));
        button.click();
    }

    @Rpc(description="Click Pair Cancel Button")
    public void pairCancel() throws IOException {
        UiObject2 button = mDevice.findObject(By.text("CANCEL"));
        button.click();
    }

    @Override
    public void shutdown() throws IOException {
        //mDevice.executeShellCommand("am force-stop " + MAIN_PACKAGE);
    }

}

