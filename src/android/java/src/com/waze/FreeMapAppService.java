/**  
 * FreeMapAppService.java
 * This class represents the android service controlling different inter-activities interaction.
 * It also increases the importance of the entire application process from the android system view.
 * The implementation is "NO IPC" because the calls are from inside the application 
 * 
 * LICENSE:
 *
 *   Copyright 2008 	@author Alex Agranovich
 *   
 *
 *   This file is part of RoadMap.
 *
 *   RoadMap is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   RoadMap is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with RoadMap; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * SYNOPSYS:
 *
 *   @see FreeMapNativeActivity_JNI.h/c
 */


package com.waze;

import com.waze.FreeMapNativeManager.FreeMapUIEvent;
import com.waze.WazeLog.LogCatMonitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

final public class FreeMapAppService extends Service
{
    @Override
    public void onCreate()
    {
    	super.onCreate();
    //	Log.w( LOG_TAG, "Service Created. Instance: " + this );
    }

    @Override
    public void onStart( Intent aIntent, int aStartId )
    {
        super.onStart(aIntent, aStartId);
        // Initialize the service instance
        mInstance = this;
        
        // Draw splash background ( ApplicationContext should be available. Can be run from onCreate of Activity however
        // in this case another functions (IsHd) should be aware of the ApplicationContext somehow )
        getMainView().DrawSplashBackground();
        
        if ( WAZE_LOGCAT_MONITOR_ENABLED )
        {
	        // Create the logcat thread
	        mLogCatMonitor = new LogCatMonitor();
	        // Start the logcat thread
	        mLogCatMonitor.start();
        }
        // Reduce the possibility to be killed 
        setForeground( true );
        
//        registerReceiver( mScreenManager, new IntentFilter( Intent.ACTION_SCREEN_OFF ) );
//        registerReceiver( mScreenManager, new IntentFilter( Intent.ACTION_SCREEN_ON ) );

        
        Log.w( LOG_TAG, "Service started. Instance: " + mInstance );
    }

    @Override
    public void onDestroy()
    {
        if ( mNativeManager != null )
        {
            mNativeManager.RestoreSystemSettings();
//            unregisterReceiver( mStandbyManager );
        }
        Log.w( LOG_TAG, "Service destroy." );
    }

    /*************************************************************************************************
     * Initializes the managers and starts the application
     * 
     * @param aContext
     *            - reference to the Activity context
     */
    public static void StartApp( FreeMapAppActivity aContext )
    {
        mMainContext = aContext;
        
        try
        {
	        // Power manager
	        mPowerManager = new FreeMapPowerManager();

	        // Start the native manager
	        mNativeManager = FreeMapNativeManager.Start();

	        // Standby manager
	        mStandbyManager = new WazeStandbyManager( mNativeManager );

	        // Screen manager
	        mScreenManager = new WazeScreenManager( mNativeManager );
	        
	        // Set notification
	        SetNotification( WAZE_NOTIFICATION_RUNNING );
        }
        catch( Exception ex )
        {
        	Log.e( "WAZE", "Error starting the application " + ex.getMessage() );
        	ex.printStackTrace();
        }

    }
    
    
    /*************************************************************************************************
     * Returns the native manager reference
     * 
     * @param void
     *            
     */
    public static FreeMapNativeManager getNativeManager()
    {
        return mNativeManager;
    }
    
    
    /*************************************************************************************************
     * Returns the power manager reference
     * 
     * @param void
     *            
     */
    public static FreeMapPowerManager getPowerManager()
    {
        return mPowerManager;
    }
    
    /*************************************************************************************************
     * Returns the standby manager reference
     * @param void
     */
    public static WazeStandbyManager getStandbyManager()
    {
        return mStandbyManager;
    }
    /*************************************************************************************************
     * Returns the screen manager reference
     * @param void
     */
    public static WazeScreenManager getScreenManager()
    {
        return mScreenManager;
    }

