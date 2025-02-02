/*
 * Copyright (C) 2009 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package plugins.quorum.Libraries.Game;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import plugins.quorum.Libraries.Interface.Accessibility.AndroidAccessibility;
import quorum.Libraries.Game.AndroidConfiguration;
import quorum.Libraries.Game.Shapes.Rectangle_;
import quorum.Libraries.Interface.Controls.Charts.Graphics.PieBox;
import quorum.Libraries.Interface.Item2D_;
import quorum.Libraries.Interface.Item3D_;
import quorum.Libraries.Interface.Item_;

import java.util.Map;

import static plugins.quorum.Libraries.Interface.Accessibility.AndroidAccessibility.onHoverVirtualView;

/**
 *
 * @author alleew
 */
public class GLSurfaceView20 extends GLSurfaceView
{
    static String TAG = "GL2JNIView";
    private static final boolean DEBUG = false;
    
    public int width;
    public int height;
    public boolean aspectRatio = false;
    public boolean canProceed = true;

    public final int scaleUpFactor = 10;
    public final int smallElementThreshold = 10;
    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleDetector;
    
    public GLSurfaceView20(Context context, AndroidConfiguration config)
    {
        super(context);
        width = config.targetWidth;
        height = config.targetHeight;
        aspectRatio = config.useAspectRatio;
        Initialize(true, 16, 0);

        gestureDetector = new GestureDetector(context, new QuorumGestureTranslator());
        scaleDetector = new ScaleGestureDetector(context, new QuorumScaleGestureTranslator());
    }
    
