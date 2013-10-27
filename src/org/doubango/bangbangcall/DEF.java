package org.doubango.bangbangcall;

import org.doubango.tinyWRAP.tdav_codec_id_t;

public class DEF {
	public static String DEFAULT_HOSTS = "115.28.38.216";
	public static String mExtenelSipUserName = null;
	public static String mExtenelSipUserPassword = null;
	public static String mExtenelSipCallUser = null;
	public static int DEFAULT_CODECS = 
		tdav_codec_id_t.tdav_codec_id_pcma.swigValue()|
		tdav_codec_id_t.tdav_codec_id_pcmu.swigValue()|
		tdav_codec_id_t.tdav_codec_id_g729ab.swigValue() |
		tdav_codec_id_t.tdav_codec_id_h263.swigValue() ;

}