    /*************************************************************************************************
     * Returns the main context reference
     * 
     * @param void
     *            
     */
    public static FreeMapAppActivity getMainActivity()
    {
        return mMainContext;
    }
    /*************************************************************************************************
     * Returns the application vew reference
     * 
     * @param void
     *            
     */
    public static WazeMainView getMainView()
    {
    	WazeMainView mainView = null;
    	if ( mMainContext != null )
    		mainView = mMainContext.getMainView();
    	return mainView;    	
    }
    /*************************************************************************************************
     * Returns the application context reference
     * 
     * @param void
     *            
     */
    public static Context getAppContext()
    {
        return mInstance.getApplicationContext();
    }
    
    /*************************************************************************************************
     * Returns the application url parameters reference
     * 
     * @param void
     *            
     */
    public static String getUrl()
    {
        return mUrl;
    }
    /*************************************************************************************************
     * Sets the application url parameters reference
     * 
     * @param String aUrl
     *            
     */
    public static void setUrl( String aUrl )
    {
		mUrl = aUrl;
    }
    
    /*************************************************************************************************
     * Shows the main activity window with the delay of aDelay msec-s
     * 
     * @param aDelay
     *            - delay in showing the main activity window
     */
    public static void ShowMainActivityWindow( long aDelay )
    {
        // Send message to show the Dialer
        mServiceMsgDispatcher.sendEmptyMessageDelayed(MSG_SHOW_MAIN_ACTIVITY, aDelay);
    }
    /*************************************************************************************************
     * Request the application restart
     * 
     * @param void
     *        
     */
    public static void RestartApplication()
    {     
    	mServiceMsgDispatcher.sendEmptyMessage( MSG_RESTART_APPLICATION );    	
    }
    
    /*************************************************************************************************
     * 
     * 
     * @param aDelay
     *            - delay in showing the main activity window
     */
    public static void OpenBrowserForUpgrade()
    {
        // Send message to show the Dialer
        mServiceMsgDispatcher.sendEmptyMessage( MSG_OPEN_BROWSER_FOR_UPGRADE );
    }


    /*************************************************************************************************
     * Shows the home window. Waits aDelay msec-s before return to the main
     * context
     * 
     * @param aDelay
     *            - time (in msec-s) to wait before return to the main window.
     *            -1 - no return
     */
    public static void ShowHomeWindow( long aDelay )
    {
        // Send message to show the Dialer
        mServiceMsgDispatcher.sendEmptyMessage(MSG_SHOW_HOME_SCREEN);
        if (aDelay >= 0)
        {
            mServiceMsgDispatcher.sendEmptyMessageDelayed(MSG_SHOW_MAIN_ACTIVITY, aDelay);
        }
    }
    /*************************************************************************************************
     * Shows the contacts list
     * 
     * @param void
     */
    public static void ShowContacts()
    {
        mServiceMsgDispatcher.sendEmptyMessage( MSG_SHOW_CONTACTS );
    }

    /*************************************************************************************************
     * Shows the dialer window. Waits aDelay msec-s before return to the main
     * context
     * 
     * @param aDelay
     *            - time (in msec-s) to wait before return to the main window.
     *            -1 - no return
     */
    public static void ShowDilerWindow( long aDelay )
    {
//        Log.d(LOG_TAG, "Post message request for Dialer");
        // Send message to show the Dialer
        mServiceMsgDispatcher.sendEmptyMessage(MSG_SHOW_DIALER_SCREEN);
        if (aDelay >= 0)
        {
            mServiceMsgDispatcher.sendEmptyMessageDelayed(MSG_SHOW_MAIN_ACTIVITY, aDelay);
        }
    }

    /*************************************************************************************************
     * Shows the Camera preview window
     * 
     */
    public static void ShowCameraPreviewWindow()
    {
//        Log.d(LOG_TAG, "Post message request for Camera");
        // Send message to show the Camera
        mServiceMsgDispatcher.sendEmptyMessage(MSG_SHOW_CAMERA_PREVIEW);
    }