    public GLSurfaceView20(Context context, AndroidConfiguration config, boolean translucent, int depth, int stencil)
    {
        super(context);
        width = config.targetWidth;
        height = config.targetHeight;
        aspectRatio = config.useAspectRatio;
        Initialize(translucent, depth, stencil);

        gestureDetector = new GestureDetector(context, new QuorumGestureTranslator());
        scaleDetector = new ScaleGestureDetector(context, new QuorumScaleGestureTranslator());
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        canProceed = false;
        quorum.Libraries.Game.AndroidInput input = (quorum.Libraries.Game.AndroidInput)GameStateManager.input;
        input.plugin_.AddEvent(event);

        gestureDetector.onTouchEvent(event);
        scaleDetector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP)
            input.plugin_.TestLongPressEnd(event);
        canProceed = true;
        return true;
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        if (AndroidApplication.accessibilityManager.isTouchExplorationEnabled() && event.getPointerCount() == 1) {
            final int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER: {
                    event.setAction(MotionEvent.ACTION_DOWN);
                } break;
                case MotionEvent.ACTION_HOVER_MOVE: {
                    event.setAction(MotionEvent.ACTION_MOVE);
                } break;
                case MotionEvent.ACTION_HOVER_EXIT: {
                    event.setAction(MotionEvent.ACTION_UP);
                } break;
            }
            return onTouchEvent(event);
        }
        return true;
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent event)
    {
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_HOVER_ENTER || action == MotionEvent.ACTION_HOVER_EXIT || !canProceed || event == null)
            return true;
        boolean handled = false;
        Rect min = null;
        Item_ minItem = null;

        for (Map.Entry<Integer, Item_> set : AndroidApplication.quorumItems.entrySet())
        {
            Item_ item = set.getValue();
            Rect childBounds;

            if (item instanceof Item2D_)
            {
                double itemX = ((Item2D_)item).GetScreenX();
                double itemY = (((Item2D_) item).GetScreenY());

                if (itemX == Double.NaN)
                    itemX = 0;

                if (itemY == Double.NaN)
                    itemY = 0;
                int left = (int)itemX;
                int top = AndroidApplication.screenHeight - (int)(itemY + ((Item2D_) item).GetHeight());
                int right = (int) (itemX + ((Item2D_) item).GetWidth());
                int bottom = AndroidApplication.screenHeight - (int)(itemY);
                Log.e("Quorum", "dispatchHoverEvent: " + left + " " + top + " " + right + " " + bottom + " Name: " + item.GetName() + " Desc: " + item.GetDescription());
                childBounds = new Rect(left, top, right, bottom);
            }
            else if (item instanceof Item3D_)
            {
                // This is only a place holder, to place a small box roughly at the
                // center of a 3D object in the screen. To calculate this correctly,
                // check how we calculate mouse input detection for 3D objects.

                Rectangle_ rectangle = ((Item3D_) item).GetScreenBounds();

                int left = (int)rectangle.GetX();
                int top = AndroidApplication.screenHeight - (int)(rectangle.GetY() + rectangle.GetHeight());
                int right = (int) (rectangle.GetX() + rectangle.GetWidth());
                int bottom = AndroidApplication.screenHeight - (int)(rectangle.GetY());
                Log.e("Quorum", "dispatchHoverEvent: " + left + " " + top + " " + right + " " + bottom + "Name: " + item.GetName());

                childBounds = new Rect(left, top, right, bottom);
            }
            else
            {
                int left = 0;
                int top = 0;
                int right = 0;
                int bottom = 0;
                Log.e("Quorum", "dispatchHoverEvent: " + left + " " + top + " " + right + " " + bottom + "Name: " + item.GetName());

                childBounds = new Rect(left, top, right, bottom);
            }

            final int childCoordsX = (int) event.getX() + getScrollX();
            final int childCoordsY = (int) event.getY() + getScrollY();
            if (!childBounds.contains(childCoordsX, childCoordsY)) {
                continue;
            }
            else
            {
                if (min == null)
                {
                    min = childBounds;
                    minItem = item;
                }
                else if ((min.height() * min.width()) > (childBounds.height() * childBounds.width()))
                {
                    min = childBounds;
                    minItem = item;
                }
            }
        }
        //Log.e("Coordinates", "dispatchHoverEvent: " + min.left + " " + min.top + " " + min.right + " " + min.bottom);
        if (min != null)
        {
            if(PieBox.class == minItem.getClass())
            {
                if (AndroidApplication.accessibilityManager.isTouchExplorationEnabled() && event.getPointerCount() == 1) {
                    switch (action) {
                        case MotionEvent.ACTION_HOVER_ENTER:
                        case MotionEvent.ACTION_HOVER_MOVE: {
                            event.setAction(MotionEvent.ACTION_DOWN);
                        } break;
                        case MotionEvent.ACTION_HOVER_EXIT: {
                            event.setAction(MotionEvent.ACTION_UP);
                        } break;
                    }
                    return onTouchEvent(event);
                }
            }
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER: {
                    AndroidAccessibility.lastHoveredChild = minItem;
                    handled |= onHoverVirtualView(minItem, event);
                    event.setAction(action);
                } break;
                case MotionEvent.ACTION_HOVER_MOVE: {
                    if (minItem == AndroidAccessibility.lastHoveredChild) {
                        handled |= onHoverVirtualView(minItem, event);
                        event.setAction(action);
                    } else {
                        MotionEvent eventNoHistory = event.getHistorySize() > 0
                                ? MotionEvent.obtainNoHistory(event) : event;
                        eventNoHistory.setAction(MotionEvent.ACTION_HOVER_EXIT);
                        onHoverVirtualView(AndroidAccessibility.lastHoveredChild, eventNoHistory);
                        eventNoHistory.setAction(MotionEvent.ACTION_HOVER_ENTER);
                        onHoverVirtualView(minItem, eventNoHistory);
                        AndroidAccessibility.lastHoveredChild = minItem;
                        eventNoHistory.setAction(MotionEvent.ACTION_HOVER_MOVE);
                        handled |= onHoverVirtualView(minItem, eventNoHistory);
                        if (eventNoHistory != event) {
                            eventNoHistory.recycle();
                        } else {
                            event.setAction(action);
                        }
                    }
                } break;
                case MotionEvent.ACTION_HOVER_EXIT: {
                    AndroidAccessibility.lastHoveredChild = null;
                    handled |= onHoverVirtualView(minItem, event);
                    event.setAction(action);
                } break;
            }
        }

        if (!handled && event.getAction() != MotionEvent.ACTION_UP) {
            handled |= onHoverEvent(event);
        }
        return handled;

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int w;
        int h;
        if (width == 0 && height == 0)
        {
            w = View.MeasureSpec.getSize(widthMeasureSpec);
            h = View.MeasureSpec.getSize(heightMeasureSpec);
        }
        else if (aspectRatio)
        {
            w = View.MeasureSpec.getSize(widthMeasureSpec);
            h = View.MeasureSpec.getSize(heightMeasureSpec);
            float desiredRatio = ((float)width) / height;
            float realRatio = ((float)w) / h;
            
            if (realRatio < desiredRatio)
            {
                h = Math.round(w / desiredRatio);
            }
            else
            {
                w = Math.round(h * desiredRatio);
            }
        }
        else
        {
            w = width;
            h = height;
        }
        setMeasuredDimension(w, h);
    }
    
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs)
    {
        // add this line, the IME can show the selectable words when use chinese input method editor.
        if (outAttrs != null)
        {
            outAttrs.imeOptions = outAttrs.imeOptions | EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        }
        
        BaseInputConnection connection = new BaseInputConnection(this, false) {
            @Override
            public boolean deleteSurroundingText(int beforeLength, int afterLength)
            {
                int sdkVersion = android.os.Build.VERSION.SDK_INT;
                if (sdkVersion >= 16)
                {
                    /*
                    * In Jelly Bean, they don't send key events for delete. Instead, they send beforeLength = 1, afterLength = 0. So,
                    * we'll just simulate what it used to do.
                    */
                   if (beforeLength == 1 && afterLength == 0) {
                        sendDownUpKeyEventForBackwardCompatibility(KeyEvent.KEYCODE_DEL);
                        return true;
                   }
                }
                return super.deleteSurroundingText(beforeLength, afterLength);
            }
            
            @TargetApi(16)
            private void sendDownUpKeyEventForBackwardCompatibility(final int code)
            {
                final long eventTime = SystemClock.uptimeMillis();
                
                super.sendKeyEvent(new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, code, 0, 0,
                    KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
                
                super.sendKeyEvent(new KeyEvent(SystemClock.uptimeMillis(), eventTime, KeyEvent.ACTION_UP, code, 0, 0,
                    KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
            }
        };
        
        return connection;
    }
    
    private void Initialize(boolean translucent, int depth, int stencil)
    {
        /*
        * By default, GLSurfaceView() creates a RGB_565 opaque surface. If we want a translucent one, we should change the
        * surface's format here, using PixelFormat.TRANSLUCENT for GL Surfaces is interpreted as any 32-bit surface with alpha by
        * SurfaceFlinger.
        */
        if (translucent)
        {
            this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }
        
        /*
        * Setup the context factory for 2.0 rendering. See ContextFactory class definition below
        */
        setEGLContextFactory(new ContextFactory());
        
        /*
        * We need to choose an EGLConfig that matches the format of our surface exactly. This is going to be done in our custom
        * config chooser. See ConfigChooser class definition below.
        */
       setEGLConfigChooser(translucent ? new ConfigChooser(8, 8, 8, 8, depth, stencil) : new ConfigChooser(5, 6, 5, 0, depth,
               stencil));

       /* Set the renderer responsible for frame rendering */
    }
    
    static class ContextFactory implements GLSurfaceView.EGLContextFactory
    {
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        public EGLContext createContext (EGL10 egl, EGLDisplay display, EGLConfig eglConfig) 
        {
            Log.w(TAG, "creating OpenGL ES 2.0 context");
            checkEglError("Before eglCreateContext", egl);
            int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
            EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
            checkEglError("After eglCreateContext", egl);
            return context;
        }

        public void destroyContext (EGL10 egl, EGLDisplay display, EGLContext context) {
            egl.eglDestroyContext(display, context);
        }
    }
    
    static void checkEglError (String prompt, EGL10 egl) 
    {
        int error;
        while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) 
        {
            Log.e(TAG, String.format("%s: EGL error: 0x%x", prompt, error));
        }
    }
    
    private static class ConfigChooser implements GLSurfaceView.EGLConfigChooser 
    {
        // Subclasses can adjust these values:
        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;
        private int[] mValue = new int[1];
        
        public ConfigChooser (int r, int g, int b, int a, int depth, int stencil) 
        {
            mRedSize = r;
            mGreenSize = g;
            mBlueSize = b;
            mAlphaSize = a;
            mDepthSize = depth;
            mStencilSize = stencil;
        }

        /*
         * This EGL config specification is used to specify 2.0 rendering. We use a minimum size of 4 bits for red/green/blue, but
         * will perform actual matching in chooseConfig() below.
         */
        private static int EGL_OPENGL_ES2_BIT = 4;
        private static int[] s_configAttribs2 = {EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4,
                EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE};

        public EGLConfig chooseConfig (EGL10 egl, EGLDisplay display) 
        {
            /*
             * Get the number of minimally matching EGL configurations
             */
            int[] num_config = new int[1];
            egl.eglChooseConfig(display, s_configAttribs2, null, 0, num_config);

            int numConfigs = num_config[0];

            if (numConfigs <= 0) 
            {
                throw new IllegalArgumentException("No configs match configSpec");
            }

            /*
             * Allocate then read the array of minimally matching EGL configs
             */
            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, s_configAttribs2, configs, numConfigs, num_config);

            if (DEBUG) 
            {
                printConfigs(egl, display, configs);
            }
            /*
             * Now return the "best" one
             */
            return chooseConfig(egl, display, configs);
        }

        public EGLConfig chooseConfig (EGL10 egl, EGLDisplay display, EGLConfig[] configs) 
        {
            for (EGLConfig config : configs) 
            {
                int d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);

                // We need at least mDepthSize and mStencilSize bits
                if (d < mDepthSize || s < mStencilSize) 
                    continue;

                // We want an *exact* match for red/green/blue/alpha
                int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
                int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
                int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
                int a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);

                if (r == mRedSize && g == mGreenSize && b == mBlueSize && a == mAlphaSize)
                    return config;
            }
            return null;
        }

        private int findConfigAttrib (EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) 
        {
            if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) 
            {
                return mValue[0];
            }
            return defaultValue;
        }

        private void printConfigs (EGL10 egl, EGLDisplay display, EGLConfig[] configs) 
        {
            int numConfigs = configs.length;
            Log.w(TAG, String.format("%d configurations", numConfigs));
            for (int i = 0; i < numConfigs; i++) 
            {
                Log.w(TAG, String.format("Configuration %d:\n", i));
                printConfig(egl, display, configs[i]);
            }
        }

        private void printConfig (EGL10 egl, EGLDisplay display, EGLConfig config) 
        {
            int[] attributes = {EGL10.EGL_BUFFER_SIZE, EGL10.EGL_ALPHA_SIZE, EGL10.EGL_BLUE_SIZE, EGL10.EGL_GREEN_SIZE,
                EGL10.EGL_RED_SIZE, EGL10.EGL_DEPTH_SIZE, EGL10.EGL_STENCIL_SIZE, EGL10.EGL_CONFIG_CAVEAT, EGL10.EGL_CONFIG_ID,
                EGL10.EGL_LEVEL, EGL10.EGL_MAX_PBUFFER_HEIGHT, EGL10.EGL_MAX_PBUFFER_PIXELS, EGL10.EGL_MAX_PBUFFER_WIDTH,
                EGL10.EGL_NATIVE_RENDERABLE, EGL10.EGL_NATIVE_VISUAL_ID, EGL10.EGL_NATIVE_VISUAL_TYPE,
                0x3030, // EGL10.EGL_PRESERVED_RESOURCES,
                EGL10.EGL_SAMPLES, EGL10.EGL_SAMPLE_BUFFERS, EGL10.EGL_SURFACE_TYPE, EGL10.EGL_TRANSPARENT_TYPE,
                EGL10.EGL_TRANSPARENT_RED_VALUE, EGL10.EGL_TRANSPARENT_GREEN_VALUE, EGL10.EGL_TRANSPARENT_BLUE_VALUE, 0x3039, // EGL10.EGL_BIND_TO_TEXTURE_RGB,
                0x303A, // EGL10.EGL_BIND_TO_TEXTURE_RGBA,
                0x303B, // EGL10.EGL_MIN_SWAP_INTERVAL,
                0x303C, // EGL10.EGL_MAX_SWAP_INTERVAL,
                EGL10.EGL_LUMINANCE_SIZE, EGL10.EGL_ALPHA_MASK_SIZE, EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RENDERABLE_TYPE, 0x3042 // EGL10.EGL_CONFORMANT
            };
            String[] names = {"EGL_BUFFER_SIZE", "EGL_ALPHA_SIZE", "EGL_BLUE_SIZE", "EGL_GREEN_SIZE", "EGL_RED_SIZE",
                "EGL_DEPTH_SIZE", "EGL_STENCIL_SIZE", "EGL_CONFIG_CAVEAT", "EGL_CONFIG_ID", "EGL_LEVEL", "EGL_MAX_PBUFFER_HEIGHT",
                "EGL_MAX_PBUFFER_PIXELS", "EGL_MAX_PBUFFER_WIDTH", "EGL_NATIVE_RENDERABLE", "EGL_NATIVE_VISUAL_ID",
                "EGL_NATIVE_VISUAL_TYPE", "EGL_PRESERVED_RESOURCES", "EGL_SAMPLES", "EGL_SAMPLE_BUFFERS", "EGL_SURFACE_TYPE",
                "EGL_TRANSPARENT_TYPE", "EGL_TRANSPARENT_RED_VALUE", "EGL_TRANSPARENT_GREEN_VALUE", "EGL_TRANSPARENT_BLUE_VALUE",
                "EGL_BIND_TO_TEXTURE_RGB", "EGL_BIND_TO_TEXTURE_RGBA", "EGL_MIN_SWAP_INTERVAL", "EGL_MAX_SWAP_INTERVAL",
                "EGL_LUMINANCE_SIZE", "EGL_ALPHA_MASK_SIZE", "EGL_COLOR_BUFFER_TYPE", "EGL_RENDERABLE_TYPE", "EGL_CONFORMANT"};
            int[] value = new int[1];
            for (int i = 0; i < attributes.length; i++) 
            {
                int attribute = attributes[i];
                String name = names[i];
                if (egl.eglGetConfigAttrib(display, config, attribute, value)) 
                {
                    Log.w(TAG, String.format("  %s: %d\n", name, value[0]));
                }
                else 
                {
                    Log.w(TAG, String.format("  %s: failed\n", name));
                    while (egl.eglGetError() != EGL10.EGL_SUCCESS);
                }
            }
        }
    }

    private class QuorumGestureTranslator extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onDown(MotionEvent event)
        {
            // We're required to return true here -- if it returns false, all other gestures will fail.
            // We could conditionally return false to temporarily block gesture input, but otherwise it must be true.
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event)
        {
            quorum.Libraries.Game.AndroidInput input = (quorum.Libraries.Game.AndroidInput)GameStateManager.input;
            input.plugin_.AddDoubleTapEvent(event);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY)
        {
            quorum.Libraries.Game.AndroidInput input = (quorum.Libraries.Game.AndroidInput)GameStateManager.input;
            input.plugin_.AddFlingEvent(event1, event2, velocityX, velocityY);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent event)
        {
            quorum.Libraries.Game.AndroidInput input = (quorum.Libraries.Game.AndroidInput)GameStateManager.input;
            input.plugin_.AddLongPressEvent(event);
        }

        @Override
        public void onShowPress(MotionEvent e)
        {
            // We currently don't do anything with the show press.
        }

        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY)
        {
            quorum.Libraries.Game.AndroidInput input = (quorum.Libraries.Game.AndroidInput)GameStateManager.input;
            input.plugin_.AddScrollEvent(event1, event2, distanceX, distanceY);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event)
        {
            quorum.Libraries.Game.AndroidInput input = (quorum.Libraries.Game.AndroidInput)GameStateManager.input;
            input.plugin_.AddSingleTapEvent(event);
            return true;
        }
    }

    private class QuorumScaleGestureTranslator extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector)
        {
            quorum.Libraries.Game.AndroidInput input = (quorum.Libraries.Game.AndroidInput)GameStateManager.input;
            input.plugin_.AddScaleBeginEvent(detector);
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            quorum.Libraries.Game.AndroidInput input = (quorum.Libraries.Game.AndroidInput)GameStateManager.input;
            input.plugin_.AddScaleContinueEvent(detector);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector)
        {
            quorum.Libraries.Game.AndroidInput input = (quorum.Libraries.Game.AndroidInput)GameStateManager.input;
            input.plugin_.AddScaleEndEvent(detector);
        }
    }
}
