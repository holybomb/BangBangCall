/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.doubango.bangbangcall.Screens;

import org.doubango.bangbangcall.DEF;
import org.doubango.bangbangcall.Engine;
import org.doubango.bangbangcall.R;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.tinyWRAP.SipStack;
import org.doubango.tinyWRAP.tdav_codec_id_t;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ScreenLogin extends BaseScreen {
	private static String TAG = ScreenLogin.class.getCanonicalName();
	private final INgnConfigurationService mConfigurationService;
	private final INgnSipService mSipService;
	
	private BroadcastReceiver mSipBroadCastRecv;
	
	public ScreenLogin() {
		super(SCREEN_TYPE.LOGIN_T, TAG);
		mSipService = getEngine().getSipService();
		mConfigurationService = getEngine().getConfigurationService();
	}
	public EditText userEdt,passEdt;
	public Button loginBtn,settingBtn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.screen_login);
		String hosts = mConfigurationService.getString(NgnConfigurationEntry.NETWORK_PCSCF_HOST, DEF.DEFAULT_HOSTS);
		int mCodecs =  mConfigurationService.getInt(NgnConfigurationEntry.MEDIA_CODECS, DEF.DEFAULT_CODECS);
		mConfigurationService.putInt(NgnConfigurationEntry.MEDIA_CODECS,  DEF.DEFAULT_CODECS);
		SipStack.setCodecs_2(mCodecs);
		mConfigurationService.putString(NgnConfigurationEntry.NETWORK_PCSCF_HOST, 
				hosts);
		mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM, 
				hosts.trim());
		mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_WIFI, 
				true);
		mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_3G, 
				true);
		if(!mConfigurationService.commit()){
			Log.e(TAG, "Failed to commit() configuration");
		}
		userEdt = (EditText) findViewById(R.id.screen_login_user);
		passEdt = (EditText) findViewById(R.id.screen_login_pw);
		loginBtn = (Button) findViewById(R.id.screen_login_btn);
		settingBtn = (Button) findViewById(R.id.screen_login_setting);
		mSipBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				cancelInProgressOnUiThread();
				// Registration Event
				if(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)){
					NgnRegistrationEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
					if(args == null){
						Log.e(TAG, "Invalid event args");
						return;
					}
					switch(args.getEventType()){
						case REGISTRATION_OK:
							cancelInProgress();
							mScreenService.show(ScreenTabDialer.class,ScreenTabDialer.class.getCanonicalName());
							finish();
							break;
						case REGISTRATION_NOK:
							Toast.makeText(Engine.getInstance().getMainActivity(), "µÇÂ¼Ê§°Ü", Toast.LENGTH_SHORT).show();
							break;
						case UNREGISTRATION_OK:
						case REGISTRATION_INPROGRESS:
						case UNREGISTRATION_INPROGRESS:
						case UNREGISTRATION_NOK:
						default:
							cancelInProgress();
							break;
					}
				}
			}
		};
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
	    registerReceiver(mSipBroadCastRecv, intentFilter);
	    if(!NgnStringUtils.isNullOrEmpty(DEF.mExtenelSipUserName)&&!NgnStringUtils.isNullOrEmpty(DEF.mExtenelSipUserPassword))
		{
	    	userEdt.setText(DEF.mExtenelSipUserName);
	    	passEdt.setText(DEF.mExtenelSipUserPassword);
	    	onLoginClicker(loginBtn);
		}
	}
	
	@Override
	protected void onDestroy() {
		if(mSipBroadCastRecv != null){
			unregisterReceiver(mSipBroadCastRecv);
			mSipBroadCastRecv = null;
		}
		super.onDestroy();
	}
	@Override
	protected void onResume() {
		super.onResume();
	}
	public void onLoginClicker(View v)
	{
		showInProgress("ÇëÉÔºó¡£¡£¡£", true, true);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(changeConfig())
					mSipService.register(ScreenLogin.this);
			}
		}).start();
	}
	public boolean changeConfig()
	{
		mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU, 
				NgnUriUtils.makeValidSipUri(userEdt.getText().toString().trim()));
		mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, 
				userEdt.getText().toString().trim());
		mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD, 
				passEdt.getText().toString().trim());
		// Compute
		if(!mConfigurationService.commit()){
			Log.e(TAG, "Failed to commit() configuration");
			return false;
		}
		return true;
	}
	public void onSettingClicker(View v)
	{
		finish();
		mScreenService.show(ScreenSettings.class,ScreenSettings.class.getCanonicalName());
	}
}