    /*************************************************************************************************
     * Must be overridden ....
     */
    @Override
    public IBinder onBind( Intent intent )
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*************************************************************************************************
     * This class implements the message handler loop for the service. Can be
     * used inside the application only
     * 
     * @author Alex Agranovich
     */
    private final static class ServiceMsgDispatcher extends Handler
    {
        @Override
        public void handleMessage( Message aMsg )
        {
            switch (aMsg.what)
            {
                // Show the main activity
                case MSG_SHOW_MAIN_ACTIVITY:
                {
                    ShowMainActivityHandler();
                    break;
                }
                    // Show the home screen
                case MSG_SHOW_HOME_SCREEN:
                {
                    ShowHomeHandler();
                    break;
                }
                    // Show the dialer screen
                case MSG_SHOW_DIALER_SCREEN:
                {
                    ShowDialerHandler();
                    break;
                }
                // Show the dialer screen
                case MSG_SHOW_CAMERA_PREVIEW:
                {
                    ShowCameraPreviewHandler();
                    break;
                }
                // Show the dialer screen
                case MSG_OPEN_BROWSER_FOR_UPGRADE:
                {
                    OpenBrowserForUpgradeHandler();
                    break;
                }
                case MSG_RESTART_APPLICATION:
                {
                	RestartApplicationHandler();
                	break;
                }
                case MSG_SHOW_CONTACTS:
                {
                	ShowContactsHandler();
                	break;
                }


            }
        }
    };
    /*************************************************************************************************
     * Shows the contacts list
     * 
     */
    private static void ShowContactsHandler()
    {
        Intent contactsIntent = new Intent( Intent.ACTION_VIEW );
        contactsIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        contactsIntent.setData( Uri.parse( SHOW_CONTACTS_URI ) );
        mInstance.startActivity( contactsIntent );
    }
    
    /*************************************************************************************************
     * Restarts the application using the alarm
     * TODO:: Check why not working
     */
    private static void RestartApplicationHandler()
    {
    	WazeIntentManager.RequestRestart( mMainContext );
    	mNativeManager.ShutDown();
    }

    /*************************************************************************************************
     * Opens the browser to download the handler
     */
    private static void OpenBrowserForUpgradeHandler()
    {
        if ( mInstance == null )
            return;
        
        Intent browserIntent = new Intent( Intent.ACTION_VIEW );
        Uri uri = Uri.parse( WAZE_UPGRADE_URL );
        browserIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        browserIntent.setData( uri );
        mInstance.startActivity( browserIntent );
    }
    /*************************************************************************************************
     * Shows the main activity by sending the intent to the main context
     */
    private static void ShowMainActivityHandler()
    {
        if (mInstance == null)
            return;

        Intent mainActivityIntent = new Intent( getAppContext(), FreeMapAppActivity.class );
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        mInstance.startActivity(mainActivityIntent);
    }

    /*************************************************************************************************
     * Shows the home screen by sending the MAIN intent
     */
    private static void ShowHomeHandler()
    {
        if (mInstance == null)
            return;

        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mInstance.startActivity(homeIntent);
    }

    /*************************************************************************************************
     * Shows the dialer by sending the ACTION_DIAL intent to the system
     */
    private static void ShowDialerHandler()
    {
        if (mInstance == null)
            return;
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mInstance.startActivity(dialIntent);
    }

    /*************************************************************************************************
     * Updates the notification bar
     */
    private static void SetNotification( int aNotification )
    {
    	int icon = R.drawable.notification;	        // icon from resources
    	
    	CharSequence tickerText = "waze";              // ticker-text
    	long when = System.currentTimeMillis();         // notification time
    	Context context = (Context) mMainContext;
    	CharSequence contentTitle = "waze";  // expanded message title
    	CharSequence contentText = "return to waze";      // expanded message text

    	Intent notificationIntent = new Intent( mMainContext, FreeMapAppActivity.class );
    	PendingIntent contentIntent = PendingIntent.getActivity( mMainContext, 0, notificationIntent, 0 );

    	Notification notification = new Notification ( icon, tickerText, when );
    	notification.setLatestEventInfo( context, contentTitle, contentText, contentIntent );
    	
    	notification.flags |=  Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;    
    	
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
    	mNotificationManager.notify( aNotification, notification );
    	mCurrentNotification = aNotification;
    }
    
    /*************************************************************************************************
     * Updates the notification bar
     */
    private static void ClearNotification()
    {
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager mNotificationManager = (NotificationManager) mMainContext.getSystemService(ns);
    	mNotificationManager.cancel( mCurrentNotification );
    }
    /*************************************************************************************************
     * Shows the camera preview by sending the appropriate intent to the
     * activity
     */
    private static void ShowCameraPreviewHandler()
    {
        if (mInstance == null)
            return;
        Intent cameraIntent = new Intent(mInstance, FreeMapCameraActivity.class);
        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mInstance.startActivity(cameraIntent);
    }

    /*************************************************************************************************
     * The activity being used as a main activity
     */
    public static void setMainContext( FreeMapAppActivity aContext )
    {
        mMainContext = aContext;
    }

    /*************************************************************************************************
     * Service initialized indicator
     */
    public static boolean IsInitialized()
    {
        return ( mInstance != null );
    }
    /*************************************************************************************************
     * Service initialized indicator
     */
    public static boolean IsAppRunning()
    {
        return ( IsInitialized() && mNativeManager != null && mNativeManager.getInitializedStatus() );
    }

    
    /*************************************************************************************************
     * The activity being used as a main activity
     */
    public void  onLowMemory()
    {
        Log.w( "Waze Service", "Low memory warning!!!" );
        if ( mNativeManager != null )
        	mNativeManager.PostUIMessage( FreeMapUIEvent.UI_EVENT_LOW_MEMORY );
    }
    
    /*************************************************************************************************
     * Stops the service
     */
    public static void ShutDown()
    {
        if (mInstance == null)
            return;
        if ( mLogCatMonitor != null ) 
            mLogCatMonitor.Destroy();
        mInstance.stopSelf();
        
        ClearNotification();
        
    }
    
	public final static int MSG_SHOW_MAIN_ACTIVITY = 0; // This message will
														// cause the main
														// application window to
														// be shown
	public final static int MSG_SHOW_HOME_SCREEN = 1; // This message will cause
														// the home window to be
														// shown
	public final static int MSG_SHOW_DIALER_SCREEN = 2; // This message will
														// cause the dialer
														// window to be shown
	public final static int MSG_SHOW_CAMERA_PREVIEW = 3; // This message will
															// cause the camera
															// preview window to
															// be shown
   public final static int MSG_OPEN_BROWSER_FOR_UPGRADE = 4; // This message will
                                                               // cause the camera
                                                               // preview window to
                                                               // be shown 
   public final static int MSG_RESTART_APPLICATION = 5; // This message will
													   // cause the application to be restarted
   
   public final static int MSG_SHOW_CONTACTS = 6; // This message will
   														// cause the application to be restarted

   
   public final static String WAZE_UPGRADE_URL = "m.waze.com"; 
   
   public final static String SHOW_CONTACTS_URI = "content://contacts/people/";

	private static ServiceMsgDispatcher mServiceMsgDispatcher = new ServiceMsgDispatcher(); // Custom
																							// service
																							// message
																							// dispatcher	
	private static FreeMapAppActivity mMainContext; // Main context created the
												    // service

    private static FreeMapPowerManager mPowerManager;
    
    private static WazeStandbyManager mStandbyManager;
    
    private static WazeScreenManager mScreenManager;
    
    private static FreeMapNativeManager mNativeManager = null; // Native manager reference

    private static String 	mUrl = null;		// Start Up URL parameters

	
    private static FreeMapAppService    mInstance               = null;

    private final static String         LOG_TAG                 = "WAZE Service";

    private static final int WAZE_NOTIFICATION_RUNNING	 = 	1;
    
    private static final boolean WAZE_LOGCAT_MONITOR_ENABLED	 = 	false;
    
    private static int 			mCurrentNotification;
    
    static public LogCatMonitor mLogCatMonitor = null;          // Log cat monitor thread   
}
